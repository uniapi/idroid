/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F2\u06F0
 */
package localhost.idroid.salamun

import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.TimeUnit
import java.util.concurrent.Executors
import java.util.concurrent.ScheduledExecutorService
import java.io.ByteArrayOutputStream
import java.io.DataInputStream
import java.io.InputStream
import java.io.IOException
import java.io.EOFException
import java.net.Socket

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.Handler
import android.os.Looper

class MqttAdvancedService : Service() {
	companion object {
		private const val TAG = "localhost.idroid.salamun"
	}
	private val bLocalAdvanced = LocalAdvancedBinder()
	private val hMain = Handler(Looper.getMainLooper())
	private var seScheduler: ScheduledExecutorService? = null
	private var tcpSocket: Socket? = null
	private var isConnected = false
	private var advancedListener: AdvancedMqttListener? = null
	private val packetIdCounter = AtomicInteger(1)
	// Tracks the background stream processing lifecycle thread
	private var receiverThread: Thread? = null

	interface AdvancedMqttListener {
		fun onLogReceived(message: String)
		fun onStateChanged(connected: Boolean)
		// Invoked natively on the Main thread when inbound application messages land
		fun onMessageReceived(topic: String, payload: String)
	}
	inner class LocalAdvancedBinder : Binder() {
		fun getService(): MqttAdvancedService = this@MqttAdvancedService
	}
	override fun onBind(intent: Intent?): IBinder = bLocalAdvanced
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
	fun isBrokerConnected(): Boolean = isConnected
	fun registerAdvancedListener(listener: AdvancedMqttListener?) {
		advancedListener = listener
		listener?.onStateChanged(isConnected)
	}
	fun startMqttPipeline(brokerAddress: String) {
		if (isConnected) return
		notifyConsole("[MQTT CONTEXT] Initializing TCP pipeline to $brokerAddress...")
		Thread {
			try {
				val socket = Socket(brokerAddress, 1883)
				tcpSocket = socket
				val outputStream = socket.getOutputStream()
				val inputStream = socket.getInputStream()
				// Execute initial low-level manual CONNEC transaction sequence
				val connectPacket = byteArrayOf(
					0x10, 0x10,					// Fixed Header
					0x00, 0x04,					// Protocol Name Length: (4)
					0x4D, 0x51, 0x54, 0x54,		// Protocol Name: ("MQTT")
					0x04,						// Protocol Level (v3.1.1)
					0x02,						// Connect Flags (Clean Session)
					0x00, 0x3C,					// Keep Alive (60 seconds)
					0x00, 0x04,					// Client ID Length: (4)
					0x54, 0x45, 0x53, 0x54		// Client Value: ("TEST")
				)
				outputStream.write(connectPacket)
				outputStream.flush()
				val bufferResponse = ByteArray(4)
				var totalBytesRead = 0
				while (totalBytesRead < 4) {
					val bytesRead = inputStream.read(bufferResponse, totalBytesRead, 4 - totalBytesRead)
					if (bytesRead == -1) throw IOException("Socket pipe severed during handshake verification.")
					totalBytesRead += bytesRead
				}
				if (bufferResponse[0] == 0x20.toByte() && bufferResponse[3] == 0x00.toByte()) {
					isConnected = true
					notifyState(true)
					notifyConsole("[MQTT HANDSHAKE] CONNACK accepted verified parameters.")
					// Kick off the background parsing runner engine loop
					startIncomingNetworkLoop(DataInputStream(inputStream))
					// Instantiate secure recurring scheduled ping heartbeat executor architecture
					seScheduler = Executors.newSingleThreadScheduledExecutor()
					seScheduler?.scheduleAtFixedRate({ sendHeartbeatPing() }, 30, 30, TimeUnit.SECONDS)
				} else {
					notifyConsole("[MQTT HANDSHAKE FAILURE] Protocol verification failure.")
					teardownEngine()
				}
			} catch (e: Exception) {
				notifyConsole("[CONNECTION EXCEPTION] ${e.localizedMessage}")
				teardownEngine()
			}
		}.start()
	}
	/**
	 * Spawns an isolated parsing background thread to sequentially handle raw packet payloads.
	 */
	private fun startIncomingNetworkLoop(dis: DataInputStream) {
		receiverThread = Thread {
			try {
				while (isConnected && !Thread.currentThread().isInterrupted) {
					// Read packet type header byte
					val headerByte = dis.read()
					if (headerByte == -1) break	// EOF reached
					val packetType = (headerByte ushr 4) and 0x0F
					val flags = headerByte and 0x0F
					// Decode the variable length structure layout parameters
					val remainingLength = decodeVariableByteInteger(dis)
					// Branch traffic parsing based on structural category types
					when (packetType) {
						0x03 -> {	// INBOUND PUBLISH PACKET RECEIVED
							parseIncomingPublish(dis, flags, remainingLength)
						}
						0x09 -> {	// SUBACK RECEIVED
							val subackBytes = ByteArray(remainingLength)
							dis.readFully(subackBytes)
							notifyConsole("[INBOUND TRAFFIC] Received SUBACK packet structure framework (${remainingLength} bytes)")
						}
						0x0D -> {	// PINGRESP RECEIVED
							// Consume any remaining bytes if they exist
							if (remainingLength > 0) {
								dis.readFully(ByteArray(remainingLength))
							}
							notifyConsole("[HEARTBEAT LOOP] Direct 2-byte PINGRESP frame cleared lane routes.")
						}
						else -> {	// Skip unhandled structural tracks gracefully
							val skipBytes = ByteArray(remainingLength)
							dis.readFully(skipBytes)
						}
					}
				}
			} catch (e: Exception) {
				if (isConnected) {
					notifyConsole("[RECEIVER LOOP EXCEPTION] ${e.localizedMessage}")
					hMain.post { teardownEngine() }
				}
			}
		}.apply { start() }
	}
	/**
	 * Decodes individual topic message payloads dynamically based on QoS markers.
	 */
	private fun parseIncomingPublish(dis: DataInputStream, flags: Int, remainingLength: Int) {
		var bytesRead = 0
		// Parse Topic Length Prefix (2 bytes)
		val topicLength = dis.readUnsignedShort()
		bytesRead += 2
		// Parse Topic String from Variable Header
		val topicBytes = ByteArray(topicLength)
		dis.readFully(topicBytes)
		bytesRead += topicLength
		val topic = String(topicBytes, Charsets.UTF_8)
		// Parse Packet Identifier if QoS > 0
		var qos = (flags ushr 1) and 0x03
		if (qos > 0) {
			dis.readUnsignedShort()	// Advance past 2-byte Tracking Variable ID
			bytesRead += 2
		}
		// Remaining spatial bounds belong cleanly to payload parameters
		val payloadLength = remainingLength - bytesRead
		val payloadBytes = ByteArray(payloadLength)
		dis.readFully(payloadBytes)
		val payload = String(payloadBytes, Charsets.UTF_8)
		// Direct payload data objects straight up out onto the UI main loop thread
		hMain.post { advancedListener?.onMessageReceived(topic, payload) }
	}
	/**
	 * Decoding framework for dynamic 7-bit continuation layout markers.
	 */
	private fun decodeVariableByteInteger(inputStream: InputStream): Int {
		var multiplier = 1
		var length = 0
		var encodedByte: Int
		var bytesRead = 0
		do {
			encodedByte = inputStream.read()
			if (encodedByte == -1) throw EOFException("End of stream parsing header parameters.")
			length += (encodedByte and 0x7F) * multiplier
			bytesRead++
			if (bytesRead > 4) throw IllegalArgumentException("Malformed remaining length framework.")
			multiplier = multiplier shl 7
		} while (encodedByte and 0x80 != 0)
		return length
	}
	fun subscribeToTopics(topics: Map<String, Int>) {
		if (!isConnected || topics.isEmpty()) return
		Thread {
			try {
				val outputStream = tcpSocket?.getOutputStream() ?: return@Thread
				val baos = ByteArrayOutputStream()
				var variableAndPayloadLength = 2
				val encodedTopics = ArrayList<Pair<ByteArray, Int>>()
				for ((filter, qos) in topics) {
					val filterBytes = filter.toByteArray(Charsets.UTF_8)
					encodedTopics.add(Pair(filterBytes, qos))
					variableAndPayloadLength += 2 + filterBytes.size + 1
				}
				baos.write(0x82)	// SUBSCRIBE Fixed Flag Mask Pattern
				var targetValue = variableAndPayloadLength
				do {
					var encodedByte = targetValue and 0x7F	// % 128
					targetValue = targetValue ushr 0x80		// /= 128
					if (targetValue > 0) encodedByte = encodedByte or 0x80
					baos.write(encodedByte)
				} while (targetValue > 0)
				val currentPacketId = packetIdCounter.getAndIncrement() and 0xFFFF
				baos.write((currentPacketId ushr 8) and 0xFF)
				baos.write(currentPacketId and 0xFF)
				for (pair in encodedTopics) {
					val filterBytes = pair.first
					baos.write((filterBytes.size ushr 8) and 0xFF)
					baos.write(filterBytes.size and 0xFF)
					baos.write(filterBytes)
					baos.write(pair.second and 0xFF)	// QoS
				}
				outputStream.write(baos.toByteArray())
				outputStream.flush()
				notifyConsole("[SUBSCRIBE TRAFFIC] Pushed out structural bytes [Packet ID: $currentPacketId]")
			} catch (e: Exception) {
				notifyConsole("[SUBSCRIBE FAULT] ${e.localizedMessage}")
				teardownEngine()
			}
		}.start()
	}
	fun publishTelemetryMessage(topic: String, payload: String) {
		if (!isConnected) return
		Thread {
			try {
				val outputStream = tcpSocket?.getOutputStream() ?: return@Thread
				val topicBytes = topic.toByteArray(Charsets.UTF_8)
				val payloadBytes = payload.toByteArray(Charsets.UTF_8)
				val variableHeaderLength = 2 + topicBytes.size
				val totalRemainingLength = variableHeaderLength + payloadBytes.size
				val baos = ByteArrayOutputStream()
				baos.write(0x30)
				encodeRemainingLength(baos, totalRemainingLength)
				baos.write((topicBytes.size ushr 8) and 0xFF)
				baos.write(topicBytes.size and 0xFF)
				baos.write(topicBytes)
				baos.write(payloadBytes)
				outputStream.write(baos.toByteArray())
				outputStream.flush()
				notifyConsole("[PUBLISH TRAFFIC] Pushed out structurual bytes [Topic: $topic]")
			} catch (e: Exception) {
				notifyConsole("[PUBLISH FAULT] ${e.localizedMessage}")
				teardownEngine()
			}
		}.start()
	}
	private fun sendHeartbeatPing() {
		if (!isConnected) return
		try {
			val outputStream = tcpSocket?.getOutputStream() ?: return
			outputStream.write(byteArrayOf(0xC0.toByte(), 0x00.toByte()))		// PINGREQ
			outputStream.flush()
			notifyConsole("[HEARTBEAT LOOP] Direct 2-byte PINGREQ frame sent down socket lanes.")
		} catch (e: Exception) {
			notifyConsole("[HEARTBEAT TERMINATION EXCEPTION] ${e.localizedMessage}")
			teardownEngine()
		}
	}
	fun teardownEngine() {
		if (isConnected) {
			try {
				tcpSocket?.getOutputStream()?.let { ostream ->
					ostream.write(byteArrayOf(0xE0.toByte(), 0x00.toByte()))	// DISCONNECT
					ostream.flush()
				}
			} catch (ignored: Exception) {}
		}
		isConnected = false
		try { receiverThread?.interrupt() } catch (ignored: Exception) {}
		try { seScheduler?.shutdownNow() } catch (ignored: Exception) {}
		try { tcpSocket?.close() } catch (ignored: Exception) {}
		receiverThread = null
		seScheduler = null
		tcpSocket = null
		notifyState(false)
		notifyConsole("[MQTT STATUS] Disconnected. Background threads decommissioned.")
	}
	private fun notifyConsole(message: String) {
		hMain.post { advancedListener?.onLogReceived("$message\n") }
	}
	private fun notifyState(connected: Boolean) {
		hMain.post { advancedListener?.onStateChanged(connected) }
	}
	override fun onDestroy() {
		teardownEngine()
		super.onDestroy()
	}
	private fun encodeRemainingLength(baos: ByteArrayOutputStream, remainingLength: Int) {
		var targetValue = remainingLength
		do {
			var encodedByte = targetValue and 0x7F
			targetValue = targetValue and 0x80
			if (targetValue > 0) encodedByte = encodedByte or 0x80
			baos.write(encodedByte)
		} while (targetValue > 0)
	}
}
