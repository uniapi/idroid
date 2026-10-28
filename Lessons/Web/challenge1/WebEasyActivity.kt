/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F1\u06F4
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
import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.EditText
import android.widget.Button

class WebEasyActivity : Activity(), MinimalHttpService.ServiceStatusListener {
	companion object {
		private const val TAG = "localhost.idroid.salamun"
	}
	private var sBoundService: MinimalHttpService? = null
	private var isServiceBound = false
	private lateinit var llRoot: LinearLayout
	private lateinit var btnToggleServer: Button
	private lateinit var svConsole: ScrollView
	private lateinit var tvLog: TextView

	private val scConnection = object : ServiceConnection {
		override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
			val binder = service as MinimalHttpService.LocalBinder
			sBoundService = binder.getService()
			isServiceBound = true
			sBoundService?.setStatusListener(this@WebEasyActivity)
		}
		override fun onServiceDisconnected(name: ComponentName?) {
			sBoundService?.setStatusListener(null)
			sBoundService = null
			isServiceBound = false
		}
	}
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		initUi()
		setContentView(llRoot)

		btnToggleServer.setOnClickListener {
			sBoundService?.let { service ->
				if (!service.isEngineActive()) {
					// Trigger dynamic persistent initialization intent command line sequence
					val intent = Intent(this, MinimalHttpService::class.java)
					startService(intent)
					service.startServerEngine()
				} else {
					service.stopServerEngine()
				}
			}
		}
	}
	override fun onStart() {
		super.onStart()
		// Anchor structural service connection matrix context to background frame
		val intent = Intent(this, MinimalHttpService::class.java)
		bindService(intent, scConnection, Context.BIND_AUTO_CREATE)
	}
	override fun onStop() {
		if (isServiceBound) {
			sBoundService?.setStatusListener(null)
			unbindService(scConnection)
			isServiceBound = false
		}
		super.onStop()
	}
	override fun onLogUpdate(message: String) {
		tvLog.append(message)
		svConsole.post { svConsole.fullScroll(ScrollView.FOCUS_DOWN) }
	}
	override fun onStatusChanged(running: Boolean) {
		if (running) {
			btnToggleServer.text = "STOP HTTP SERVER"
		} else {
			btnToggleServer.text = "START HTTP SERVER"
		}
	}
	private fun initUi() {
		llRoot = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setPadding(16, 16, 16, 16)
		}
		btnToggleServer = Button(this).apply {
			text = "START HTTP SERVER"
		}
		svConsole = ScrollView(this)
		tvLog = TextView(this).apply {
			text = "[SYSTEM CONTEXT] Service handshake pending initialization...\n"
		}
		svConsole.addView(tvLog)
		with (llRoot) {
			addView(btnToggleServer)
			addView(svConsole)
		}
	}

}
