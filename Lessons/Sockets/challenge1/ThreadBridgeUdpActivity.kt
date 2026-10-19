/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F4
 */
package localhost.idroid.salamun

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

import android.util.Log
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Gravity
import android.view.MotionEvent
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import android.widget.FrameLayout
import android.widget.TextView
import android.widget.Button
import android.graphics.Typeface
import android.graphics.Color

/**
 * IDroid Project - NetworkSockets Module
 * Challenge 1 (Easy): Connectionless UDP Telemetry Pumper Solution
 *
 * Architectural Constraints Met:
 * 1. 100% Pure Code / Programmatic UI Layout (Zero XML Used)
 * 2. Pure Native Android APIs (Zero Jetpack / AndroidX components)
 * 3. Asynchronous Non-Blocking Datagram Transmission Loops
 * 4. Strict Variable Shorthand Notation Compliance (hMain, llRoot, flTouchPad, tvTelemetryConsole)
 */
class ThreadBridgeUdpActivity : Activity() {
	private companion object {
		const val TAG = "localhost.idroid.salamun"
		const val UDP_PORT = 9999
		const val LOOPBACK_IP = "127.0.0.1"
		const val BUFFER_SIZE = 256
	}
	// Thread-Bridge tied directly to the main execution loop
	private val hMain = Handler(Looper.getMainLooper())
	// Programmatic UI view references
	private lateinit var flTouchPad: FrameLayout
	private lateinit var tvTelemetryConsole: TextView
	// Sockets and Thread lifecycle state controls
	private var dsReceiver: DatagramSocket? = null
	@Volatile private var isReceiverRunning = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val llRoot = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setBackgroundColor(Color.parseColor("#1E1E24"))	// Charcoal Gray Palette
			setPadding(30, 40, 30, 40)
		}
		val tvHeader = TextView(this).apply {
			gravity = Gravity.CENTER_HORIZONTAL
			text = "UDP LOOPBACK TELEMETRY MONITOR"
			textSize = 14f
			setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
			setTextColor(Color.parseColor("#50FA7B"))		// Matrix Green Accent
			setPadding(0, 0, 0, 30)
		}
		flTouchPad = FrameLayout(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 0.6f).apply {
				setMargins(0, 0, 0, 30)
			}
			setBackgroundColor(Color.parseColor("#282A36"))	// Deep Dark Purple Slate
		}
		val tvPadLabel = TextView(this).apply {
			layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
			gravity = Gravity.CENTER
			text = "DRAG FINGER HERE TO PUMP DATAGRAMS"
			textSize = 12f
			setTypeface(Typeface.MONOSPACE)
			setTextColor(Color.parseColor("#6272A4"))		// Muted Slate
		}
		tvTelemetryConsole = TextView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 0.4f)
			text = "TELEMETRY BUS: Initializing low-level background threads...\n"
			textSize = 12f
			setTypeface(Typeface.MONOSPACE)
			setTextColor(Color.parseColor("#8BE9FD"))		// Telemetry Cyan
			setBackgroundColor(Color.parseColor("#111216"))
			setPadding(20, 20, 20, 20)
		}
		flTouchPad.addView(tvPadLabel)
		with(llRoot) {
			addView(tvHeader)
			addView(flTouchPad)
			addView(tvTelemetryConsole)
		}
		setContentView(llRoot)
		// Establish Low-Level Background UDP Listener Loop
		startUdpReceiverPipeline()
		// Bind Touch Motion Listeners to the Datagram Emitter Thread
		flTouchPad.setOnTouchListener { _, event ->
			if (event.action == MotionEvent.ACTION_MOVE) {
				val coordinatesPayload = "X:${event.x.toInt()},Y:${event.y.toInt()}"
				transmitDatagramPacketAsync(coordinatesPayload)
			}
			true
		}
	}
	/**
	 * Initializes a persistent background Linux socket that loops infinitely to process incoming
	 * UDP datagrams without causing UI thread dropped frames.
	 */
	private fun startUdpReceiverPipeline() {
		isReceiverRunning = true
		Thread {
			try {
				dsReceiver = DatagramSocket(UDP_PORT)
				val byteBuffer = ByteArray(BUFFER_SIZE)
				hMain.post { tvTelemetryConsole.append("STATUS: Bound to loopback interface on port $UDP_PORT.\n") }
				while (isReceiverRunning) {
					val incomingPacket = DatagramPacket(byteBuffer, byteBuffer.size)
					// Native low-level blocking primitive call. Awaits incoming network frames.
					dsReceiver?.receive(incomingPacket)
					val parsedString = String(
						incomingPacket.data,
						incomingPacket.offset,
						incomingPacket.length,
						Charsets.UTF_8
					)
					hMain.post {
						tvTelemetryConsole.text = "LIVE STREAM RX -> TARGET: $LOOPBACK_IP \nDATAGRAM BLOCK: $parsedString\n"
					}
				}
			} catch (e: Exception) {
				if (isReceiverRunning) {
					hMain.post { tvTelemetryConsole.append("ERROR: Receiver crashed: ${e.localizedMessage}\n") }
				}
			}
		}.start()
	}
	/**
	 * Allocates an independent transient worker thread to send connectionless datagram frames to the loopback layer.
	 */
	private fun transmitDatagramPacketAsync(payload: String) {
		Thread {
			try {
				val clientSocket = DatagramSocket()
				val byteData = payload.toByteArray(Charsets.UTF_8)
				val ipAddress = InetAddress.getByName(LOOPBACK_IP)
				val outboundPacket = DatagramPacket(byteData, byteData.size, ipAddress, UDP_PORT)
				clientSocket.send(outboundPacket)
				clientSocket.close()	// Instantly release transient local port descriptor back to system
			} catch (e: Exception) {
				// Silently swallow packet errors to protect high-frequency real-time touch rendering smoothness
			}
		}.start()
	}
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putString("KEY_CONSOLE_LOG", tvTelemetryConsole.text.toString())
	}
	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		super.onRestoreInstanceState(savedInstanceState)
		savedInstanceState.getString("KEY_CONSOLE_LOG")?.let { logs ->
			tvTelemetryConsole.text = logs
		}
	}
	override fun onDestroy() {
		Log.d(TAG, "ThreadBridgeUdpActivity: onDestroy")
		super.onDestroy()
		isReceiverRunning = false
		hMain.removeCallbacksAndMessages(null)
		dsReceiver?.close()		// Forcibly breaks the blocking kernel .receive()
	}
}
