/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F1\u06F7
 */
package localhost.idroid.salamun

import android.util.Log
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.IBinder
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Button
import android.graphics.Typeface

class WebMasterActivity : Activity(), CryptographicHttpsService.HttpsStatusListener {
	companion object {
		private const val TAG = "localhost.idroid.salamun"
	}
	private var sBoundHttps: CryptographicHttpsService? = null
	private var isHttpsBound = false
	private lateinit var llRoot: LinearLayout
	private lateinit var btnToggleHttps: Button
	private lateinit var svTelemetryScroll: ScrollView
	private lateinit var tvTelemetryLog: TextView

	private val scHttpsConnection = object : ServiceConnection {
		override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
			val binder = service as CryptographicHttpsService.LocalHttpsBinder
			sBoundHttps = binder.getService()
			isHttpsBound = true
			sBoundHttps?.registerStatusListener(this@WebMasterActivity)
		}
		override fun onServiceDisconnected(name: ComponentName?) {
			sBoundHttps?.registerStatusListener(null)
			sBoundHttps = null
			isHttpsBound = false
		}
	}
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		llRoot = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setPadding(48, 48, 48, 48)
		}
		val tvHeaderTitle = TextView(this).apply {
			text = "🔒 HTTPS TELEMETRY PANEL"
			textSize = 22f
			setPadding(0, 0, 0, 32)
			typeface = Typeface.DEFAULT_BOLD
		}
		btnToggleHttps = Button(this).apply {
			text = "START HTTPS SERVER"
		}
		svTelemetryScroll = ScrollView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f).apply {
				topMargin = 32
			}
		}
		tvTelemetryLog = TextView(this).apply {
			text = "[SYSTEM] Initializing console pipeline view...\n"
			textSize = 13f
			typeface = Typeface.MONOSPACE
		}
		svTelemetryScroll.addView(tvTelemetryLog)
		with(llRoot) {
			addView(tvHeaderTitle)
			addView(btnToggleHttps)
			addView(svTelemetryScroll)
		}
		setContentView(llRoot)
		// Attach interaction listeners triggering secure service startup loops
		btnToggleHttps.setOnClickListener {
			sBoundHttps?.let { service ->
				if (!service.isEngineActive()) {
					val intent = Intent(this, CryptographicHttpsService::class.java)
					startService(intent)	// Ensures service continues running when unbound
					service.startHttpsEngine()
				} else {
					service.stopHttpsEngine()
				}
			}
		}
	}
	override fun onStart() {
		super.onStart()
		// Bind the contextual application envelope framework to the CryptographicHttpsService
		val intent = Intent(this, CryptographicHttpsService::class.java)
		bindService(intent, scHttpsConnection, Context.BIND_AUTO_CREATE)
	}
	override fun onStop() {
		// Perform clean unbind procedures to prevent background context leaks
		if (isHttpsBound) {
			sBoundHttps?.registerStatusListener(null)
			unbindService(scHttpsConnection)
			isHttpsBound = false
		}
		super.onStop()
	}
	override fun onTelemetryLogReceived(message: String) {
		// Push message tracking details onto tvTelemetryLog, matching trailing down-scrolls
		tvTelemetryLog.append(message)
		svTelemetryScroll.post { svTelemetryScroll.fullScroll(ScrollView.FOCUS_DOWN) }
	}
	override fun onEngineStateChanged(running: Boolean) {
		btnToggleHttps.text = if (running) "STOP HTTPS SERVER" else "START HTTPS SERVER"
	}
}
