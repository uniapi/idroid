/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F1\u06F3
 */
package localhost.idroid.salamun

import java.util.concurrent.CopyOnWriteArrayList
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.Socket
import java.security.KeyStore
import javax.net.ssl.KeyManagerFactory
import javax.net.ssl.SSLContext
import javax.net.ssl.SSLServerSocket

import android.util.Log
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.IBinder
import android.graphics.Color

/**
 * IDroid Project - BackgroundServices Module
 * Challenge 3 (Master): Multi-Client TLS Cryptographic Multiplexer Engine Solution
 * Isolation Background Engine Layer
 *
 * Architectural Constraints Met:
 * 1. 100% Pure Code / Programmatic UI Layout (Zero XML Used)
 * 2. Pure Native Android APIs (Zero Jetpack / AndroidX components)
 * 3. Thread-Safe Concurrent Transport Frame Multiplexing Architecture
 * 4. Custom Local Security KeyStore Parsing Engine Lifecycle Context
 * 5. Strict Variable Shorthand Notation Compliance (sssSecureListener, sClientNode, etc.)
 * 6. 100% Native API Level 10 Compile-safe Architecture Profiles
 */
class TlsMuxEngineService : Service() {
	companion object {
		private const val TAG = "localhost.idroid.salamun"
		const val ACTION_MUX_TELEMETRY = "$TAG.ACTION_MUX_TELEMETRY"
		const val ACTION_MUX_SYNC = "$TAG.ACTION_MUX_SYNC"
	}
	private val sbMuxCache = StringBuilder()
	private val clientNodePool = CopyOnWriteArrayList<Socket>()
	private var sssSecureListener: SSLServerSocket? = null
	@Volatile private var isMuxActive = false
	private val NOTIFICATION_ID = 3033
	private val CHANNEL_ID = "TLS_MUX_ENGINE_CHANNEL"

	private val syncReceiver = object: BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			when (intent?.action) {
				ACTION_MUX_SYNC -> flushTelemetry()
			}
		}
	}
	override fun onCreate() {
		super.onCreate()
		registerReceiver(syncReceiver, IntentFilter(ACTION_MUX_SYNC))
	}
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		Log.d(TAG, "onStartCommand")
		val listenPort = intent?.getIntExtra("KEY_LISTEN_PORT", 8443) ?: 8443
		setupForegroundChannel()
		if (!isMuxActive) {
			isMuxActive = true
			appendMuxLog("[SECURE] Initializing multi-channel cryptographic multiplexer matrix...")
			bootSecureServer(listenPort)
		}
		return START_STICKY
	}
	private fun setupForegroundChannel() {
		Log.d(TAG, "setupForegroundChannel")
		//val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
		var nServiceItem = Notification()
		nServiceItem.icon = android.R.drawable.ic_lock_lock
		nServiceItem.flags = nServiceItem.flags or Notification.FLAG_ONGOING_EVENT
		startForeground(NOTIFICATION_ID, nServiceItem)
	}
	private fun bootSecureServer(port: Int) {
		Log.d(TAG, "bootSecureServer")
		Thread{
			try {
				val protocolVersion = "TLSv1.2"
				val sslContext = SSLContext.getInstance(protocolVersion)
				val keyStore = KeyStore.getInstance("PKCS12")
				assets.open("idroid_keystore.p12").use { inputStream ->
					keyStore.load(inputStream, "salamun_secret_pass".toCharArray())
				}
				val kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm())
				kmf.init(keyStore, "matrix_key_pass".toCharArray())
				sslContext.init(kmf.keyManagers, null, null)
				sssSecureListener = sslContext.serverSocketFactory.createServerSocket(port) as SSLServerSocket
				appendMuxLog("[SECURE] Cryptographic listener locked on interface port $port using $protocolVersion.")
				while (isMuxActive) {
					val sClientNode = sssSecureListener!!.accept()
					appendMuxLog("[CONNECTED] Client node associated from -> ${sClientNode.remoteSocketAddress}")
					clientNodePool.add(sClientNode)
					flushTelemetry()
					// Delegate child context stream thread
					spawnNodeHandler(sClientNode)
				}
			} catch (e: Exception) {
				appendMuxLog("[CRITICAL] Server socket loop drop exception -> ${e.localizedMessage}")
			} finally {
				teardownEngine()
			}
		}.start()
	}
	private fun spawnNodeHandler(sClientNode: Socket) {
		Thread {
			try {
				val reader= BufferedReader(InputStreamReader(sClientNode.getInputStream()))
				val nodeIp = sClientNode.remoteSocketAddress.toString()
				while (isMuxActive) {
					val receivedFrame = reader.readLine() ?: break
					appendMuxLog("[$nodeIp]: $receivedFrame")
					// Multiplex incoming payload frames to all concurrent active terminal nodes
					broadcastToPool(nodeIp, receivedFrame)
				}
			} catch (e: Exception) {
				// Muted node drop log anomalies
			} finally {
				clientNodePool.remove(sClientNode)
				appendMuxLog("[DISCONNECTED] Client node disconnected from transport channel frame.")
				flushTelemetry()
				try { sClientNode.close() } catch (e: Exception) {}
			}
		}.start()
	}
	private fun broadcastToPool(senderIp: String, frame: String) {
		Thread {
			for (node in clientNodePool) {
				try {
					if (node.remoteSocketAddress.toString() != senderIp && !node.isClosed) {
						val writer = PrintWriter(node.getOutputStream(), true)
						writer.println("[$senderIp] $frame")
					}
				} catch (e: Exception) {
					// Safe cleanup handled by isolated child loop channels
				}
			}
		}.start()
	}
	private fun appendMuxLog(msg: String) {
		synchronized(sbMuxCache) {
			sbMuxCache.append("$msg\n")
		}
		flushTelemetry()
	}
	private fun flushTelemetry() {
		val intent = Intent(ACTION_MUX_TELEMETRY).apply {
			putExtra("EXTRA_MUX_LOG", sbMuxCache.toString())
			putExtra("EXTRA_CLIENT_COUNT", clientNodePool.size)
		}
		sendBroadcast(intent)
	}
	private fun teardownEngine() {
		isMuxActive = false
		appendMuxLog("[SYSTEM] Requesting infrastructure teardown sequence...")
		for (node in clientNodePool) {
			try { node.close() } catch (e: Exception) {}
		}
		clientNodePool.clear()
		try { sssSecureListener?.close() } catch (e: Exception) {}
		sssSecureListener = null
		appendMuxLog("[SYSTEM] Cryptographic multiplexer offline.")
		stopForeground(true)
		stopSelf()
	}
	override fun onBind(intent: Intent?): IBinder? = null
	override fun onDestroy() {
		super.onDestroy()
		try { unregisterReceiver(syncReceiver) } catch (e: Exception) {}
		teardownEngine()
	}
}
