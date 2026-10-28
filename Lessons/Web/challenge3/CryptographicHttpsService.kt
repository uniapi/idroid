/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F1\u06F7
 */
package localhost.idroid.salamun

import java.util.concurrent.Executors
import java.util.concurrent.ExecutorService
import java.io.BufferedWriter
import java.io.InputStream
import java.io.OutputStream
import java.io.OutputStreamWriter
import java.net.Socket
import java.net.ServerSocket
import java.net.InetAddress
import java.net.URLDecoder
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLServerSocket
import javax.net.ssl.SSLSocket

import android.util.Log
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.os.Handler
import android.os.Looper

class CryptographicHttpsService : Service() {
	companion object {
		private const val TAG = "localhost.idroid.salamun"
	}
	private val bLocalHttps = LocalHttpsBinder()
	private val hMain = Handler(Looper.getMainLooper())
	private var tpExecutor: ExecutorService? = null
	private var secureServerSocket: ServerSocket? = null
	@Volatile private var isHttpsAlive = false
	@Volatile private var cachedHtmlTemplate: String? = null
	private var httpsListener: HttpsStatusListener? = null

	interface HttpsStatusListener {
		fun onTelemetryLogReceived(message: String)
		fun onEngineStateChanged(running: Boolean)
	}
	inner class LocalHttpsBinder : Binder() {
		fun getService(): CryptographicHttpsService = this@CryptographicHttpsService
	}
	override fun onBind(intent: Intent?): IBinder = bLocalHttps
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY
	fun isEngineActive(): Boolean = isHttpsAlive
	fun registerStatusListener(listener: HttpsStatusListener?) {
		httpsListener = listener
		listener?.onEngineStateChanged(isHttpsAlive)
	}
	fun startHttpsEngine() {
		if (isHttpsAlive) return
		isHttpsAlive = true
		notifyState(true)
		notifyTelemetry("[HTTPS CONTEXT] Initializing secure network infrastructure...")
		tpExecutor = Executors.newFixedThreadPool(4)
		Thread {
			try {
				val inputKeystore: InputStream = assets.open("idroid_keystore.p12")
				val identityStore = KeyStore.getInstance("PKCS12")
				val passwordChars = "salamun_secret_pass".toCharArray()
				identityStore.load(inputKeystore, passwordChars)
				inputKeystore.close()
				val kmfFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
				kmfFactory.init(identityStore, passwordChars)
				val cryptographicContext = SSLContext.getInstance("TLSv1.2")
				cryptographicContext.init(kmfFactory.keyManagers, null, null)
				val ssfFactory = cryptographicContext.serverSocketFactory
				secureServerSocket = ssfFactory.createServerSocket(8443) as SSLServerSocket
				notifyTelemetry("[HTTPS STATUS] Secure assymetric listener live on port 8443.")
				while (isHttpsAlive) {
					val secureClient = secureServerSocket?.accept() as? SSLSocket ?: break
					tpExecutor?.execute { handleSecureTransaction(secureClient) }
				}
			} catch (e: Exception) {
				notifyTelemetry("[HTTPS INITIALIZATION FAILURE] ${e.localizedMessage}")
				isHttpsAlive = false
				notifyState(false)
			}
		}.start()
	}
	private fun getOrLoadTemplate(fileName: String): String? {
		// Return immediately if the asset is already stored in active RAM memory
		cachedHtmlTemplate?.let { return it }
		// Thread-synchronized fallback block to load the file on the first request
		synchronized(this) {
			// Double-check pattern ensures concurrent threads don't read disk simultaneously
			cachedHtmlTemplate?.let { return it }
			return try {
				assets.open(fileName).use { inputStream ->
					val freshTemplate = inputStream.bufferedReader(Charsets.UTF_8).readText()
					cachedHtmlTemplate = freshTemplate	// Cache it for all subsequent requests
					notifyTelemetry("[SYSTEM CACHE] Asset '$fileName' successfully locked into RAM.")
					freshTemplate
				}
			} catch (e: Exception) {
				notifyTelemetry("❌ ASSET INITIALIZATION ERROR: Failed to read $fileName -> ${e.localizedMessage}")
				null
			}
		}
	}
	private fun handleSecureTransaction(socket: SSLSocket) {
		try {
			// Force the TLS handshake sequence upfront
			socket.startHandshake()
			val inputSecure = socket.inputStream
			val outputRaw = socket.outputStream
			// Maintain an accelerated 2KB local decryption window
			val scanBuffer = ByteArray(2048)
			var bufferLength = 0
			var boundaryIndex = -1
			// Scan decrypted text streams for the standard Layer 7 boundary
			while (boundaryIndex == -1) {
				val bytesRemaining = scanBuffer.size - bufferLength
				if (bytesRemaining <= 0) {
					return	// Out of bounds safety threshold met
				}
				// Decrypts chunk off the network interface into our scanBuffer
				val readCount = inputSecure.read(scanBuffer, bufferLength, bytesRemaining)
				if (readCount == -1) break
				bufferLength += readCount
				val scanStart = (bufferLength - readCount - 3).coerceAtLeast(0)
				for (i in scanStart until (bufferLength - 1)) {
					if (i <= bufferLength - 4 &&
						scanBuffer[i] == '\r'.toByte() && scanBuffer[i + 1] == '\n'.toByte() &&
						scanBuffer[i + 2] == '\r'.toByte() && scanBuffer[i + 3] == '\n'.toByte()) {
						boundaryIndex = i
						break
					} else if (scanBuffer[i] == '\n'.toByte() && scanBuffer[i + 1] == '\n'.toByte()) {
						boundaryIndex = i
						break
					}
				}
			}
			if (boundaryIndex == -1) return
			val isL7Standard = scanBuffer[boundaryIndex] == '\r'.toByte()
			val headerEndOffset = boundaryIndex + if (isL7Standard) 4 else 2
			// Extract explicit routing frames from the secure stream
			val headerString = String(scanBuffer, 0, boundaryIndex, Charsets.UTF_8)
			val headerLines = headerString.split(Regex("\r?\n"))
			if (headerLines.isEmpty() || headerLines[0].isBlank()) return
			val requestLine = headerLines[0]
			var contentLength = -1
			for (line in headerLines) {
				val lowerLine = line.toLowerCase()
				if (lowerLine.startsWith("content-length:")) {
					contentLength = lowerLine.substring(15).trim().toIntOrNull() ?: -1
				}
			}
			val currentCipher = socket.session.cipherSuite
			val currentProtocol = socket.session.protocol
			notifyTelemetry("[HTTPS TRAFFIC] Handshake verified ($currentProtocol -> $currentCipher)")
			notifyTelemetry("[HTTPS ROUTE] Intercepted Request -> $requestLine")
			// Securely capture body payload if present (Supports complex POST over TLS)
			var rawBodyString = ""
			if (contentLength > 0) {
				val bodyBuffer = ByteArray(contentLength)
				val preReadBodyBytesCount = bufferLength - headerEndOffset
				if (preReadBodyBytesCount > 0) {
					System.arraycopy(scanBuffer, headerEndOffset, bodyBuffer, 0, preReadBodyBytesCount.coerceAtMost(contentLength))
				}
				var totalBytesRead = preReadBodyBytesCount.coerceAtMost(contentLength)
				while (totalBytesRead < contentLength) {
					val bytesRead = inputSecure.read(bodyBuffer, totalBytesRead, contentLength - totalBytesRead)
					if (bytesRead == -1) break
					totalBytesRead += bytesRead
				}
				rawBodyString = String(bodyBuffer, 0, totalBytesRead, Charsets.UTF_8)
			}
			// Build telemetry response page layout
			val rawTemplate = getOrLoadTemplate("secure_console.html")
			if (rawTemplate != null) {
				val finalHtml = rawTemplate
					.replace("{{PROTOCOL}}", currentProtocol)
					.replace("{{CIPHER}}", currentCipher)
					.replace("{{REQUEST_LINE}}", requestLine)
					.replace("{{PAYLOAD}}", if (rawBodyString.isEmpty()) "[Empty]" else rawBodyString)
				val rawBytes = finalHtml.toByteArray(Charsets.UTF_8)
				sendHttpResponse(outputRaw, "200 OK", "text/html", rawBytes)
			} else {
				// Fail-safe frame if asset goes missing
				val internalError = "<h2>500 Internal Error: Dynamic Console Template Missing</h2>"
				sendHttpResponse(outputRaw, "500 Internal Server Error", "text/html", internalError.toByteArray(Charsets.UTF_8))
			}
		} catch (e: Exception) {
			notifyTelemetry("[CRYPTOGRAPHIC HANDSHAKE ERROR] ${e.localizedMessage}")
		} finally {
			try { socket.close() } catch (ignored: Exception) {}
		}
	}
	private fun sendHttpResponse(output: OutputStream, status: String, contentType: String, payload: ByteArray) {
		val writer = output.bufferedWriter(Charsets.UTF_8)	// BufferedWriter(OutputStreamWriter(output, Charsets.UTF_8))
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
	fun stopHttpsEngine() {
		if (!isHttpsAlive) return
		isHttpsAlive = false
		try {
			secureServerSocket?.close()
		} catch (e: Exception) {
			notifyTelemetry("[SECURE SOCKET CLOSURE EXCEPTION] ${e.localizedMessage}")
		}
		tpExecutor?.shutdown()
		secureServerSocket = null
		tpExecutor = null
		notifyState(false)
		notifyTelemetry("[HTTPS STATUS] Deconfigured. Security channels unbound.")
	}
	private fun notifyTelemetry(message: String) {
		hMain.post { httpsListener?.onTelemetryLogReceived("$message\n") }
	}
	private fun notifyState(running: Boolean) {
		hMain.post { httpsListener?.onEngineStateChanged(running) }
	}
	override fun onDestroy() {
		stopHttpsEngine()
		super.onDestroy()
	}
}
