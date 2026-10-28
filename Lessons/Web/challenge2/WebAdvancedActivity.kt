/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F1\u06F6
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
import android.widget.Button

class WebAdvancedActivity : Activity(), DynamicRouterService.RouterStatusListener {
	companion object {
		private const val TAG = "localhost.idroid.salamun"
	}
	private var sBoundRouter: DynamicRouterService? = null
	private var isRouterBound = false
	private lateinit var llRoot: LinearLayout
	private lateinit var btnToggleEngine: Button
	private lateinit var tvServerTitle: TextView
	private lateinit var svConsole: ScrollView
	private lateinit var tvConsole: TextView

	private val scRouterConnection = object : ServiceConnection {
		override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
			val binder = service as DynamicRouterService.LocalRouterBinder
			sBoundRouter = binder.getService()
			isRouterBound = true
			sBoundRouter?.registerStatusListener(this@WebAdvancedActivity)
		}
		override fun onServiceDisconnected(name: ComponentName?) {
			sBoundRouter?.registerStatusListener(null)
			sBoundRouter = null
			isRouterBound = false
		}
	}
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		llRoot = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setPadding(16, 16, 16, 16)
		}
		tvServerTitle = TextView(this).apply {
			text = "IDROID APP NODE"
			textSize = 20f
			setPadding(0, 0, 0, 24)
		}
		btnToggleEngine = Button(this).apply {
			text = "START REST ROUTER"
		}
		svConsole = ScrollView(this)
		tvConsole = TextView(this).apply {
			text = "[CONSOLE CONTEXT] Service connection verification pending...\n"
		}
		svConsole.addView(tvConsole)
		with(llRoot) {
			addView(tvServerTitle)
			addView(btnToggleEngine)
			addView(svConsole)
		}
		setContentView(llRoot)

		btnToggleEngine.setOnClickListener {
			sBoundRouter?.let { service ->
				if (!service.isEngineActive()) {
					val intent = Intent(this, DynamicRouterService::class.java)
					startService(intent)
					service.startRouterEngine()
				} else {
					service.stopRouterEngine()
				}
			}
		}
	}
	override fun onStart() {
		super.onStart()
		val intent = Intent(this, DynamicRouterService::class.java)
		bindService(intent, scRouterConnection, Context.BIND_AUTO_CREATE)
	}
	override fun onStop() {
		if (isRouterBound) {
			sBoundRouter?.registerStatusListener(null)
			unbindService(scRouterConnection)
			isRouterBound = false
		}
		super.onStop()
	}
	override fun onLogReceived(message: String) {
		tvConsole.append(message)
		svConsole.post { svConsole.fullScroll(ScrollView.FOCUS_DOWN) }
	}
	override fun onTitleUpdateRequired(newTitle: String) {
		tvServerTitle.text = newTitle
	}
	override fun onEngineStateChanged(running: Boolean) {
		btnToggleEngine.text = if (running) "STOP REST ROUTER" else "START REST ROUTER"
	}
}
