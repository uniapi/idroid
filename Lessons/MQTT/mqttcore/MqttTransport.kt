/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F2\u06F3
 */

package mqttcore

import java.io.InputStream
import java.io.OutputStream
import java.net.Socket
import javax.net.ssl.SSLSocket

interface MqttTransport {
	val inputStream: InputStream
	val outputStream: OutputStream
	fun close()
}

class TextTransport(private val socket: Socket) : MqttTransport {
	override val inputStream: InputStream get() = socket.getInputStream()
	override val outputStream: OutputStream get() = socket.getOutputStream()
	override fun close() = socket.close()
}

class SecureTransport(private val sslSocket: SSLSocket) : MqttTransport {
	override val inputStream: InputStream get() = sslSocket.getInputStream()
	override val outputStream: OutputStream get() = sslSocket.getOutputStream()
	override fun close() = sslSocket.close()
}
