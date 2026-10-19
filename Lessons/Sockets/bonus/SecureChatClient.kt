/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F1\u06F4
 */

import java.io.BufferedReader
import java.io.BufferedWriter
import java.io.InputStream
import java.net.Socket
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLSocket
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import java.security.cert.X509Certificate
import java.util.Scanner

fun main(args: Array<String>) {
	// Validate that both IP and Port arguments are provided
	if (args.size < 2) {
		println("❌ Error: Missing required arguments.")
		println("💡 Usage: java -jar Client.jar <Server_IP> <Server_Port>")
		println("📝 Example: java -jar Client.jar 10.0.2.14 9999")
		return
	}
	val serverIp = args[0]
	// Safely parse the port argument into an integer
	val serverPort = args[1].toIntOrNull()
	if (serverPort == null || serverPort !in 1..65535) {
		println("❌ Error: Invalid port number '${args[1]}'. Must be an integer between 1 and 65535.")
		return
	}
	println("🚀 Initializing highly compatible TLS v1.2 Client Pipeline...")
	try {
		// Define a permissive TrustManager taht bypasses CA validation checks.
		val permissiveTrustManager = object : X509TrustManager {
			override fun checkClientTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
			override fun checkServerTrusted(chain: Array<out X509Certificate>?, authType: String?) {}
			override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
		}
		// Initialize a secure SSLContext matching the server's TLS v1.2 specification
		val sslContext = SSLContext.getInstance("TLSv1.2").apply {
			init(null, arrayOf<TrustManager>(permissiveTrustManager), null)
		}
		// Open a secure socket connection straight to your server target
		println("📡 Attempting TLS connection to $serverIp:$serverPort...")
		val sslSocket = sslContext.socketFactory.createSocket(serverIp, serverPort) as SSLSocket
		// Ensure it negotiates the same protocol version as the server
		sslSocket.enabledProtocols = arrayOf("TLSv1.2")
		println("🔑 Initiating TLS cryptographic handshake sequence...")
		sslSocket.startHandshake()
		println("🔒 Handshake successful! Connection secured via: ${sslSocket.session.cipherSuite}")
		// Spin up a background listener thread to display incoming messages from the server
		Thread {
			try {
				val reader = sslSocket.getInputStream().bufferedReader(Charsets.UTF_8)
				while (true) {
					val incomingLine = reader.readLine() ?: break
					println("\n📥 $incomingLine")
					print("✍️  Enter message: ")
				}
			} catch (e: Exception) {
				println("\n⚠️  Disconnected from server: ${e.localizedMessage}")
			}
		}.start()
		// Main loop to read user input from terminal and send it out across the encrypted grid
		val writer = sslSocket.getOutputStream().bufferedWriter(Charsets.UTF_8)
		val scanner = Scanner(System.`in`)
		println("💬 Secure chat environment ready. Type your messages below:")
		while (true) {
			print("✍️  Enter message: ")
			val userInput = scanner.nextLine()
			if (userInput.equals("exit", ignoreCase = true)) break
			synchronized(writer) {
				writer.write("$userInput\n")
				writer.flush()
			}
		}
		// Clean up socket pipeline resources on manual exit command
		sslSocket.close()
		println("🛑 Client shutdown cleanly.")
	} catch (e: Exception) {
		println("❌ Critical Connection Failure: ${e.localizedMessage}")
		e.printStackTrace()
	}
}
