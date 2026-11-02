/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F2\u06F3
 */

package mqttcore

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.EOFException

object MqttPayload {
	/*
	 * Packs ONLY the CONNECT payload strings sequentially.
	 * Empty strings represent optional fields that should be omitted.
	 */
	fun packConnect(clientId: String, username: String = "", password: Any = ByteArray(0), willTopic: String = "", willMessage: Any = ByteArray(0)): ByteArray {
		val payloadStream = ByteArrayOutputStream()
		// 1. Client ID (Always mandatory)
		MqttPacket.writeString(payloadStream, clientId)
		// 2 & 3. Will Fields (Packed only if a topic is specified)
		if (willTopic.isNotEmpty()) {
			MqttPacket.writeString(payloadStream, willTopic)
			MqttPacket.writeBytes(payloadStream, willMessage)
		}
		// 4. Username (Independent structural string field)
		if (username.isNotEmpty()) {
			MqttPacket.writeString(payloadStream, username)
		}
		//5. Password (Independent structural binary payload field)
		val hasPassword = when (password) {
			is ByteArray -> password.size > 0
			is String -> password.isNotEmpty()
			else -> password.toString().isNotEmpty()
		}
		if (hasPassword) {
			MqttPacket.writeBytes(payloadStream, password)
		}
		return payloadStream.toByteArray()
	}
	/**
	 * Unpacks ONLY the payload byte chunk bases on explicit Connect Flags.
	 * Returns a strictly read-only Map interface to preserve data immutability
	 */
	fun unpackConnect(payload: ByteArray, connectFlags: Int): Map<String, Any> {
		val payloadStream = ByteArrayInputStream(payload)
		val properties = mutableMapOf<String, Any>()
		val hasUsername = (connectFlags and 0x80) != 0
		val hasPassword = (connectFlags and 0x40) != 0
		val hasWill     = (connectFlags and 0x04) != 0
		try {
			// 1. Client ID
			properties["clientId"] = MqttPacket.readString(payloadStream, "Failed to read Client ID")
			// 2 & 3. Will Fields
			if (hasWill) {
				properties["willTopic"] = MqttPacket.readString(payloadStream, "Failed to read Will Topic")
				properties["willMessage"] = MqttPacket.readBytes(payloadStream, "Failed to read Will Message")
			} else {
				properties["willTopic"] = ""
				properties["willMessage"] = ByteArray(0)
			}
			// 4. Username
			properties["username"] = if (hasUsername) {
				MqttPacket.readString(payloadStream, "Failed to read Username")
			} else ""
			// 5. Password (Extracted as a raw binary slice)
			properties["password"] = if (hasPassword) {
				MqttPacket.readBytes(payloadStream, "Failed to read Password")
			} else ByteArray(0)
		} catch (e: Exception) {
			// Robust fail-safe fallback out of bad network segments
		}
		return properties
	}
	/**
	 * Packs a list of SUBSCRIBE Topic Filters and requested QoS levels into a raw byte array.
	 */
	fun packSubscribe(subscriptions: List<Pair<String, Int>>): ByteArray {
		val payloadStream = ByteArrayOutputStream()
		for ((topicFilter, qos) in subscriptions) {
			MqttPacket.writeString(payloadStream, topicFilter)
			payloadStream.write(qos and 0x03)
		}
		return payloadStream.toByteArray()
	}
	/**
	 * Unpacks a SUBSCRIBE raw byte array back into a structred List of Pairs.
	 */
	fun unpackSubscribe(payload: ByteArray): List<Pair<String, Int>> {
		val list = mutableListOf<Pair<String, Int>>()
		val payloadStream = ByteArrayInputStream(payload)
		try {
			while (payloadStream.available() > 0) {
				val topicFilter = MqttPacket.readString(payloadStream)
				val qosByte = payloadStream.read()
				if (qosByte == -1) throw EOFException("Missing QoS byte configuration field.")
				val qos = qosByte and 0x03
				list.add(Pair(topicFilter, qos))
			}
		} catch (e: Exception) {
			// Keep parsing robust; return whatever was recovered successfully up to the fault point
		}
		return list
	}
	/**
	 * Packs a list of UNSUBSCRIBE Topic Filters into a raw byte array.
	 */
	fun packUnsubscribe(topics: List<String>): ByteArray {
		val payloadStream = ByteArrayOutputStream()
		for (topicFilter in topics) {
			MqttPacket.writeString(payloadStream, topicFilter)
		}
		return payloadStream.toByteArray()
	}
	/*
	 * Unpacks an UNSUBSCRIBE raw byte array back into a structured List of Topic Filter Strings.
	 */
	fun unpackUnsubscribe(payload: ByteArray): List<String> {
		val list = mutableListOf<String>()
		val payloadStream = ByteArrayInputStream(payload)
		try {
			while (payloadStream.available() > 0) {
				val topicFilter = MqttPacket.readString(payloadStream)
				list.add(topicFilter)
			}
		} catch (e: Exception) {
			// Keep parsing robust
		}
		return list
	}
}
