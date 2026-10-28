/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F1\u06F6
 */
package localhost.idroid.salamun

import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.io.BufferedReader
import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import java.net.ServerSocket
import java.net.InetAddress
import java.net.URLDecoder

import android.util.Log
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.os.SystemClock

class DynamicRouterService : Service() {
	companion object {
		private const val TAG = "localhost.idroid.salamun"
	}
	private val bLocalRouter = LocalRouterBinder()
	private val hMain = Handler(Looper.getMainLooper())
	private var tpExecutor: ExecutorService? = null
	private var serverSocket: ServerSocket? = null
	@Volatile private var isEngineRunning = false
	private var routerListener: RouterStatusListener? = null

	interface RouterStatusListener {
		fun onLogReceived(message: String)
		fun onTitleUpdateRequired(newTitle: String)
		fun onEngineStateChanged(running: Boolean)
	}
	inner class LocalRouterBinder : Binder() {
		fun getService(): DynamicRouterService = this@DynamicRouterService
	}
	override fun onBind(intent: Intent?): IBinder = bLocalRouter
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
	fun isEngineActive(): Boolean = isEngineRunning
	fun registerStatusListener(listener: RouterStatusListener?) {
		routerListener = listener
		listener?.onEngineStateChanged(isEngineRunning)
	}
	fun startRouterEngine() {
		if (isEngineRunning) return
		isEngineRunning = true
		notifyStatus(true)
		notifyConsole("[ROUTER CONTEXT] Initializing Bounded Fixed Thread Pool Engine [Size: 4]...")
		tpExecutor = Executors.newFixedThreadPool(4)
		Thread {
			try {
				val backlog = 50
				val bindAddress = InetAddress.getByName("0.0.0.0")
				serverSocket = ServerSocket(8080, backlog, bindAddress)
				notifyConsole("[ROUTER STATUS] Operational routing tables live on port 8080.")
				while (isEngineRunning) {
					val clientSocket = serverSocket?.accept() ?: break
					// Defer parsing blocks seamlessly directly onto Thread Pool worker slots
					tpExecutor?.execute { handleRouteTransaction(clientSocket) }
				}
			} catch (e: Exception) {
				notifyConsole("[ROUTER EXCEPTION] ${e.localizedMessage}")
				isEngineRunning = false
				notifyStatus(false)
			}
		}.start()
	}
	// Helper function to read asset files directly into a clean ByteArray
	private fun readAssetFile(context: Context, fileName: String): ByteArray? {
		return try {
			context.assets.open(fileName).use { inputStream ->
				inputStream.readBytes()
			}
		} catch (e: Exception) {
			Log.d("Router", "Failed to read asset: $fileName", e)
			null
		}
	}
	private fun handleRouteTransaction(socket: Socket) {
		try {
			val inputRaw = socket.getInputStream()
			val outputRaw = socket.getOutputStream()
			var requestLine: String?
			var contentLength = -1
			// Read raw bytes manually to parse headers without over-consuming the stream buffer
			val headerBuilder = StringBuilder()
			while (true) {
				val byteRead = inputRaw.read()
				if (byteRead == -1) break
				headerBuilder.append(byteRead.toChar())
				// Check for the standard Layer 7 empty space boundary (\r\n\r\n)
				if (headerBuilder.endsWith("\r\n\r\n") || headerBuilder.endsWith("\n\n")) {
					break
				}
			}
			val headerLines = headerBuilder.toString().split(Regex("\r?\n"))
			if (headerLines.isEmpty() || headerLines[0].isBlank()) return
			requestLine = headerLines[0]
			// Parse out Content-Length from the lines array
			for (line in headerLines) {
				val lowerLine = line.toLowerCase()
				if (lowerLine.startsWith("content-length:")) {
					contentLength = lowerLine.substring(15).trim().toIntOrNull() ?: -1
				}
			}
			val requestTokens = requestLine.split(" ")
			if (requestTokens.size < 2) return

			val httpMethod = requestTokens[0]
			val routeTarget = requestTokens[1]
			val route = if (routeTarget.isEmpty()) "/" else routeTarget
			notifyConsole("[ROUTER TRAFFIC] Intercepted $httpMethod query targeting path -> $route")
			when (httpMethod) {
				"GET" -> {
					when (route) {
						"/" -> {
							// Load HTML Documentation index page from assets
							val htmlPayload = readAssetFile(this, "api_index.html")
							if (htmlPayload != null) {
								writeResponseFrame(outputRaw, "200 OK", "text/html", htmlPayload)
							} else {
								val internalError = "<h2>500 Internal Error: HTML Index Asset Missing</h2>"
								writeResponseFrame(outputRaw, "500 Internal Server Error", "text/html", internalError.toByteArray(Charsets.UTF_8))
							}
						}
						"/api" -> {
							// Load Machine-Readable JSON file directory index from assets
							val jsonPayload = readAssetFile(this, "endpoints.json")
							if (jsonPayload != null) {
								writeResponseFrame(outputRaw, "200 OK", "application/json", jsonPayload)
							} else {
								val internalError = "{\"error\":\"500 Internal Error: Endpoints Asset Missing\"}"
								writeResponseFrame(outputRaw, "500 Internal Server Error", "application/json", internalError.toByteArray(Charsets.UTF_8))
							}
						}
						"/api/status" -> {
							val jsonResponse = "{\"status\":\"RUNNING\",\"uptime_seconds\":${SystemClock.elapsedRealtime() / 1000}}"
							writeResponseFrame(outputRaw, "200 OK", "application/json", jsonResponse.toByteArray(Charsets.UTF_8))
						}
						"/api/cores" -> {
							// Dynamically pull active runtime core count metric via framework calls
							val activeCores = Runtime.getRuntime().availableProcessors()
							val jsonResponse = "{\"available_cpu_cores\":$activeCores,\"runtime_environment\":\"Android Runtime JVM\"}"
							writeResponseFrame(outputRaw, "200 OK", "application/json", jsonResponse.toByteArray(Charsets.UTF_8))
						}
						else -> sendNotFoundResponse(outputRaw)
					}
				}
				"POST" -> {
					when (route) {
						"/api/config" -> {
							if (contentLength <= 0) {
								val errorHtml = "<h2>400 Bad Request: Missing Required Content-Length Data</h2>"
								writeResponseFrame(outputRaw, "400 Bad Request", "text/html", errorHtml.toByteArray(Charsets.UTF_8))
								return
							}
							val bufferPayload = ByteArray(contentLength)
							var totalBytesRead = 0
							while (totalBytesRead < contentLength) {
								val bytesRead = inputRaw.read(bufferPayload, totalBytesRead, contentLength - totalBytesRead)
								if (bytesRead == -1) break
								totalBytesRead += bytesRead
							}
							val rawBodyString = String(bufferPayload, 0, totalBytesRead, Charsets.UTF_8)
							var targetLabelValue = ""
							val formPairs = rawBodyString.split("&")
							for (pair in formPairs) {
								val tokens = pair.split("=")
								if (tokens.size == 2 && tokens[0] == "server_label") {
									targetLabelValue = URLDecoder.decode(tokens[1], "UTF-8")
								}
							}
							if (targetLabelValue.isNotEmpty()) {
								hMain.post { routerListener?.onTitleUpdateRequired(targetLabelValue) }
								notifyConsole("[ROUTER STATE UPDATE] Mutation commited successfully -> '$targetLabelValue'")
							}
							val successHtml = "<h2>Configuration Saved Successfully. Device Terminal State Mutated.</h2>"
							writeResponseFrame(outputRaw, "200 OK", "text/html", successHtml.toByteArray(Charsets.UTF_8))
						}
						else -> sendNotFoundResponse(outputRaw)
					}
				}
				else -> {
					val methodNotAllowedHtml = "<html><body><h1>405 Method Not Allowed</h1></body></html>"
					writeResponseFrame(outputRaw, "405 Method Not Allowed", "text/html", methodNotAllowedHtml.toByteArray(Charsets.UTF_8))
				}
			}
		} catch (e: Exception) {
			notifyConsole("[TRANSACTION RUNTIME EXCEPTION] ${e.localizedMessage}")
		} finally {
			try { socket.close() } catch (ignored: Exception) {}
		}
	}
	private fun sendNotFoundResponse(outputRaw: OutputStream) {
		var notFoundHtml = "<html><body><h1>404 Endpoint Route Target Not Valid</h1><p>Visit <a href='/'>Root Directory</a> for documentation.</p></body></html>"
		writeResponseFrame(outputRaw, "404 Not Found", "text/html", notFoundHtml.toByteArray(Charsets.UTF_8))
	}
	private fun writeResponseFrame(output: OutputStream, status: String, contentType: String, payload: ByteArray) {
		val writer = output.bufferedWriter(Charsets.UTF_8)
		with(writer) {
			write("HTTP/1.1 $status\r\n")
			write("Content-Type: $contentType; charset=utf-8\r\n")
			write("Content-Length: ${payload.size}\r\n")
			write("Connection: close\r\n")
			write("\r\n")
			flush()
		}
		output.write(payload)
		output.flush()
	}
	fun stopRouterEngine() {
		if (!isEngineRunning) return
		isEngineRunning = false
		try {
			serverSocket?.close()
		} catch (e: Exception) {
			notifyConsole("[SERVER DISPOSAL ERROR] ${e.localizedMessage}")
		}
		tpExecutor?.shutdown()
		serverSocket = null
		tpExecutor = null
		notifyStatus(false)
		notifyConsole("[ROUTER STATUS] Deconfigured. Thread pools killed.")
	}
	private fun notifyConsole(message: String) {
		hMain.post { routerListener?.onLogReceived("$message\n") }
	}
	private fun notifyStatus(running: Boolean) {
		hMain.post { routerListener?.onEngineStateChanged(running) }
	}
	override fun onDestroy() {
		stopRouterEngine()
		super.onDestroy()
	}
}
