/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F1\u06F3
 */
package localhost.idroid.salamun

import android.util.Log
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.view.View
import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.EditText
import android.widget.Button
import android.graphics.Typeface
import android.graphics.Color

/**
 * IDroid Project - BackgroundServices Module
 * Challenge 3 (Master): Multi-Client TLS Cryptographic Multiplexer Engine Solution
 * UI Controller Panel Layer
 *
 * Architectural Constraints Met:
 * 1. 100% Pure Code / Programmatic UI Layout (Zero XML Used)
 * 2. Pure Native Android APIs (Zero Jetpack / AndroidX components)
 * 3. Asynchronous Telemetry Synchronization Engine
 * 4. Strict Variable Shorthand Notation Compliance (hMain, llRoot, btnStartEngine, etc.)
 * 5. 100% Native API Level 10 Compile-safe Architecture Profiles
 */
class TlsMuxEngineActivity : Activity() {
	private companion object {
		const val TAG = "localhost.idroid.salamun"
	}
	private val hMain = Handler(Looper.getMainLooper())
	// UI Architecture Layout elements
	private lateinit var llRoot: LinearLayout
	private lateinit var llPanel: LinearLayout
	private lateinit var etPort: EditText
	private lateinit var tvClientCount: TextView
	private lateinit var btnStartEngine: Button
	private lateinit var btnStopEngine: Button
	private lateinit var svConsole: ScrollView
	private lateinit var tvLog: TextView
	// Decoupled inter-process telemetry data receiver matrix
	private val telemetryReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			val logBatch = intent?.getStringExtra("EXTRA_MUX_LOG") ?: return
			val activeClients = intent.getIntExtra("EXTRA_CLIENT_COUNT", 0)
			hMain.post {
				tvClientCount.text = "ACTIVE TERMINAL NODES: $activeClients"
				tvLog.text = logBatch
				svConsole.post { svConsole.fullScroll(ScrollView.FOCUS_DOWN) }
			}
		}
	}
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		initUi()
		setContentView(llRoot)

		btnStartEngine.setOnClickListener {
			val portText = etPort.text.toString().trim()
			if (portText.isEmpty()) {
				tvLog.append("[ERROR] Cryptographic interface port binding missing.\n")
				return@setOnClickListener
			}
			val listenPort = portText.toIntOrNull() ?: 8443
			val intent = Intent(this, TlsMuxEngineService::class.java).apply {
				putExtra("KEY_LISTEN_PORT", listenPort)
			}
			startService(intent)
			tvLog.append("[SYSTEM] Initialization request injected to TLS Engine.\n")
			btnStartEngine.isEnabled = false
			btnStartEngine.setBackgroundColor(Color.parseColor("#CDDDFF"))
			btnStopEngine.isEnabled = true
			btnStopEngine.setBackgroundColor(Color.parseColor("#FF5555"))	// Emergency Red
		}
		btnStopEngine.setOnClickListener {
			stopService(Intent(this, TlsMuxEngineService::class.java))
			tvLog.append("[SYSTEM] Emergency halt request executed.\n")
			btnStartEngine.isEnabled = true
			btnStartEngine.setBackgroundColor(Color.parseColor("#82AAFF"))	// Cobalt System
			btnStopEngine.isEnabled = false
			btnStopEngine.setBackgroundColor(Color.parseColor("#FFB3B3"))
		}
	}
	override fun onStart() {
		super.onStart()
		val filter = IntentFilter(TlsMuxEngineService.ACTION_MUX_TELEMETRY)
		registerReceiver(telemetryReceiver, filter)
		// Push instance request parameter synchronization up to background server state pool
		sendBroadcast(Intent(TlsMuxEngineService.ACTION_MUX_SYNC))
	}
	override fun onStop() {
		super.onStop()
		unregisterReceiver(telemetryReceiver)
	}
	private fun initUi() {
		llRoot = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setBackgroundColor(Color.parseColor("#0D0E15"))
			setPadding(35, 45, 35, 45)
		}
		llPanel = LinearLayout(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			orientation = LinearLayout.HORIZONTAL
			weightSum = 3.0f
		}
		etPort = EditText(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f).apply {
				setMargins(16, 0, 0, 0)
			}
			hint = "Port"
			inputType = InputType.TYPE_CLASS_NUMBER
			setTypeface(Typeface.MONOSPACE)
			setHintTextColor(Color.GRAY)
			setTextColor(Color.WHITE)
			setBackgroundColor(Color.parseColor("#1E2030"))
			setPadding(16, 8, 0, 8)
			setText("8443")
		}
		tvClientCount = TextView(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 2.0f)
			text = "ACTIVE TERMINAL NODES: 0"
			textSize = 12f
			setGravity(Gravity.CENTER_VERTICAL)
			setTextColor(Color.parseColor("#FFB86C"))		// Alert Amber
			setPadding(32, 0, 0, 0)
		}
		val llControl = LinearLayout(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 16, 0, 16)
			}
			orientation = LinearLayout.HORIZONTAL
		}
		btnStartEngine = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f)
			text = "START ENGINE"
			textSize = 14f
			setTextColor(Color.BLACK)
			setBackgroundColor(Color.parseColor("#82AAFF"))	// Cobalt System
		}
		btnStopEngine = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f)
			text = "STOP ENGINE"
			textSize = 14f
			setTextColor(Color.BLACK)
			setBackgroundColor(Color.parseColor("#FFB3B3"))
			isEnabled = false
		}
		svConsole = ScrollView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f)
			setBackgroundColor(Color.parseColor("#141622"))
			setPadding(20, 20, 20, 20)
		}
		tvLog = TextView(this).apply {
			text = "CRYPTO SYSTEM RE-ROUTING OPERATIONAL PANELS STATUS: UNBOUND...\n"
			textSize = 13f
			setTypeface(Typeface.MONOSPACE)
			setTextColor(Color.parseColor("#8BE9FD"))		// Telemetry Cyan
			setLineSpacing(5f, 1f)
		}
		with(llPanel) {
			addView(etPort)
			addView(tvClientCount)
		}
		with(llControl) {
			addView(btnStartEngine)
			addView(btnStopEngine)
		}
		svConsole.addView(tvLog)
		with(llRoot) {
			addView(llPanel)
			addView(llControl)
			addView(svConsole)
		}
	}
}
