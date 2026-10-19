/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F8
 */
package localhost.idroid.salamun

import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLServerSocket
import java.util.Date
import java.util.concurrent.CopyOnWriteArrayList
import java.util.concurrent.atomic.AtomicInteger
import java.net.Socket
import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.security.KeyStore
import java.text.SimpleDateFormat

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
 * Challenge 3 (Master): Multi-Client TLS Secure Chat Server & Frame Multiplexer
 *
 * =====================================================================================
 * 🛠️ HOW TO GENERATE THE COMPANION KEYSTORE (Run in your development machine terminal):
 * =====================================================================================
 * keytool -genkeypair \
 *   -alias idroid_server \
 *   -keyalg RSA \
 *   -keysize 2048 \
 *   -validity 365 \
 *   -keystore idroid_keystore.p12 \
 *   -storetype PKCS12 \
 *   -storepass salamun_secret_pass \
 *   -dname "CN=localhost, OU=IDroid, O=uniapi, L=Brest, C=BY"
 *
 * After generation, push this 'idroid_keystore.p12' file straight into your app's
 * sandboxed files directory path via Device File Explorer or adb command line utility:
 * adb push idroid_keystore.p12 /data/data/localhost.idroid.salamun/files/
 * or supply with your app as assets if no root access
 * =====================================================================================
 *
 * Architectural Constraints Met:
 * 1. 100% Pure Code Layout Structure (Zero XML Used)
 * 2. Pure Native Android APIs (Zero Jetpack / AndroidX components)
 * 3. Native Cryptographic TLS v1.3 SSLServerSocket Connection Handshake Loops
 * 4. Concurrent Thread-Safe Broadcaster Multiplexing Topology
 * 5. Strict Variable Shorthand Notation Compliance (hMain, llRoot, btnToggle, tvActiveConnections, svTerminalScroll, tvTerminalLog)
 */
class SecureChatServerActivity : Activity() {
	private companion object {
		const val TAG = "localhost.idroid.salamun"
		const val SECURE_PORT = 9999
		const val KEYSTORE_NAME = "idroid_keystore.p12"
		const val KEYSTORE_PASS = "salamun_secret_pass"
	}
	// Main UI Thread Handler Bridge
	private val hMain = Handler(Looper.getMainLooper())
	// Thread-Safe Storage Containers to multiplex client connections without state race conditions
	private val listSecureClients = CopyOnWriteArrayList<Socket>()
	private val listClientWriters = CopyOnWriteArrayList<BufferedWriter>()
	private val atomicActiveCount = AtomicInteger(0)
	// Programmatic UI view layouts
	private lateinit var btnToggle: Button
	private lateinit var tvActiveConnections: TextView
	private lateinit var svTerminalScroll: ScrollView
	private lateinit var tvTerminalLog: TextView
	// Secure Network Primitives
	private var sssSecureListener: SSLServerSocket? = null
	@Volatile private var isServerRunning = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val llRoot = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setBackgroundColor(Color.parseColor("#0D0E15"))	// Dark Cipher Obsidian Black
			setPadding(35, 45, 35, 45)
		}
		val tvTitle = TextView(this).apply {
			gravity = Gravity.CENTER_HORIZONTAL
			text = "TLS SECURE MULTI-CLIENT CHAT SERVER"
			textSize = 13f
			setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
			setTextColor(Color.parseColor("#FF79C6"))		// Reactive Cyber Pink Accent
			setPadding(0, 0, 0, 30)
		}
		tvActiveConnections = TextView(this).apply {
			gravity = Gravity.CENTER_HORIZONTAL
			text = "SECURE ACTIVE NODES: 0"
			textSize = 12f
			setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
			setTextColor(Color.parseColor("#50FA7B"))		// Matrix Green Metric Label
			setPadding(0, 0, 0, 30)
		}
		btnToggle = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 0, 40)
			}
			text = "INITIALIZE SECURE SERVER ENGINE"
			textSize = 13f
			setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
			setTextColor(Color.BLACK)
			setBackgroundColor(Color.parseColor("#82AAFF"))	// Cobalt System Blue
		}
		svTerminalScroll = ScrollView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f)
			setBackgroundColor(Color.parseColor("#141622"))
			setPadding(20, 20, 20, 20)
		}
		tvTerminalLog = TextView(this).apply {
			text = "CONSOLE: Secure layer dormant. Load your sandboxed PKCS12 keystore file to fire loop handlers...\n"
			textSize = 11f
			setTypeface(Typeface.MONOSPACE)
			setTextColor(Color.parseColor("#8BE9FD"))		// Telemetry Cyan
			setLineSpacing(5f, 1f)
		}
		svTerminalScroll.addView(tvTerminalLog)
		with(llRoot) {
			addView(tvTitle)
			addView(tvActiveConnections)
			addView(btnToggle)
			addView(svTerminalScroll)
		}
		setContentView(llRoot)

		btnToggle.setOnClickListener {
			if (!isServerRunning) {
				igniteSecureServerPipeline()
			} else {
				shutdownSecureServerEngine()
			}
		}
	}
	/**
	 * Loads the sandboxed local PKCS12 keystore binary file, boots up a native SSLContext
	 * configured for strict TLS v1.3 encryption, and allocates the ServerSocket listener pool loop.
	 */
	private fun igniteSecureServerPipeline() {
/*		val fileKeystore = File(filesDir, KEYSTORE_NAME)
		if (!fileKeystore.exists()) {
			appendConsoleLog("ABORT", "Missing keystore certificate file link inside sandboxed root path directory.")
			appendConsoleLog("HELP", "Please run 'keytool' terminal script and push file straight to: $filesDir")
			return
		}
*/		isServerRunning = true
		btnToggle.text = "TERMINATE CRYPTOGRAPHIC INSTANCE"
		btnToggle.setBackgroundColor(Color.parseColor("#FF5555"))	// Emergency Red State
		appendConsoleLog("INIT", "Booting up asymmetric key manager factories layers...")
		Thread {
			var assetStream: InputStream? = null
			try {
				// Access AssetManager via context to fetch the packed certificate keystore
				assetStream = assets.open(KEYSTORE_NAME)
				// Initialize local keystore database interface
				val ksInstance = KeyStore.getInstance("PKCS12")
				val passPasswordChars = KEYSTORE_PASS.toCharArray()
				ksInstance.load(assetStream, passPasswordChars)
/*				FileInputStream(fileKeystore).use { fis ->
					ksInstance.load(fis, passPasswordChars)
				}
*/				// Initialize private keys authentication managers factory
				val kmfInstance = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm()).apply {
					init(ksInstance, passPasswordChars)
				}
				// Instantiate low-level cryptographic Context engine configured for TLS v1.2
				val sslContextInstance = SSLContext.getInstance("TLSv1.2").apply {
					init(kmfInstance.keyManagers, null, null)
				}
				// Fire the secure server listener wrapper handle on port 9999
				sssSecureListener = sslContextInstance.serverSocketFactory.createServerSocket(SECURE_PORT) as SSLServerSocket
				hMain.post { appendConsoleLog("SECURE_OK", "SSLServerSocket successfully bound to port $SECURE_PORT using TLS v1.2.") }
				while (isServerRunning) {
					// Continuous loop listening. Blocks until incoming secure client performs a valid TLS handshake
					val sClientHandshake = sssSecureListener?.accept() ?: break
					// Route active secure client nodes straight to multiplex loop tracking threads
					Thread {
						handleSecureClientPipeline(sClientHandshake)
					}.start()
				}
			} catch (e: Exception) {
				if (isServerRunning) {
					hMain.post { appendConsoleLog("CRIT_ERR", "Secure framework infrastructure breakdown: ${e.localizedMessage}") }
				}
			} finally {
				try { assetStream?.close() } catch (e: Exception) {}
				hMain.post { shutdownSecureServerEngine() }
			}
		}.start()
	}
	/**
	 * Allocates buffers, signs up outbound streams to the master broadcast list, and holds an infinite
	 * character parsing reader check loop on the incoming client stream channel.
	 */
	private fun handleSecureClientPipeline(socket: Socket) {
		val clientAddress = socket.inetAddress.hostAddress
		var brInChannel: BufferedReader? = null
		var bwOutChannel: BufferedWriter? = null
		try {
			brInChannel = socket.getInputStream().bufferedReader(Charsets.UTF_8)
			bwOutChannel = socket.getOutputStream().bufferedWriter(Charsets.UTF_8)
			// Append descriptors to thread-safe collections for global message broadcast routing
			listSecureClients.add(socket)
			listClientWriters.add(bwOutChannel)
			val currentTotal = atomicActiveCount.incrementAndGet()
			hMain.post {
				tvActiveConnections.text = "SECURE ACTIVE NODES: $currentTotal"
				appendConsoleLog("JOIN", "Node connected safely from address: $clientAddress")
			}
			while (isServerRunning && socket.isConnected) {
				// Continuous stream check block. Blocks sequentially waiting for characters ending in standard '\n' limits
				val incomingLine = brInChannel.readLine() ?: break	// Returns null instantly if remote node crashes
				hMain.post { appendConsoleLog("DATA_RX", "[$clientAddress]: $incomingLine") }
				// Multiplex payload frame cleanly to every other client active on the network grid
				multiplexMessageFrameBroadcast(clientAddress, incomingLine, bwOutChannel)
			}
		} catch (e: Exception) {
			// Drop out cleanly when socket disruptions trigger exception loops
		} finally {
			// Dynamic node reclamation sequence
			listSecureClients.remove(socket)
			bwOutChannel?.let { listClientWriters.remove(it) }
			try { brInChannel?.close() } catch (e: Exception) {}
			try { bwOutChannel?.close() } catch (e: Exception) {}
			try { socket.close() } catch (e: Exception) {}
			val finalTotal = atomicActiveCount.decrementAndGet()
			hMain.post {
				tvActiveConnections.text = "SECURE ACTIVE NODES: $finalTotal"
				appendConsoleLog("QUIT", "Cleaned up and disconnected link container for: $clientAddress")
			}
		}
	}
	/**
	 * Iterates through thread-safe writer streams to transmit input lines to all other active connected targets.
	 */
	private fun multiplexMessageFrameBroadcast(senderAddress: String, text: String, currentWriter: BufferedWriter) {
		val outboundPayloadBlock = "[$senderAddress]:$text\n"
		for (writer in listClientWriters) {
			if (writer == currentWriter) continue	// Skip the client node that originally sent the message payload
			Thread {
				try {
					synchronized(writer) {
						writer.write(outboundPayloadBlock)
						writer.flush()	// Direct network clear push
					}
				} catch (e: Exception) {
					// Failures are automatically caught and discarded; individual client connection tears clean cleanly inside main loop
				}
			}.start()
		}
	}
	/**
	 * Clears arrays, destroys sockets, and stops background loop pumping processes securely.
	 */
	private fun shutdownSecureServerEngine() {
		if (!isServerRunning) return
		isServerRunning = false
		btnToggle.text = "INITIALIZE SECURE SERVER ENGINE"
		btnToggle.setBackgroundColor(Color.parseColor("#82AAFF"))
		tvActiveConnections.text = "SECURE ACTIVE NODES: 0"
		atomicActiveCount.set(0)
		appendConsoleLog("SHUTDOWN", "Teardown command detected. Purging active sockets allocations...")
		Thread {
			// Close active client arrays handles
			for (socket in listSecureClients) {
				try { socket.close() } catch (e: Exception) {}
			}
			listSecureClients.clear()
			listClientWriters.clear()
			// Break main ServerSocket blocking capture states loop
			try {
				sssSecureListener?.close()
				sssSecureListener = null
			} catch (e: Exception) {}
			hMain.post { appendConsoleLog("SYS", "Security socket grid offline. Encryption ports wiped cleanly.") }
		}.start()
	}
	/**
	 * Appends real-time structural data monitoring text strings right onto the scrolling console canvas views.
	 */
	private fun appendConsoleLog(tag: String, text: String) {
		val timeStamp = SimpleDateFormat("HH:mm:ss.SSS").format(Date())
		tvTerminalLog.append("[$timeStamp][$tag] $text\n")
		svConsoleScrollDown()
	}
	private fun svConsoleScrollDown() {
		svTerminalScroll.fullScroll(ScrollView.FOCUS_DOWN)
	}
	override fun onDestroy() {
		Log.d(TAG, "SecureChatServerActivity onDestroy")
		super.onDestroy()
		shutdownSecureServerEngine()
		hMain.removeCallbacksAndMessages(null)
	}
}
