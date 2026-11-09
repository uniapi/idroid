/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F1\u06F8
 */
package localhost.idroid.salamun

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket

import android.util.Log
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.EditText
import android.widget.Button

class MqttEasyActivity : Activity() {
	private companion object {
		const val TAG = "localhost.idroid.salamun"
	}
	private val hMain = Handler(Looper.getMainLooper())
	private var tcpSocket: Socket? = null
	private var isConnectedState = false

	private lateinit var llRoot: LinearLayout
	private lateinit var etBrokerAddress: EditText
	private lateinit var btnConnectBroker: Button
	private lateinit var btnClearLog: Button
	private lateinit var svTelemetryScroll: ScrollView
	private lateinit var tvTelemetryLog: TextView


	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val llRoot = LinearLayout(this).apply {
			layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
			orientation = LinearLayout.VERTICAL
			setPadding(32, 32, 32, 32)
		}
		etBrokerAddress = EditText(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			hint = "broker.hivemq.com"
			setText("broker.hivemq.com")
		}
		btnConnectBroker = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			text = "Connect to Broker"
		}
		btnClearLog = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			text = "Clear Log"
			textSize = 10f
			setOnClickListener { tvTelemetryLog.text = "" }
		}
		svTelemetryScroll = ScrollView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f)
		}
		tvTelemetryLog = TextView(this).apply {
			layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			text = "--- SYSTEM READY ---\n"
			textSize = 14f
		}
		svTelemetryScroll.addView(tvTelemetryLog)
		with(llRoot) {
			addView(etBrokerAddress)
			addView(btnConnectBroker)
			addView(btnClearLog)
			addView(svTelemetryScroll)
		}
		setContentView(llRoot)

		btnConnectBroker.setOnClickListener {
			val brokerAddress = etBrokerAddress.text.toString().trim()
			if (brokerAddress.isEmpty()) return@setOnClickListener
			if (!isConnectedState) {
				btnConnectBroker.isEnabled = false
				executeHandshakePipeline(brokerAddress)
			} else {
				notifyTelemetry("[MQTT CONTEXT] Intentionally disconnecting socket channel...")
				safelyTeardownChannels()
			}
		}
	}
	private fun executeHandshakePipeline(brokerAddress: String) {
		notifyTelemetry("[MQTT CONTEXT] Dispatching network connection request to $brokerAddress...")
		Thread {
			try {
				// Initialize raw connection via unencrypted port 1883
				val socket = Socket(brokerAddress, 1883)
				tcpSocket = socket
				val outputStream = socket.getOutputStream()
				val inputStream = socket.getInputStream()
				// Invoke manual packet serialization and push over wire
				serialiseAndPushConnectPacket(outputStream)
				// Invoke blocking capture and verification framework
				captureAndVerifyConnack(inputStream)
				hMain.post {
					isConnectedState = true
					btnConnectBroker.text = "Disconnect"
					btnConnectBroker.isEnabled = true
					etBrokerAddress.isEnabled = false
				}
			} catch (e: Exception) {
				notifyTelemetry("[MQTT EXCEPTION] ${e.localizedMessage}")
				hMain.post { btnConnectBroker.isEnabled = true }
				safelyTeardownChannels()
			}
		}.start()
	}
	private fun serialiseAndPushConnectPacket(outputStream: OutputStream) {
		// Construct standard MQTT v3.1.1 CONNECT packet manually
		// Fixed Header, Variable Header and ClientID payload matching exactly 18 bytes total size
		val connectPacket = byteArrayOf(
			0x10,					// Fixed Header: Packet Type (CONNECT)
			0x10,					// Remaining Length: 16 bytes follow
			0x00, 0x04,				// Protocol Name Length: (4)
			0x4D, 0x51, 0x54, 0x54,	// Protocol Name: "MQTT"
			0x04,					// Protocol Level: v3.1.1
			0x02,					// Connect Flags: Clean Session = 1 (active)
			0x00, 0x3C,				// Keep-Alive: 60 Seconds
			0x00, 0x04,				// Payload Client ID String Length: (4)
			0x54, 0x45, 0x53, 0x54	// Payload Client ID String: "TEST"
		)
		outputStream.write(connectPacket)
		outputStream.flush()
		notifyTelemetry("[MQTT TRAFFIC] CONNECT control packet (0x10) pushed out.")
	}
	private fun captureAndVerifyConnack(inputStream: InputStream) {
		val bufferResponse = ByteArray(4)
		var totalBytesRead = 0
		// Explicitly read exactly 4 bytes out of the pipeline channel array
		while (totalBytesRead < 4) {
			val bytesRead = inputStream.read(bufferResponse, totalBytesRead, 4 - totalBytesRead)
			if (bytesRead == -1) throw IOException("Stream terminated prematurely by broker context.")
			totalBytesRead += bytesRead
		}
		// Verify incoming CONNACK packet markers
		val isConnackType = bufferResponse[0] == 0x20.toByte()
		val isConnectionAccepted = bufferResponse[3] == 0x00.toByte()
		notifyTelemetry("[MQTT ENGINE] Received raw bytes: [${bufferResponse.joinToString { "0x%02X".format(it) }}]")
		if (isConnackType && isConnectionAccepted) {
			notifyTelemetry("[MQTT HANDSHAKE] CONNACK verified. Connection Accepted.")
		} else {
			notifyTelemetry("[MQTT HANDSHAKE FAILURE] Type signature ${"%02X".format(bufferResponse[0])} with Return Code ${"%02X".format(bufferResponse[3])}.")
			safelyTeardownChannels()
		}
	}
	private fun safelyTeardownChannels() {
		try {
			tcpSocket?.close()
		} catch (e: Exception) {
			notifyTelemetry("[MQTT TEARDOWN EXCEPTION] ${e.localizedMessage}")
		}
		tcpSocket = null
		hMain.post {
			isConnectedState = false
			btnConnectBroker.text = "Connect to Broker"
			btnConnectBroker.isEnabled = true
			etBrokerAddress.isEnabled = true
		}
		notifyTelemetry("[MQTT STATUS] Disconnected. Socket channel closed.]")
	}
	private fun notifyTelemetry(message: String) {
		hMain.post { tvTelemetryLog.append("$message\n") }
	}
	override fun onDestroy() {
		super.onDestroy()
		safelyTeardownChannels()
	}
}
