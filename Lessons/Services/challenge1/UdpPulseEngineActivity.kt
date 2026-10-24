/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F1\u06F0
 */
package localhost.idroid.salamun

import android.util.Log
import android.app.Activity
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.Bundle
import android.os.Binder
import android.os.IBinder
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.View
import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.EditText
import android.widget.TextView
import android.widget.Button
import android.graphics.Color

/**
 * IDroid Project - BackgroundServices Module
 * Challenge 1 (Easy): Background UDP Pulse Engine Solution
 *
 * Architectural Constraints Met:
 * 1. 100% Pure Code / Programmatic UI Layout (Zero XML Used)
 * 2. Pure Native Android APIs (Zero Jetpack / AndroidX components)
 * 3. Complete Integration of Concurrency handling to prevent main thread blocking
 * 4. Strict Variable Shorthand Notation Compliance (hMain, llRoot, sSvc, bBinder, etc.)
 */
class UdpPulseEngineActivity : Activity() {
	private companion object {
		const val TAG = "localhost.idroid.salamun"
	}
	private val hMain = Handler(Looper.getMainLooper())
	// Programmatic UI references
	private lateinit var llRoot: LinearLayout
	private lateinit var llConfigRow: LinearLayout
	private lateinit var etIp: EditText
	private lateinit var etPort: EditText
	private lateinit var etMsg: EditText
	private lateinit var btnBind: Button
	private lateinit var btnUnbind: Button
	private lateinit var btnSend: Button
	private lateinit var svConsole: ScrollView
	private lateinit var tvLog: TextView
	// Service Management states
	private var sSvc: UdpPulseService? = null
	private var isBound = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		initProgrammaticUI()
		setContentView(llRoot)

		btnBind.setOnClickListener {
			val portStr = etPort.text.toString().trim()
			if (portStr.isEmpty()) {
				appendUiLog("[ALERT] Please supply an initialized network port target.")
				return@setOnClickListener
			}
			val port = portStr.toIntOrNull() ?: 8080
			val intent = Intent(this, UdpPulseService::class.java).apply {
				putExtra("EXTRA_PORT", port)
			}
			startService(intent)
			bindService(intent, sConn, Context.BIND_AUTO_CREATE)
		}
		btnUnbind.setOnClickListener {
			if (isBound) {
				sSvc?.setLogListener(null)
				unbindService(sConn)
				isBound = false
				appendUiLog("[SYSTEM] Interface unbound safely.")
			}
			stopService(Intent(this, UdpPulseService::class.java))
			appendUiLog("[SYSTEM] Service engine fully terminated.")
		}
		btnSend.setOnClickListener {
			val ip = etIp.text.toString().trim()
			val portStr = etPort.text.toString().trim()
			val payload = etMsg.text.toString()
			val port = portStr.toIntOrNull() ?: 8080
			if (ip.isEmpty() || payload.isEmpty()) {
				appendUiLog("[ALERT] Destination address parameters or payload data missing.")
				return@setOnClickListener
			}
			if (isBound && sSvc != null) {
				sSvc?.sendFrame(ip, port, payload)
				etMsg.setText("")
			} else {
				appendUiLog("[ERROR] Engine disconnected. Bind the interface before transmission.")
			}
		}
		// Automatic re-bind check if service survived perviously on rotation
		val checkIntent = Intent(this, UdpPulseService::class.java)
		bindService(checkIntent, sConn, 0)
	}
	override fun onDestroy() {
		super.onDestroy()
		if (isBound){
			sSvc?.setLogListener(null)
			unbindService(sConn)
			isBound = false
		}
	}
	private fun initProgrammaticUI() {
		llRoot = LinearLayout(this).apply {
			setGravity(Gravity.TOP)
			orientation = LinearLayout.VERTICAL
			setBackgroundColor(Color.parseColor("#12141A"))	// Outer Application Canvas
			setPadding(32, 32, 32, 32)
		}
		llConfigRow = LinearLayout(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			orientation = LinearLayout.HORIZONTAL
			weightSum = 3.0f
		}
		etIp = EditText(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 2.0f)
			hint = "TARGET IP"
			setHintTextColor(Color.parseColor("#A0A5B5"))
			setTextColor(Color.BLACK)
			inputType = InputType.TYPE_CLASS_TEXT
			setText("10.0.2.14")
		}
		etPort = EditText(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f)
			hint = "PORT"
			setHintTextColor(Color.parseColor("#A0A5B5"))
			setTextColor(Color.BLACK)
			inputType = InputType.TYPE_CLASS_NUMBER
			setText("8080")
		}
		val llActionRow = LinearLayout(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 24, 0, 24)
			}
			orientation = LinearLayout.HORIZONTAL
		}
		btnBind = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f).apply {
				setMargins(0, 0, 12, 0)
			}
			text = "Start Engine"
			textSize = 14f
			setTextColor(Color.WHITE)
			setBackgroundColor(Color.parseColor("#2F66DF"))	// Professional Accent Blue
		}
		btnUnbind = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f)
			text = "Stop Engine"
			textSize = 14f
			setTextColor(Color.WHITE)
			setBackgroundColor(Color.parseColor("#3A3F4D"))	// Muted Control Gray
		}
		val llMessageRow = LinearLayout(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 0, 24)
			}
			orientation = LinearLayout.HORIZONTAL
			setGravity(Gravity.CENTER_VERTICAL)	// Forces both children to align vertically in the center
		}
		etMsg = EditText(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 3.0f)
			hint = "Message Payload Buffer"
			setHintTextColor(Color.parseColor("#A0A5B5"))
			setTextColor(Color.BLACK)
			// Set single line properties to prevent the EditText from expanding vertically
			// setMaxLines(1)
		}
		btnSend = Button(this).apply {
			// Match the height parameter exactly with WRAP_CONTENT and apply a weight of 1
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f).apply {
				gravity = Gravity.CENTER_VERTICAL	// Lock button alignment relative to the row
			}
			text = "Send"
			textSize = 14f
			setTextColor(Color.WHITE)
			setBackgroundColor(Color.parseColor("#2F66DF"))
		}
		svConsole = ScrollView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f)
			setBackgroundColor(Color.parseColor("#1E1E24"))	// Dark Terminal Background
		}
		tvLog = TextView(this).apply {
			textSize = 14f
			setTextColor(Color.parseColor("#FFFFFF"))		// Pure White text for logs
			setPadding(24, 24, 24, 24)
			setLineSpacing(0f, 1.2f)
		}
		with(llConfigRow) {
			addView(etIp)
			addView(etPort)
		}
		with(llActionRow) {
			addView(btnBind)
			addView(btnUnbind)
		}
		svConsole.addView(tvLog)
		with(llMessageRow) {
			addView(etMsg)
			addView(btnSend)
		}
		with(llRoot) {
			addView(llConfigRow)
			addView(llActionRow)
			addView(svConsole)
			addView(llMessageRow)
		}
	}
	private val sConn = object : ServiceConnection {
		override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
			val bBinder = service as UdpPulseService.LocalBinder
			sSvc = bBinder.getService()
			isBound = true
			// Synchronize log cache instantly upon binding connection
			sSvc?.setLogListener { updatedLog ->
				hMain.post {
					tvLog.text = updatedLog
					svConsole.post { svConsole.fullScroll(ScrollView.FOCUS_DOWN) }
				}
			}
			appendUiLog("[SYSTEM} Framework bound to service pipeline successfully.")
		}
		override fun onServiceDisconnected(name: ComponentName?) {
			sSvc?.setLogListener(null)
			sSvc = null
			isBound = false
			appendUiLog("[SYSTEM] Connection to background pipeline dropped.")
		}
	}
	private fun appendUiLog(msg: String) {
		tvLog.append("$msg\n")
		svConsole.post { svConsole.fullScroll(ScrollView.FOCUS_DOWN) }
	}
}
