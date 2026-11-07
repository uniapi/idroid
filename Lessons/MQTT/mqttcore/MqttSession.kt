/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F2\u06F7
 */

package mqttcore

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicInteger
import java.net.SocketTimeoutException

class MqttSession(
	private val transport: MqttTransport,
	// Inject null to dynamically spin up a clean single-threaded worker locally
	private val ioExecutor: ExecutorService? = null,
	// Structural outbox bucket persitent across socket lifecycles
	private val inflightPackets: ConcurrentHashMap<Int, MqttPacket>,
	private val onPacketReceived: (session: MqttSession, packet: MqttPacket) -> Unit,
	private val onSessionClosed: (session: MqttSession, exception: Exception?) -> Unit
) {
	private val isUsingInternalExecutor = (ioExecutor == null)
	private val activeExecutor: ExecutorService = ioExecutor ?: Executors.newSingleThreadExecutor { runnable ->
		Thread(runnable, "mqtt-fallback-io-${clientId ?: "unknown"}").apply {
			isDaemon = true
		}
	}
	@Volatile var isConnected: Boolean = true
	var clientId: String? = null
	var keepAliveSeconds: Int = 0
		set(value) {
			field = value
			if (value > 0 && isConnected) {
				startKeepAliveMonitor()
			}
		}
	private val packetIdCounter = AtomicInteger(1)
	private val writeLock = Any()
	@Volatile private var lastWriteTimestamp: Long = System.currentTimeMillis()
	@Volatile private var lastReadTimestamp: Long = System.currentTimeMillis()
	@Volatile private var pingOutstanding = false
	private var keepAliveScheduler: ScheduledExecutorService? = null
	/**
	 * Safely loads a batch of historical/offline packets into this active session.
	 */
	fun restoreInflightPackets(packets: Map<Int, MqttPacket>) {
		if (packets.isNotEmpty()) {
			this.inflightPackets.putAll(packets)
		}
	}
	/**
     * Exposes a snapshot or read-only view of the current in-flight packets,
     * or a direct reference specifically intended for cleanup extraction.
     */
	fun getInflightPackets(): Map<Int, MqttPacket> {
		return this.inflightPackets
	}
	/*
	 * Spins up the connection data injection tracking loop on the active thread executor engine.
	 */
	fun startReadingLoop() {
		activeExecutor.submit {
			try {
				val oldName = Thread.currentThread().name
				Thread.currentThread().name = "mqtt-io-${clientId ?: "unknown"}"
				try {
					while (isConnected) {
						val packet = MqttPacket.read(transport.inputStream)
						lastReadTimestamp = System.currentTimeMillis()
						when (packet.type) {
							MqttPacketType.PINGRESP -> {
								pingOutstanding = false
							}
							MqttPacketType.PUBACK,
							MqttPacketType.PUBCOMP,
							MqttPacketType.SUBACK,
							MqttPacketType.UNSUBACK -> {
								packet.packetId?.let { inflightPackets.remove(it) }
							}
							else -> {}
						}
						onPacketReceived(this, packet)
					}
				} finally {
					Thread.currentThread().name = oldName
				}
			} catch (e: Exception) {
				close(e)
			}
		}
	}
	/*
	 * Schedules periodic keep-alive checks to eliminate clock drift and handle silent network drops.
	 */
	private fun startKeepAliveMonitor() {
		val keepAliveMs = keepAliveSeconds * 1000L
		val checkInterval = keepAliveMs / 2
		synchronized(writeLock) {
			keepAliveScheduler?.shutdown()
			if (!isConnected) return
			val scheduler = Executors.newSingleThreadScheduledExecutor { runnable ->
				Thread(runnable, "mqtt-keepalive-${clientId ?: "unknown"}").apply {
					isDaemon = true
				}
			}
			keepAliveScheduler = scheduler
			scheduler.scheduleAtFixedRate({
				if (!isConnected) return@scheduleAtFixedRate
				val now = System.currentTimeMillis()
				// MQTT 3.1.1 Spec: Keep alive window updates on BOTH sent and received frames
				val maxTimestamp = maxOf(lastWriteTimestamp, lastReadTimestamp)
				val idleTime = now - maxTimestamp
				if (idleTime >= keepAliveMs) {
					if (pingOutstanding) {
						close(SocketTimeoutException("Keep Alive timeout: Broker missed PINGRESP deadline."))
						return@scheduleAtFixedRate
					}
					pingOutstanding = true
					send(MqttPacket(MqttPacketType.PINGREQ))
				}
			}, checkInterval, checkInterval, TimeUnit.MILLISECONDS)
		}
	}
	/**
	 * Synchronizes outbound writes to protect the shared network stream from data corruption.
	 */
	fun send(packet: MqttPacket) {
		if (!isConnected) return
		val qos = (packet.flags ushr 1) and 0x03
		if ((packet.type == MqttPacketType.PUBLISH && qos > 0) ||
			packet.type == MqttPacketType.SUBSCRIBE ||
			packet.type == MqttPacketType.UNSUBSCRIBE) {
			packet.packetId?.let { inflightPackets[it] = packet }
		}
		synchronized(writeLock) {
			if (!isConnected) return
			try {
				packet.write(transport.outputStream)
				lastWriteTimestamp = System.currentTimeMillis()
			} catch (e: Exception) {
				close(e)
			}
		}
	}
	/**
	 * Atomically loops through identifiers from 1 to 65535, preventing multi-threaded collisions.
	 */
	fun nextPacketId(): Int {
		while (true) {
			val current = packetIdCounter.get()
			val next = if (current >= 65535) 1 else current + 1
			if (packetIdCounter.compareAndSet(current, next)) {
				return current
			}
		}
	}
	/**
	 * Gracefully tears down active pipelines and halts monitoring schedulers cleanly.
	 */
	fun close(exception: Exception? = null) {
		val executeCallback: Boolean
		synchronized(writeLock) {
			executeCallback = isConnected
			isConnected = false
			keepAliveScheduler?.shutdown()
			keepAliveScheduler = null
			if (isUsingInternalExecutor) {
				activeExecutor.shutdown()
			}
		}
		// Executed OUTSIDE the synchronization block to prevent blocking lock deadlocks
		try { transport.close() } catch (_: Exception) {}
		inflightPackets.clear()
		if (executeCallback) {
			onSessionClosed(this, exception)
		}
	}
}
