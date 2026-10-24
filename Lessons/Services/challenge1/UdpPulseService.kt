/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F1\u06F0
 */
package localhost.idroid.salamun

import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

import android.util.Log
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder

/**
 * IDroid Project - BackgroundServices Module
 * Challenge 1 (Easy): Background TCP Pulse Server Solution
 */
class UdpPulseService : Service() {
	private companion object {
		const val TAG = "localhost.idroid.salamun"
	}
	private val bBinder = LocalBinder()
	private var sbLogCache = StringBuilder()
	private var onLogUpdate: ((String) -> Unit)? = null
	private var dsNode: DatagramSocket? = null
	private var isRunning = false

	inner class LocalBinder : Binder() {
		fun getService(): UdpPulseService = this@UdpPulseService
	}

	override fun onBind(intent: Intent?): IBinder {
		return bBinder
	}
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		val port = intent?.getIntExtra("EXTRA_PORT", 8080) ?: 8080
		if (!isRunning) {
			isRunning = true
			appendSvcLog("[SYSTEM] Initializing datagram channel...")
			spawnNetworkSvc(port)
		}
		return START_STICKY
	}
	fun setLogListener(listener: ((String) -> Unit)?) {
		this.onLogUpdate = listener
		// Push initial cache batch synchronously upon connection handle
		listener?.invoke(sbLogCache.toString())
	}
	private fun spawnNetworkSvc(port: Int) {
		Thread {
			try {
				dsNode = DatagramSocket(port)
				appendSvcLog("[STATUS] UDP pipeline actively listening on port $port")
				val buffer = ByteArray(2048)
				while (isRunning) {
					val packet = DatagramPacket(buffer, buffer.size)
					dsNode?.receive(packet)
					val msg = String(packet.data, 0, packet.length).trim()
					appendSvcLog("RX: $msg")
				}
			} catch (e: Exception) {
				if (isRunning) {
					appendSvcLog("[WARNING] Pipeline interface exception: ${e.localizedMessage}")
				}
			} finally {
				teardownTransport()
			}
		}.start()
	}
	fun sendFrame(ip: String, port: Int, msg: String) {
		Thread {
			try {
				val address = InetAddress.getByName(ip)
				val bytes = msg.toByteArray()
				val packet = DatagramPacket(bytes, bytes.size, address, port)
				dsNode?.send(packet)
				appendSvcLog("TX: $msg")
			} catch (e: Exception) {
				appendSvcLog("[ERROR] Data transmission failed: ${e.localizedMessage}")
			}
		}.start()
	}
	private fun appendSvcLog(msg: String) {
		synchronized(sbLogCache) {
			sbLogCache.append("$msg\n")
			onLogUpdate?.invoke(sbLogCache.toString())
		}
	}
	private fun teardownTransport() {
		isRunning = false
		try { dsNode?.close() } catch (e: Exception) {}
		dsNode = null
		appendSvcLog("[SYSTEM] Pipeline transport safely closed.")
	}
	override fun onDestroy() {
		super.onDestroy()
		teardownTransport()
	}
}
