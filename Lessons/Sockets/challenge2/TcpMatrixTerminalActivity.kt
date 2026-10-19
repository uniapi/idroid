/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F7
 */
package localhost.idroid.salamun

import java.net.ServerSocket
import java.net.Socket
import java.io.BufferedReader
import java.io.BufferedWriter

import android.util.Log
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.EditText
import android.widget.Button
import android.graphics.Typeface
import android.graphics.Color

/**
 * IDroid Project - NetworkSockets Module
 * Challenge 2 (Advanced): Bi-Directional TCP Command Matrix Terminal Solution
 *
 * Architectural Constraints Met:
 * 1. 100% Pure Code / Programmatic UI Layout (Zero XML Used)
 * 2. Pure Native Android APIs (Zero Jetpack / AndroidX components)
 * 3. Complete Integration of Concurrency handling to prevent main thread blocking
 * 4. Strict Variable Shorthand Notation Compliance (hMain, llRoot, llConfigRow, etTargetIp, etc.)
 */
class TcpMatrixTerminalActivity : Activity() {
	private companion object {
		const val TAG = "localhost.idroid.salamun"
		const val SERVER_PORT = 8888
	}
	// Main UI Thread Handler Bridge
	private val hMain = Handler(Looper.getMainLooper())
	// Structural View Objects
	private lateinit var etTargetIp: EditText
	private lateinit var btnStartServer: Button
	private lateinit var btnConnectClient: Button
	private lateinit var etMessageInput: EditText
	private lateinit var btnSendMessage: Button
	private lateinit var svTerminalScroll: ScrollView
	private lateinit var tvTerminalLog: TextView
	// Persistent Socket Stream Descriptors
	private var ssListener: ServerSocket? = null
	private var sActiveNode: Socket? = null
	private var brInChannel: BufferedReader? = null
	private var bwOutChannel: BufferedWriter? = null

	@Volatile private var isListening = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val llRoot = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setBackgroundColor(Color.parseColor("#0C0E14"))	// Dark Terminal Background
			setPadding(35, 45, 35, 45)
		}
		val tvHeader = TextView(this).apply {
			text = "BI-DIRECTIONAL TCP COMMAND MATRIX"
			textSize = 13f
			setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
			setTextColor(Color.parseColor("#BD93F9"))		// Soft Cyber Purple
			setPadding(0, 0, 0, 30)
		}
		val llConfigRow = LinearLayout(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 0, 20)
			}
		}
		etTargetIp = EditText(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.4f)
			hint = "Enter Master Node IP Address"
			setText("192.168.1.100")		// Sample Default Reference Address Template
			textSize = 12f
			setTypeface(Typeface.MONOSPACE)
			setHintTextColor(Color.parseColor("#44475A"))
			setTextColor(Color.BLACK)
		}
		btnStartServer = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.3f)
			text = "LISTEN"
			textSize = 10f
			setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
			setTextColor(Color.BLACK)
			setBackgroundColor(Color.parseColor("#FFB86C"))	// Alert Orange Gold
		}
		btnConnectClient = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.3f)
			text = "CONNECT"
			textSize = 10f
			setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
			setTextColor(Color.WHITE)
			setBackgroundColor(Color.parseColor("#82AAFF"))	// Cobalt Blue State
		}
		val llMessagingRow = LinearLayout(this).apply {
			orientation = LinearLayout.HORIZONTAL
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 0, 30)
			}
		}
		etMessageInput = EditText(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.75f)
			hint = "Type raw terminal string data command..."
			textSize = 13f
			setTypeface(Typeface.MONOSPACE)
			setHintTextColor(Color.parseColor("#44475A"))
			setTextColor(Color.BLACK)
		}
		btnSendMessage = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.25f)
			text = "SEND"
			textSize = 11f
			isEnabled = false	// Locked until secure connection handshake resolves
			setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
			setTextColor(Color.BLACK)
			setBackgroundColor(Color.parseColor("#50FA7B"))	// Matrix Green
		}
		svTerminalScroll = ScrollView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f)
			setBackgroundColor(Color.parseColor("#151720"))
			setPadding(20, 20, 20, 20)
		}
		tvTerminalLog = TextView(this).apply {
			text = "MATRIX CONSOLE: Select node routing configuration role.\n"
			textSize = 12f
			setTypeface(Typeface.MONOSPACE)
			setTextColor(Color.parseColor("#F1FA8C"))		// Informational Light Yellow
			setLineSpacing(5f, 1f)
		}
		with(llConfigRow) {
			addView(etTargetIp)
			addView(btnStartServer)
			addView(btnConnectClient)
		}
		with (llMessagingRow) {
			addView(etMessageInput)
			addView(btnSendMessage)
		}
		svTerminalScroll.addView(tvTerminalLog)
		with(llRoot) {
			addView(tvHeader)
			addView(llConfigRow)
			addView(llMessagingRow)
			addView(svTerminalScroll)
		}
		setContentView(llRoot)

		btnStartServer.setOnClickListener { initializeMasterServerPipeline() }
		btnConnectClient.setOnClickListener { connectAsClientTerminalInstance() }
		btnSendMessage.setOnClickListener { transmitTerminalMessageAsync() }
	}
	/**
	 * Configuration A: Spawns the ServerSocket runtime listener loop inside an isolated thread layer.
	 */
	private fun initializeMasterServerPipeline() {
		lockConfigurationControls()
		isListening = true
		tvTerminalLog.append("SYS: Starting ServerSocket loop on port $SERVER_PORT...\n")
		Thread {
			try {
				ssListener = ServerSocket(SERVER_PORT)
				hMain.post { tvTerminalLog.append("SERVER: Active. Awaiting structural connection incoming handshake...\n") }
				val socketHandshake = ssListener?.accept() ?: return@Thread		// Blocking socket initialization call
				hMain.post { tvTerminalLog.append("SERVER: Connection handshaking completed from: ${socketHandshake.inetAddress.hostAddress}\n") }
				setupBiDirectionalStreamChannels(socketHandshake)
			} catch (e: Exception) {
				if (isListening) hMain.post { tvTerminalLog.append("SERVER ERROR: ${e.localizedMessage}\n"); resetConfigurationControls() }
			}
		}.start()
	}
	/**
	 * Configuration B: Connects directly across the Wi-Fi local routing plane to the targeted address string.
	 */
	private fun connectAsClientTerminalInstance() {
		val targetIp = etTargetIp.text.toString().trim()
		if (targetIp.isEmpty()) return
		lockConfigurationControls()
		tvTerminalLog.append("CLIENT: Initiating 3-way handshake to coordinate: $targetIp:$SERVER_PORT\n")
		Thread {
			try {
				val clientSocketConnection = Socket(targetIp, SERVER_PORT)
				hMain.post { tvTerminalLog.append("CLIENT: Node connection handshake established successfully.\n") }
				setupBiDirectionalStreamChannels(clientSocketConnection)
			} catch (e: Exception) {
				hMain.post { tvTerminalLog.append("CLIENT ERROR: Handshake rejected: ${e.localizedMessage}\n"); resetConfigurationControls() }
			}
		}.start()
	}
	/**
	 * Extracts low-level I/O handles from the active stream socket and maps a
	 * dedicated stream monitoring background thread.
	 */
	private fun setupBiDirectionalStreamChannels(socket: Socket) {
		sActiveNode = socket
		Thread {
			try {
				// Bind buffer reader and writer layers explicitly specifying UTF_8
				brInChannel = socket.getInputStream().bufferedReader(Charsets.UTF_8)
				bwOutChannel = socket.getOutputStream().bufferedWriter(Charsets.UTF_8)
				hMain.post { btnSendMessage.isEnabled = true }
				while (sActiveNode?.isConnected == true) {
					// Continuous loop checking. Blocks sequentially awaiting a clear '\n' character break
					val streamLine = brInChannel?.readLine() ?: break	// ReadLine returns null instantly if remote node crashes
					hMain.post {
						tvTerminalLog.append("INCOMING DATA -> $streamLine\n")
						svTerminalScroll.fullScroll(ScrollView.FOCUS_DOWN)
					}
				}
			} catch (e: Exception) {
				// Drop out silently when stream pipeline closures close handles manually
			} finally {
				hMain.post { tvTerminalLog.append("SYS: Connection node terminated.\n"); resetConfigurationControls() }
			}
		}.start()
	}
	/**
	 * Formats input structures and fires character serialization lines out over the outbound data stream channel.
	 */
	private fun transmitTerminalMessageAsync() {
		val rawMessage = etMessageInput.text.toString()
		if (rawMessage.isEmpty()) return
		Thread {
			try {
				bwOutChannel?.write(rawMessage + "\n")	// Append the critical parsing line-delimeter
				bwOutChannel?.flush()		// Flush structural cache elements down the physical socket channel immediately
				hMain.post {
					tvTerminalLog.append("OUTBOUND DATA -> $rawMessage\n")
					etMessageInput.setText("")
					svTerminalScroll.fullScroll(ScrollView.FOCUS_DOWN)
				}
			} catch (e: Exception) {
				hMain.post { tvTerminalLog.append("WRITE ERROR: Message delivery failed: ${e.localizedMessage}\n") }
			}
		}.start()
	}
	private fun lockConfigurationControls() {
		btnStartServer.isEnabled = false
		btnConnectClient.isEnabled = false
		etTargetIp.isEnabled = false
	}
	private fun resetConfigurationControls() {
		btnStartServer.isEnabled = true
		btnConnectClient.isEnabled = true
		etTargetIp.isEnabled = true
		btnSendMessage.isEnabled = false
		closeActiveConnectionHandles()
	}
	private fun closeActiveConnectionHandles() {
		try { brInChannel?.close() } catch (e: Exception) {}
		try { bwOutChannel?.close() } catch (e: Exception) {}
		try { sActiveNode?.close() } catch (e: Exception) {}
		try { ssListener?.close() } catch (e: Exception) {}
		sActiveNode = null
		ssListener = null
	}
	override fun onDestroy() {
		Log.d(TAG, "TcpMatrixTerminalActivity onDestroy")
		super.onDestroy()
		isListening = false
		hMain.removeCallbacksAndMessages(null)
		closeActiveConnectionHandles()
	}
}
