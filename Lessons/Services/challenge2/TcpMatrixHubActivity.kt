/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F1\u06F1
 */
package localhost.idroid.salamun

import android.util.Log
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Build
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
import android.graphics.Color
import android.graphics.Typeface

class TcpMatrixHubActivity : Activity() {
	private companion object {
		const val TAG = "localhost.idroid.salamun"
	}
	private val hMain = Handler(Looper.getMainLooper())
	// UI Elements
	private lateinit var llRoot: LinearLayout
	private lateinit var llConfig: LinearLayout
	private lateinit var etIp: EditText
	private lateinit var etPort: EditText
	private lateinit var llControl: LinearLayout
	private lateinit var btnStartSvc: Button
	private lateinit var btnStopSvc: Button
	private lateinit var svConsole: ScrollView
	private lateinit var tvLog: TextView
	private lateinit var llCommand: LinearLayout
	private lateinit var etCommand: EditText
	private lateinit var btnSendCommand: Button

	// Broadcast Receiver to catch text packets pushed from the Foreground Service
	private val matrixUpdateReceiver = object : BroadcastReceiver() {
		override fun onReceive(context: Context?, intent: Intent?) {
			val payloadText = intent?.getStringExtra("EXTRA_PAYLOAD") ?: return
			hMain.post {
				tvLog.text = payloadText
				svConsole.post { svConsole.fullScroll(ScrollView.FOCUS_DOWN) }
			}
		}
	}
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		initUi()
		setContentView(llRoot)

		btnStartSvc.setOnClickListener {
			val targetIp = etIp.text.toString().trim()
			val targetPortStr = etPort.text.toString().trim()
			if (targetIp.isEmpty() || targetPortStr.isEmpty()) {
				tvLog.append("[SYSTEM] Invalid parameters.\n")
				return@setOnClickListener
			}
			val targetPort = targetPortStr.toIntOrNull() ?: 9999
			val intent = Intent(this, TcpMatrixHubService::class.java).apply {
				putExtra("KEY_IP", targetIp)
				putExtra("KEY_PORT", targetPort)
			}
			startService(intent)
			Log.d(TAG, "startService")
			tvLog.append("[SYSTEM] Initialization vector pushed to Foreground Service.\n")
		}
		btnStopSvc.setOnClickListener {
			stopService(Intent(this, TcpMatrixHubService::class.java))
			tvLog.append("[SYSTEM] Teardown vector pushed to Foreground Service.\n")
		}
		btnSendCommand.setOnClickListener {
			val cmd = etCommand.text.toString().trim()
			if (cmd.isNotEmpty()) {
				val intent = Intent(TcpMatrixHubService.ACTION_TX).apply {
					putExtra("KEY_CMD", cmd)
				}
				sendBroadcast(intent)
				etCommand.setText("")
			}
		}
	}
	override fun onStart() {
		super.onStart()
		val filter = IntentFilter(TcpMatrixHubService.ACTION_LOG)
		// Multi-argument signature bypass for API 33+ Context.RECEIVER_NOT_EXPORTED
		registerReceiver(matrixUpdateReceiver, filter)
		// Sync log state instantly on visibility wake
		sendBroadcast(Intent(TcpMatrixHubService.ACTION_SYNC))
	}
	override fun onStop() {
		super.onStop()
		unregisterReceiver(matrixUpdateReceiver)
	}
	private fun initUi() {
		llRoot = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setBackgroundColor(Color.parseColor("#0D0E15"))
			setPadding(35, 45, 35, 45)
		}
		llConfig = LinearLayout(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			orientation = LinearLayout.HORIZONTAL
			weightSum = 3.0f
		}
		etIp = EditText(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 2.0f).apply {
				setMargins(0, 0, 16, 0)
			}
			hint = "Target IP"
			inputType = InputType.TYPE_CLASS_TEXT
			setTypeface(Typeface.MONOSPACE)
			setHintTextColor(Color.GRAY)
			setTextColor(Color.WHITE)
			setBackgroundColor(Color.parseColor("#1E2030"))
			setPadding(16, 8, 0, 8)
			setText("10.0.2.2")
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
			setText("9999")
		}
		llControl = LinearLayout(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 16, 0, 16)
			}
			orientation = LinearLayout.HORIZONTAL
		}
		btnStartSvc = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f)
			text = "START ENGINE"
			textSize = 14f
			setTextColor(Color.BLACK)
			setBackgroundColor(Color.parseColor("#82AAFF"))	// Cobalt System
		}
		btnStopSvc = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f)
			text = "STOP ENGINE"
			textSize = 14f
			setTextColor(Color.BLACK)
			setBackgroundColor(Color.parseColor("#FF5555"))	// Emergency Red
		}
		svConsole = ScrollView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f)
			setBackgroundColor(Color.parseColor("#141622"))
			setPadding(20, 20, 20, 20)
		}
		tvLog = TextView(this).apply {
			text = "CONSOLE INITIALIZED...\n"
			textSize = 13f
			setTypeface(Typeface.MONOSPACE)
			setTextColor(Color.parseColor("#8BE9FD"))		// Telemetry Cyan
			setLineSpacing(5f, 1f)
		}
		llCommand = LinearLayout(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 30, 0, 0)
			}
		}
		etCommand = EditText(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1.0f)
			hint = "Execute remote command"
			setTypeface(Typeface.MONOSPACE)
			setHintTextColor(Color.GRAY)
			setTextColor(Color.WHITE)
			setBackgroundColor(Color.parseColor("#1E2030"))
			setPadding(20, 25, 20, 25)
		}
		btnSendCommand = Button(this).apply {
			text = "EXEC"
			setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
			setTextColor(Color.BLACK)
			setBackgroundColor(Color.parseColor("#50FA7B"))	// Matrix Green
		}
		with(llConfig) {
			addView(etIp)
			addView(etPort)
		}
		with(llControl) {
			addView(btnStartSvc)
			addView(btnStopSvc)
		}
		svConsole.addView(tvLog)
		with(llCommand) {
			addView(etCommand)
			addView(btnSendCommand)
		}
		with(llRoot) {
			addView(llConfig)
			addView(llControl)
			addView(svConsole)
			addView(llCommand)
		}

	}
}
