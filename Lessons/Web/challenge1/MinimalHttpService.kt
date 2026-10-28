/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F1\u06F4
 */
package localhost.idroid.salamun

import java.util.concurrent.TimeUnit
import java.io.BufferedReader
import java.io.BufferedWriter
import java.net.Socket
import java.net.ServerSocket
import java.net.InetAddress

import android.util.Log
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.Binder
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.os.SystemClock

class MinimalHttpService : Service() {
	private companion object {
		const val TAG = "localhost.idroid.salamun"
	}
	private val bLocal = LocalBinder()
	private val hMain = Handler(Looper.getMainLooper())
	private var serverSocket: ServerSocket? = null
	@Volatile private var isServerRunning = false
	private var serviceListener: ServiceStatusListener? = null

	interface ServiceStatusListener {
		fun onLogUpdate(message: String)
		fun onStatusChanged(running: Boolean)
	}
	inner class LocalBinder : Binder() {
		fun getService(): MinimalHttpService = this@MinimalHttpService
	}
	override fun onBind(intent: Intent?): IBinder = bLocal
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		// Enforce sticky lifecycle optimization matrix so service auto-reboots if pushed out by memory contention
		return START_STICKY
	}
	fun setStatusListener(listener: ServiceStatusListener?) {
		serviceListener = listener
		// Instantly synchronize current operational matrix execution bounds to the newly attached client surface
		listener?.onStatusChanged(isServerRunning)
	}
	fun isEngineActive(): Boolean = isServerRunning

	fun startServerEngine() {
		if (isServerRunning) return
		isServerRunning = true
		notifyStatusChanged(true)
		notifyLog("[SERVER CONTEXT] Spawning port 8080 background execution thread...")
		Thread {
			try {
				val backlog = 50
				val bindAddress = InetAddress.getByName("0.0.0.0")
				serverSocket = ServerSocket(8080, backlog, bindAddress)
				notifyLog("[SERVER STATUS] Live and listening on all interfaces at port 8080")
				while (isServerRunning) {
					val clientSocket = serverSocket?.accept() ?: break
					// Instantly offload transaction context to prevent thread block stalls on acceptance lane
					Thread { handleClientTransaction(clientSocket) }.start()
				}
			} catch (e: Exception) {
				notifyLog("[SERVER EXCEPTION] ${e.localizedMessage}")
				isServerRunning = false
				notifyStatusChanged(false)
			}
		}.start()
	}
	fun stopServerEngine() {
		if (!isServerRunning) return
		isServerRunning = false
		try {
			serverSocket?.close()
		} catch (e: Exception) {
			notifyLog("[SERVER TEARDOWN EXCEPTION] ${e.localizedMessage}")
		}
		serverSocket = null
		notifyStatusChanged(false)
		notifyLog("[SERVER STATUS] Terminated. Socket unbound.")
	}
	private fun handleClientTransaction(socket: Socket) {
		try {
			var reader = socket.getInputStream().bufferedReader(Charsets.UTF_8)
			var requestLine: String? = null
			var currentLine: String?
			// Parse HTTP text layer boundaries up to emtpy frame carriage validation index
			while (true) {
				currentLine = reader.readLine()
				if (currentLine == null || currentLine.isEmpty()) break
				if (requestLine == null) {
					requestLine = currentLine
				}
			}
			if (requestLine != null && requestLine.startsWith("GET")) {
				val uptimeMillis = SystemClock.elapsedRealtime()
				val uptimeHours = TimeUnit.MILLISECONDS.toHours(uptimeMillis)
				val uptimeMinutes = TimeUnit.MILLISECONDS.toMinutes(uptimeMillis) % 60
				val uptimeSeconds = TimeUnit.MILLISECONDS.toSeconds(uptimeMillis) % 60
				val htmlPayload = """
					<!DOCTYPE html>
					<html>
		            <head>
        		        <meta charset="UTF-8">
                		<title>IDroid Matrix Status Hub</title>
                		<style>
                		    body { font-family: monospace; background-color: #0f141c; color: #00ff66; padding: 30px; }
                		    h1 { color: #ffffff; border-bottom: 2px solid #00ff66; padding-bottom: 10px; }
               		     	.metric-box { background: #1a2332; padding: 15px; border-radius: 5px; margin-top: 15px; border: 1px solid #33ff77; }
                		    .label { color: #8892b0; font-weight: bold; }
                    		.value { color: #ffffff; }
                		</style>
            		</head>
		            <body>
		                <h1>🌐 IDroid Layer 7 Telemetry Monitor</h1>
		                <p>Status: <span style="color: #00ff66; font-weight: bold;">ONLINE</span></p>

		                <div class="metric-box">
		                    <h3>📱 Device Fingerprint</h3>
		                    <p><span class="label">Hardware Model:</span> <span class="value">${Build.MODEL}</span></p>
		                    <p><span class="label">OS Version:</span> <span class="value">Android ${Build.VERSION.RELEASE} (API ${Build.VERSION.SDK_INT})</span></p>
		                    <p><span class="label">Device Signature:</span> <span class="value">${Build.FINGERPRINT}</span></p>
		                </div>
		                <div class="metric-box">
		                    <h3>⏱️ System Engine Performance</h3>
		                    <p><span class="label">Kernel Uptime:</span> <span class="value">${uptimeHours}h ${uptimeMinutes}m ${uptimeSeconds}s</span></p>
		                    <p><span class="label">Available Processors:</span> <span class="value">${Runtime.getRuntime().availableProcessors()} cores</span></p>
		                </div>
		            </body>
					</html>
				""".trimIndent()

				val rawBytes = htmlPayload.toByteArray(Charsets.UTF_8)
				val writer = socket.getOutputStream().bufferedWriter(Charsets.UTF_8)
				// Write compliant baseline layer 7 framing components directly to hardware output pipe
				with(writer) {
					write("HTTP/1.1 200 OK\r\n")
					write("Content-Type: text/html; charset=utf-8\r\n")
					write("Content-Length: ${rawBytes.size}\r\n")
					write("Connection: close\r\n")
					write("\r\n")
					flush()
				}
				socket.getOutputStream().write(rawBytes)
				socket.getOutputStream().flush()
				notifyLog("[TRANSACTION HANDLED] 200 OK -> ${socket.remoteSocketAddress}")
			}
		} catch (e: Exception) {
			notifyLog("[CLIENT DATA EXCEPTION] ${e.localizedMessage}")
		} finally {
			try { socket.close() } catch (e: Exception) {}
		}
	}
	private fun notifyLog(message: String) {
		hMain.post { serviceListener?.onLogUpdate("$message\n") }
	}
	private fun notifyStatusChanged(running: Boolean) {
		hMain.post { serviceListener?.onStatusChanged(running) }
	}
	override fun onDestroy() {
		stopServerEngine()
		super.onDestroy()
	}
}
