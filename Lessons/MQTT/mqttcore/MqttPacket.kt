/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F2\u06F2
 */

package mqttcore

import java.io.EOFException
import java.io.InputStream
import java.io.OutputStream
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream

enum class MqttPacketType(val value: Int) {
	CONNECT(1), CONNACK(2), PUBLISH(3), PUBACK(4), PUBREC(5), PUBREL(6), PUBCOMP(7),
	SUBSCRIBE(8), SUBACK(9), UNSUBSCRIBE(10), UNSUBACK(11), PINGREQ(12), PINGRESP(13),
	DISCONNECT(14);
	companion object {
		// Used by MqttPacket.read() to quickly identify the enum from raw stream bytes
		fun fromInt(value: Int): MqttPacketType? = values().find { it.value == value }
	}
}

open class MqttPacket private constructor(
	val type: MqttPacketType,
	val flags: Int = 0,
	val packetId: Int? = null,
	val topic: String? = null,
	var payload: ByteArray = byteArrayOf(),
	val properties: Map<String, Any> = emptyMap()	// Holds metadata like keepAlive, returnCode, etc.
) {
	// 1. Empty/Control Packets (PINGREQ, PINGRESP, DISCONNECT)
	constructor(type: MqttPacketType) : this(
		type = type.also {
			require(it == MqttPacketType.PINGREQ || it == MqttPacketType.PINGRESP || it == MqttPacketType.DISCONNECT) {
				"Invalid packet type for an empty control packet: $type"
			}
		},
		flags = 0
	)
	// 2. Basic Acknowledgment/Identifiable Packets (PUBACK, PUBREC, PUBREL, PUBCOMP, UNSUBACK)
	constructor(type: MqttPacketType, packetId: Int) : this(
		type = type.also {
			require(it == MqttPacketType.PUBACK || it == MqttPacketType.PUBREC || it == MqttPacketType.PUBREL ||
					it == MqttPacketType.PUBCOMP || it == MqttPacketType.UNSUBACK) {
				"Invalid packet type for a basic acknowledgment packet: $it"
			}
		},
		packetId = packetId,
		flags = if (type == MqttPacketType.PUBREL) 2 else 0
	)
	// 3. Connect Request Packet (CONNECT)
	constructor(
		payload: ByteArray,
		hasUsername: Boolean = false,
		hasPassword: Boolean = false,
		keepAliveSeconds: Int = 60,
		cleanSession: Boolean = true,
		hasWill: Boolean = false,
		willQos: Int = 0,
		willRetain: Boolean = false
	) : this(
		type = MqttPacketType.CONNECT,
		payload = payload,
		properties = mapOf(
			"keepAlive" to keepAliveSeconds,
			"cleanSession" to cleanSession,
			"hasUsername" to hasUsername,
			"hasPassword" to hasPassword,
			"hasWill" to hasWill,
			"willQos" to willQos,
			"willRetain" to willRetain,
			"connectFlags" to (
				((if (cleanSession) 1 else 0) shl 1) or
				((if (hasWill) 1 else 0) shl 2) or
				((willQos and 0x03) shl 3) or
				((if (willRetain) 1 else 0) shl 5) or
				((if (hasPassword) 1 else 0) shl 6) or
				((if (hasUsername) 1 else 0) shl 7)
			)
		)
	)
	// 4. Connect Acknowledgment Packet (CONNACK)
	constructor(returnCode: Int, sessionPresent: Boolean) : this(
		type = MqttPacketType.CONNACK,
		properties = mapOf(
			"returnCode" to returnCode,
			"sessionPresent" to sessionPresent
		)
	)
	// 5. Publish Message Packet (PUBLISH)
	constructor(topic: String, payload: ByteArray = byteArrayOf(), qos: Int = 0, retain: Boolean = false, packetId: Int? = null, duplicate: Boolean = false) : this(
		type = MqttPacketType.PUBLISH,
		flags = ((if (duplicate) 1 else 0) shl 3) or ((qos and 0x03) shl 1) or (if (retain) 1 else 0),
		packetId = packetId.also {
			if (qos > 0) {
				require(it != null) { "A Packet Identifier (packetId) is strictly mandatory for PUBLISH packets when QoS > 0." }
			} else {
				require(it == null) { "A Packet Identifier (packetId) must be omitted for PUBLISH packets when QoS is 0." }
			}
		},
		topic = topic,
		payload = payload,
		properties = mapOf(
			"duplicate" to duplicate,
			"qos" to qos,
			"retain" to retain
		)
	)
	// 6. Subscription/Unsubscription Request Packet (SUBSCRIBE, UNSUBSCRIBE)
	constructor(
		type: MqttPacketType,
		packetId: Int,			// Strictrly mandatory for tracking acknowledgments
		payload: ByteArray		// Raw serialized topic filter configuration data chunk
	) : this(
		type = type.also {
			require(it == MqttPacketType.SUBSCRIBE || it == MqttPacketType.UNSUBSCRIBE) {
				"Invalid packet type for a subscription request packet: $it"
			}
		},
		packetId = packetId,
		payload = payload,
		flags = 2
	)
	// 7. Subscription Acknowledgment Packet (SUBACK)
	constructor(packetId: Int, grantedQosList: List<Int>) : this(
		type = MqttPacketType.SUBACK,
		packetId = packetId,
		properties = mapOf("grantedQos" to grantedQosList)
	)
	/**
	 * Serializes this packet directly to the output network socket stream
	 */
	fun write(outputStream: OutputStream) {
		outputStream.write((type.value shl 4) or (flags and 0x0F))
		when (type) {
			MqttPacketType.PINGREQ,
			MqttPacketType.PINGRESP,
			MqttPacketType.DISCONNECT -> {
				outputStream.write(0x00)
				outputStream.flush()
				return
			}
		}
		val packetStream = ByteArrayOutputStream()
		when (type) {
			MqttPacketType.PUBACK,
			MqttPacketType.PUBREC,
			MqttPacketType.PUBREL,
			MqttPacketType.PUBCOMP,
			MqttPacketType.UNSUBACK -> {
				writeShort(packetStream, packetId!!)
			}
			MqttPacketType.CONNECT -> {
				// 1. Write Protocol Name Length & String
				writeString(packetStream, "MQTT")
				// 2. Write Protocol Level (4 = MQTT 3.1.1)
				packetStream.write(0x04)
				// 3. Extract and Write Connect Flags
				packetStream.write(properties["connectFlags"] as Int)
				// 4. Write Keep Alive Time
				writeShort(packetStream, properties["keepAlive"] as Int)
				// 5. Write Payload
				packetStream.write(payload)
			}
			MqttPacketType.CONNACK -> {
				val sessionPresent = properties["sessionPresent"] as Boolean
				packetStream.write(if (sessionPresent) 0x01 else 0x00)
				packetStream.write(properties["returnCode"] as Int)
			}
			MqttPacketType.PUBLISH -> {
				// 1. Write the topic string length prefix and bytes
				writeString(packetStream, topic!!)
				// 2. Write the 2-byte packet identifier safely if present (QoS 1 or 2)
				packetId?.let { writeShort(packetStream, it) }
				// 3. Write the raw binary payload array
				packetStream.write(payload)
			}
			MqttPacketType.SUBSCRIBE,
			MqttPacketType.UNSUBSCRIBE -> {
				writeShort(packetStream, packetId!!)
				packetStream.write(payload)
			}
		}
		val bodyBytes = packetStream.toByteArray()
		writeRemainingLength(outputStream, bodyBytes.size)
		if (bodyBytes.isNotEmpty()) outputStream.write(bodyBytes)
		outputStream.flush()
	}
	companion object {
		/**
		 * Blocks and reads from the socket stream to return a complete MqttPacket
		 */
		fun read(inputStream: InputStream): MqttPacket {
			// 1. Read the very first Fixed Header byte (Type & Flags)
			val firstByte = inputStream.read()
			if (firstByte == -1) throw EOFException("Connection closed.")
			val typeValue = (firstByte ushr 4) and 0x0F
			val flags = firstByte and 0x0F
			val packetType = MqttPacketType.fromInt(typeValue)
				?: throw IllegalArgumentException("Unknown packet type: $typeValue")
			// 2. Read the remaining length variable-byte integer 2+
			val remainingLength = readRemainingLength(inputStream)
			// 3. Before proceed, check if PINGREQ, PINGRESP or DISCONNECT
			when (packetType) {
				MqttPacketType.PINGREQ,
				MqttPacketType.PINGRESP,
				MqttPacketType.DISCONNECT -> {
					require(remainingLength == 0) { "Malformed empty packet $packetType. Length must be 0." }
					return MqttPacket(packetType)
				}
			}
			// 4. BATCH READ: Pull the rest of the packet out of the socket instantly
			val packetBuffer = ByteArray(remainingLength)
			var totalBytesRead = 0
			while (totalBytesRead < remainingLength) {
				val bytesRead = inputStream.read(packetBuffer, totalBytesRead, remainingLength - totalBytesRead)
				if (bytesRead == -1) throw EOFException("Stream closed before packet was fully read.")
				totalBytesRead += bytesRead
			}
			// 5. Wrap the batch buffer in a local stream for easy parsing
			val packetStream = ByteArrayInputStream(packetBuffer)
			// 6. Local memory extraction
			return when (packetType) {
				MqttPacketType.PUBACK,
				MqttPacketType.PUBREC,
				MqttPacketType.PUBREL,
				MqttPacketType.PUBCOMP,
				MqttPacketType.UNSUBACK -> {
					// These packets always have a remaining length of exactly 2 bytes (the Packet ID)
					val packetId = readShort(packetStream, "Failed to read packet ID")
					MqttPacket(packetType, packetId)
				}
				MqttPacketType.CONNECT -> {
					// 1. Read Protocol Name (Standard MQTT is 4 bytes: "MQTT")
					val protocolName = readString(packetStream, "Failed to read protocol name")
					require(protocolName == "MQTT") { "Invalid protocol name: $protocolName" }
					// 2. Read Protocol Level (e.g. 4 for MQTT 3.1.1)
					val protocolLevel = packetStream.read()
					// 3. Read Connect Flags
					val connectFlags = packetStream.read()
					val cleanSession = ((connectFlags ushr 1) and 0x01) == 1
					val hasWill = ((connectFlags ushr 2) and 0x01) == 1
					val willQos = ((connectFlags ushr 3) and 0x03)
					val willRetain = ((connectFlags ushr 5) and 0x01) == 1
					val hasPassword = ((connectFlags ushr 6) and 0x01) == 1
					val hasUsername = ((connectFlags ushr 7) and 0x01) == 1
					// 4. Read Keep Alive (2 bytes)
					val keepAlive = readShort(packetStream, "Failed to read keepAlive")
					val payloadSize = packetStream.available()
					val payload = readBytes(packetStream, payloadSize, "Failed to read $packetType payload")
					MqttPacket(payload, hasUsername, hasPassword, keepAlive, cleanSession, hasWill, willQos, willRetain)
				}
				MqttPacketType.CONNACK -> {
					// 1. Read Acknowledge Flags (1st byte contains Session Present flag in bit 0)
					val ackFlags = packetStream.read()
					val sessionPresent = (ackFlags and 0x01) == 1
					// 2. Read Connect Return Code (2nd byte)
					val returnCode = packetStream.read()
					MqttPacket(returnCode, sessionPresent)
				}
				MqttPacketType.PUBLISH -> {
					// 1. Read 2-byte Topic Length and String
					val topic = readString(packetStream, "Failed to read $packetType topic")
					// 2. Read Packet ID if QoS > 0 (checked via flags)
					val qos = (flags ushr 1) and 0x03
					val packetId = if (qos > 0) { readShort(packetStream, "Failed to read $packetType packet ID") } else null
					// 3. The RETAIN and DUP flags
					val retain = (flags and 0x01) == 1
					val duplicate = ((flags ushr 3) and 0x01) == 1
					// 4. The rest of the packet buffer is the exact application payload
					val payloadSize = packetStream.available()
					val payload = readBytes(packetStream, payloadSize, "Failed to read $packetType payload")
					MqttPacket(topic, payload, qos, retain, packetId, duplicate)
				}
				MqttPacketType.SUBSCRIBE,
				MqttPacketType.UNSUBSCRIBE -> {
					// Protocol check: Fixed flags must be exactly 0010 (2)
					require(flags == 2) { "Malformed $packetType packet flags. Bits must be exactly 0010." }
					// 1. Read the 2-byte tracking Packet Identifier
					val packetId = readShort(packetStream, "Failed to read $packetType packet ID")
					// 2. Read the rest of the payload filters into a raw array for the broker to slice up
					val payloadSize = packetStream.available()
					val payload = readBytes(packetStream, payloadSize, "Failed to read $packetType payload")
					MqttPacket(packetType, packetId, payload)
				}
				MqttPacketType.SUBACK -> {
					// 1. Read the 2-byte tracking Packet Identifier
					val packetId = readShort(packetStream, "Failed to read $packetType packet ID")
					val grantedQosList = mutableListOf<Int>()
					while (packetStream.available() > 0) {
						grantedQosList.add(packetStream.read())
					}
					MqttPacket(packetId, grantedQosList)
				}
				else -> throw UnsupportedOperationException("Unknown packet type: $packetType")
			}
		}
		fun readShort(packetStream: ByteArrayInputStream, errorMessage: String = "Unexpected end of stream reading short"): Int {
			val msb = packetStream.read()
			val lsb = packetStream.read()
			if (msb == -1 || lsb == -1) throw EOFException(errorMessage)
			return (msb shl 8) or lsb
		}
		fun readString(packetStream: ByteArrayInputStream, errorMessage: String = "Failed to fully read string data"): String {
			// Automatically extract length from stream
			val length = readShort(packetStream, "$errorMessage (missing length prefix)")
			validateFieldLength(packetStream, length, "$errorMessage (insufficient bytes available)")
			val bytes = ByteArray(length)
			val readBytes = packetStream.read(bytes)
			if (readBytes != length) throw EOFException("$errorMessage (incomplete string segment read)")
			return String(bytes, Charsets.UTF_8)
		}
		fun readBytes(packetStream: ByteArrayInputStream, errorMessage: String = "Failed to fully read binary data"): ByteArray {
			// Automatically extract length from stream
			val length = readShort(packetStream, "$errorMessage (missing length prefix)")
			validateFieldLength(packetStream, length, "$errorMessage (insufficient bytes available)")
			return readBytes(packetStream, length, "$errorMessage")
		}
		private fun readBytes(packetStream: ByteArrayInputStream, length: Int, errorMessage: String = "Failed to fully read binary data"): ByteArray {
			val bytes = ByteArray(length)
			val readBytes = packetStream.read(bytes)
			if (readBytes != length) throw EOFException("$errorMessage (incomplete byte payload read)")
			return bytes
		}
		fun writeShort(packetStream: ByteArrayOutputStream, value: Int) {
			packetStream.write((value ushr 8) and 0xFF);
			packetStream.write(value and 0xFF)
		}
		fun writeString(packetStream: ByteArrayOutputStream, value: String) {
			val bytes = value.toByteArray(Charsets.UTF_8)
			// Ensure the 2-byte MQTT length prefix is always prepended automatically
			writeShort(packetStream, bytes.size)
			packetStream.write(bytes)
		}
		fun writeBytes(packetStream: ByteArrayOutputStream, value: Any) {
			val bytes = when (value) {
				is ByteArray -> value
				is String -> value.toByteArray(Charsets.UTF_8)
				else -> value.toString().toByteArray(Charsets.UTF_8)
			}
			// Ensure the 2-byte MQTT length prefix is always prepended automatically
			writeShort(packetStream, bytes.size)
			packetStream.write(bytes)
		}
		private fun validateFieldLength(packetStream: ByteArrayInputStream, length: Int, errorMessage: String = "Malformed length") {
			if (length < 0 || packetStream.available() < length) throw EOFException(errorMessage)
		}
		private fun writeRemainingLength(outputStream: OutputStream, length: Int) {
			require(length in 0..268435455) { "Malformed remaining legnth: $length" }
			var num = length
			do {
				var encodedByte = num and 0x7F
				num = num ushr 7
				// If there are mote bytes to encode, set the continuation bit (7)
				if (num > 0) {
					encodedByte = encodedByte or 0x80
				}
				outputStream.write(encodedByte)
			} while (num > 0)
		}
		private fun readRemainingLength(inputStream: InputStream): Int {
			var multiplier = 1
			var length = 0
			var encodedByte: Int
			var bytesRead = 0
			do {
				encodedByte = inputStream.read()
				if (encodedByte == -1) throw EOFException("Stream closed before packet was fully read.")
				length += (encodedByte and 0x7F) * multiplier
				bytesRead++
				if (bytesRead > 4) throw IllegalArgumentException("Malformed remaining length")
				multiplier = multiplier shl 7
			} while (encodedByte and 0x80 != 0)
			return length
		}
	}
}
