/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F1\u06F1
 */
package localhost.idroid.salamun

import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.PrintWriter
import java.net.ServerSocket
import java.net.Socket

import android.util.Log
import android.app.Notification
import android.app.NotificationManager
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
import android.os.IBinder

/**
 * IDroid Project - BackgroundServices Module
 * Challenge 2 (Advanced): Persistent TCP Command Matrix Foreground Hub
 * Isolation Background Engine Layer - Listening Server Variant
 */
class TcpMatrixHubService : Service() {
	companion object {
		private const val TAG = "localhost.idroid.salamun"
		const val ACTION_LOG = "$TAG.ACTION_LOG"
		const val ACTION_TX = "$TAG.ACTION_TX"
		const val ACTION_SYNC = "$TAG.ACTION_SYNC"
	}
	private val sbCache = StringBuilder()
	private var sServer: ServerSocket? = null
	private var sClientNode: Socket? = null
	private var writer: PrintWriter? = null
	@Volatile private var isEngineRunning = false
	private val NOTIFICATION_ID = 2026
	private val CHANNEL_ID = "MATRIX_HUB_SERVICE"

	private val commandReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			when (intent?.action) {
				ACTION_TX -> {
					val cmdText = intent.getStringExtra("KEY_CMD") ?: return
					sendFrame(cmdText)
				}
				ACTION_SYNC -> {
					flushLog()
				}
			}
		}
	}
	override fun onCreate() {
		super.onCreate()
		val filter = IntentFilter().apply {
			addAction(ACTION_TX)
			addAction(ACTION_SYNC)
		}
		registerReceiver(commandReceiver, filter)
	}
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
		// IP parameter is discarded in Server Mode as we bind locally to all interfaces
		val destinationPort = intent?.getIntExtra("KEY_PORT", 9999) ?: 9999
		setupForegroundChannel()
		if (!isEngineRunning) {
			isEngineRunning = true
			appendLog("[SERVICE] Initializing non-evictable terminal server socket...")
			startMatrixServerEngine(destinationPort)
		}
		return START_STICKY
	}
	private fun setupForegroundChannel() {
		var nStatusItem = Notification().apply {
			icon = android.R.drawable.stat_notify_sync
			flags = flags or Notification.FLAG_ONGOING_EVENT
		}
		startForeground(NOTIFICATION_ID, nStatusItem)
	}
	private fun startMatrixServerEngine(port: Int) {
		Thread {
			try {
				appendLog("[SERVICE] Binding structural server context to local port $port...")
				sServer = ServerSocket(port)
				while (isEngineRunning) {
					appendLog("[SERVICE] Awaiting inbound node connection...")
					val acceptedNode = sServer?.accept() ?: break
					// Clean up any stray dangling older notes prior to tracking the new link
					try { sClientNode?.close() } catch (e: Exception) {}
					sClientNode = acceptedNode
					writer = PrintWriter(sClientNode!!.getOutputStream(), true)
					val reader = BufferedReader(InputStreamReader(sClientNode!!.getInputStream()))
					val remoteIp = sClientNode?.inetAddress?.hostAddress
					appendLog("[SERVICE] Target aligned safely! Node authorized from: $remoteIp")
					// Inner streaming ingestion block for the active connected stream
					try {
						while (isEngineRunning) {
							val frameLine = reader.readLine() ?: break
							appendLog("[MATRIX RX] $frameLine")
						}
					} catch (streamEx: Exception) {
						appendLog("[SERVICE] Node communication matrix broken -> ${streamEx.localizedMessage}")
					} finally {
						appendLog("[SERVICE] Dropping node tracking links. Re-entering poll baseline...")
						try { writer?.close() } catch (e: Exception) {}
						try { sClientNode?.close() } catch (e: Exception) {}
						writer = null
						sClientNode = null
					}
				}
			} catch (e: Exception) {
				if (isEngineRunning) {
					appendLog("[CRITICAL ERROR] Main server sequence collapsed -> ${e.localizedMessage}")
				}
			} finally {
				teardownTransport()
			}
		}.start()
	}
	private fun sendFrame(cmd: String) {
		Thread {
			try {
				val activeWriter = writer
				if (activeWriter != null) {
					activeWriter.println(cmd)
					appendLog("[MATRIX TX] $cmd")
				} else {
					appendLog("[ERROR] TX Dropped. No node currently attached to transport matrix.")
				}
			} catch (e: Exception) {
				appendLog("[ERROR] Command delivery vector failed -> ${e.localizedMessage}")
			}
		}.start()
	}
	private fun appendLog(logMessage: String) {
		synchronized(sbCache) {
			sbCache.append("$logMessage\n")
		}
		flushLog()
	}
	private fun flushLog() {
		val intent = Intent(ACTION_LOG).apply {
			putExtra("EXTRA_PAYLOAD", sbCache.toString())
		}
		sendBroadcast(intent)
	}
	private fun teardownTransport() {
		isEngineRunning = false
		try { writer?.close() } catch (e: Exception) {}
		try { sClientNode?.close() } catch (e: Exception) {}
		try { sServer?.close() } catch (e: Exception) {}
		writer = null
		sClientNode = null
		sServer = null
		appendLog("[SERVICE] Matrix server architecture completely dark.")
		stopForeground(true)
		stopSelf()
	}
	override fun onBind(intent: Intent?): IBinder? = null
	override fun onDestroy() {
		super.onDestroy()
		try { unregisterReceiver(commandReceiver) } catch (e: Exception) {}
		teardownTransport()
	}
}
