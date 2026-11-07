/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F2\u06F8
 */

package mqttcore

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.CopyOnWriteArrayList

class MqttClient(private val transportProvider: () -> MqttTransport) {
	// Interface facade to bubble server events cleanly up to UI or foreground services
	interface Listener {
		fun onConnected(returnCode: Int, sessionPresent: Boolean)
		fun onMessageArrived(topic: String, payload: ByteArray, qos: Int, retain: Boolean, duplicate: Boolean)
		fun onConnectionLost(cause: Throwable?)
	}
	@Volatile private var session: MqttSession? = null
	@Volatile var isHandshakeComplete: Boolean = false
		private set
	val listeners = CopyOnWriteArrayList<Listener>()
	// Persistent ledger that survives individual socket session teardowns
	private val persistentInflightPackets = ConcurrentHashMap<Int, MqttPacket>()
	/*
	 * Initializes the transport pipeline, constructs the connection layout, and sends the CONNECT handshake.
	 */
	fun connect(
		clientId: String,
		username: String = "",
		password: Any = ByteArray(0),
		keepAliveSeconds: Int = 60,
		cleanSession: Boolean = true,
		willTopic: String = "",
		willMessage: Any = ByteArray(0),
		willQos: Int = 0,
		willRetain: Boolean = false
	) {
		val transport = transportProvider()
		if (cleanSession) {
			persistentInflightPackets.clear()
		}
		val clientSession = MqttSession(
			transport = transport,
			ioExecutor = null,	// Leverages the clean local single-thread fallback inside MqttSession
			inflightPackets = persistentInflightPackets,
			onPacketReceived = { _, packet -> handleIncomingPacket(packet) },
			onSessionClosed = { targetSession, ex -> handleDisconnect(targetSession, ex) }
		).apply {
			this.clientId = clientId
			this.keepAliveSeconds = keepAliveSeconds
		}
		this.session = clientSession
		this.isHandshakeComplete = false
		// 1. Pack dynamic variables into the payload byte chunk via stateless MqttPayload
		val payload = MqttPayload.packConnect(clientId, username, password, willTopic, willMessage)
		// 2. Map structural constructor inputs using named explicit arguments
		val packet = MqttPacket(
			payload = payload,
			hasUsername = username.isNotEmpty(),
			hasPassword = when (password) {
				is ByteArray -> password.size > 0
				is String -> password.isNotEmpty()
				else -> password.toString().isNotEmpty()
			},
			keepAliveSeconds = keepAliveSeconds,
			hasWill = willTopic.isNotEmpty(),
			willQos = willQos,
			willRetain = willRetain
		)
		// Reading thread must spin BEFORE writing onto the pipe to catch rapid server drops
		clientSession.startReadingLoop()
		clientSession.send(packet)
	}
	/**
	 * Encapsulates raw variables into an explicit MqttPacket, routing QoS states to tracing queues.
	 */
	fun publish(topic: String, payload: ByteArray, qos: Int = 0, retain: Boolean = false) {
		val currentSession = session
		if (currentSession == null || !isHandshakeComplete) {
			throw IllegalStateException("No authorized, active connection established with the broker.")
		}
		val packetId = if (qos > 0) currentSession.nextPacketId() else null
		val packet = MqttPacket(topic, payload, qos, retain, packetId, false)
		if (qos > 0) {
			persistentInflightPackets[packetId!!] = packet
		}
		currentSession.send(packet)
	}
	/*
	 * Packs specific topic targets via the stateless MqttPayload engine and transmits a SUBSCRIBE request.
	 */
	fun subscribe(subscriptions: List<Pair<String, Int>>) {
		val currentSession = session
		if (currentSession == null || !isHandshakeComplete) {
			throw IllegalStateException("No authorized, active connection established with the broker.")
		}
		val packetId = currentSession.nextPacketId()
		val packet = MqttPacket(MqttPacketType.SUBSCRIBE, packetId, MqttPayload.packSubscribe(subscriptions))
		currentSession.send(packet)
	}
	/**
	 * Submits an unsubscribe packet to remove server routing pathways for specific topic channels.
	 */
	fun unsubscribe(topics: List<String>) {
		val currentSession = session
		if (currentSession == null || !isHandshakeComplete) {
			throw IllegalStateException("No authorized, active connection established with the broker.")
		}
		val packetId = currentSession.nextPacketId()
		val packet = MqttPacket(MqttPacketType.UNSUBSCRIBE, packetId, MqttPayload.packUnsubscribe(topics))
		currentSession.send(packet)
	}
	/*
	 * Gracefully notifies the broker and stops background workers instantly.
	 */
	fun disconnect() {
		val currentSession = session ?: return
		isHandshakeComplete = false
		try {
			currentSession.send(MqttPacket(MqttPacketType.DISCONNECT))
		} catch (_: Exception) {}
		currentSession.close()
	}
	/**
	 * Handles handshake verification and automatic acknowledgments.
	 */
	private fun handleIncomingPacket(packet: MqttPacket) {
		when (packet.type) {
			MqttPacketType.CONNACK -> {
				val returnCode = packet.properties["returnCode"] as Int
				val sessionPresent = packet.properties["sessionPresent"] as Boolean
				if (returnCode == 0) {
					isHandshakeComplete = true
					flushInflightPackets()	// Sweep and flush outbox unacknowledged backlog queue
				}
				listeners.forEach { it.onConnected(returnCode, sessionPresent) }
			}
			MqttPacketType.PUBLISH -> {
				val topic = packet.topic ?: return
				val payload = packet.payload
				val qos = (packet.flags ushr 1) and 0x03
				val retain = ((packet.flags ushr 3) and 0x01) == 1
				val duplicate = ((packet.flags ushr 3) and 0x01) == 1
				packet.packetId?.let { id ->
					when (qos) {
						1 -> session?.send(MqttPacket(MqttPacketType.PUBACK, id))
						2 -> session?.send(MqttPacket(MqttPacketType.PUBREC, id))
						else -> { /* QoS 0 requires zero protocol feedback tracking packets */ }
					}
				}
				listeners.forEach { it.onMessageArrived(topic, payload, qos, retain, duplicate) }
			}
			MqttPacketType.PUBREC -> {
				packet.packetId?.let { id ->
					session?.send(MqttPacket(MqttPacketType.PUBREL, id))
				}
			}
			MqttPacketType.PUBREL -> {
				packet.packetId?.let { id ->
					session?.send(MqttPacket(MqttPacketType.PUBCOMP, id))
				}
			}
			else -> { /* SUBACK and UNSUBACK maps are automatically reaped directly inside MqttSession */ }
		}
	}
	/**
	 * Loops through unacknowledged elements and re-transmits them with the duplicate flag active.
	 */
	private fun flushInflightPackets() {
		val currentSession = session ?: return
		persistentInflightPackets.values.forEach { pendingPacket ->
			val duplicatePacket = if (pendingPacket.type == MqttPacketType.PUBLISH) {
				MqttPacket(
					topic = pendingPacket.topic!!,
					payload = pendingPacket.payload,
					qos = (pendingPacket.flags ushr 1) and 0x03,
					retain = (pendingPacket.flags and 0x01) == 1,
					packetId = pendingPacket.packetId!!,
					duplicate = true	// Flagged retransmission trace ONLY for PUBLISH packets
				)
			} else pendingPacket	// If it's a PUBREL, SUBSCRIBE, etc., send it exactly as it was logged
			currentSession.send(duplicatePacket)
		}
	}
	private fun handleDisconnect(failedSession: MqttSession, ex: Exception?) {
		// Drop caching only if the invoking failure block belongs to  the active pipeline instance
		if (this.session === failedSession) {
			this.session = null
			this.isHandshakeComplete = false
			listeners.forEach { it.onConnectionLost(ex) }
		}
	}
}
