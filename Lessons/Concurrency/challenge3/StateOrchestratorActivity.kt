/* \uFDFD
 *                     \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *        \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u0631\u0628\u064A\u0639 \u0671\u0644\u0622\u062E\u0631 \u06F2\u06F9
 */
package localhost.idroid.salamun

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

import android.util.Log
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.MessageQueue
import android.os.Message
import android.view.View
import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import android.widget.FrameLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Button
import android.widget.ProgressBar
import android.graphics.Typeface
import android.graphics.Color

/**
 * IDroid Project - Concurrency & Handlers Module
 * Challenge 3 (Master): Multi-Directional State Orchestrator & Event Bus Solution
 *
 * Architectural Constraints Met:
 * 1. 100% Pure Code / Programmatic UI Layout (Zero XML)
 * 2. Strict Native Platform SDK Only (Zero AndroidX dependencies)
 * 3. Single Persistent Background Thread with a Custom System Looper Loop Engine
 * 4. Explicit Messaging Primitives Factory Mapping ( Bypassing high-level Runnables )
 * 5. Full Observance of UI View Naming Prefixes (btn, tv, pb, ll, sv)
 */
class StateOrchestratorActivity : Activity() {
	companion object {
		const val TAG = "localhost.idroid.salamun"
		const val MSG_PRIORITY_TASK = 0x101
		const val MSG_LOG_ANALYSIS = 0x102
		const val MSG_LIFECYCLE_ALERT = 0x103
	}
	// Main UI Thread Handler Bridge
	private val hMain = Handler(Looper.getMainLooper())
	// Reference to the persistent custom looper handler engine running on the background thread
	private var hOrchestrator: Handler? = null
	private lateinit var thrOrchestrator: StateOrchestratorThread
	// UI Layout references
	private lateinit var tvTelemetryConsole: TextView
	private lateinit var pbSystemLoad: ProgressBar
	private lateinit var btnDispatchPriority: Button
	private lateinit var btnDispatchAnalysis: Button
	private lateinit var svConsoleScroll: ScrollView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val llRoot = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setBackgroundColor(Color.parseColor("#0A0C10"))	// Pitch Dark Terminal
			setPadding(35, 45, 35, 45)
		}
		val tvHeader = TextView(this).apply {
			gravity = Gravity.CENTER_HORIZONTAL
			text = "SYSTEM STATE ORCHESTRATOR BUS"
			textSize = 15f
			setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
			setTextColor(Color.parseColor("#FFB86C"))		// Warning Orange Accent
			setPadding(0, 0, 0, 40)
		}
		btnDispatchPriority = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 0, 20)
			}
			text = "DISPATCH PRIORITY CRUNCH"
			textSize = 14f
			setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
			setTextColor(Color.BLACK)
			setBackgroundColor(Color.parseColor("#50FA7B"))	// Matrix Green
		}
		btnDispatchAnalysis = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 0, 30)
			}
			text = "RUN RAW LOG STREAM DECODE"
			textSize = 14f
			setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
			setTextColor(Color.WHITE)
			setBackgroundColor(Color.parseColor("#BD93F9"))	// Soft Purple
		}
		pbSystemLoad = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 0, 40)
			}
			max = 100
			progress = 0
			visibility = ProgressBar.INVISIBLE
		}
		svConsoleScroll = ScrollView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f)	// Weight matches standard adapter container structures
			setBackgroundColor(Color.parseColor("#141923"))
			setPadding(20, 20, 20, 20)
		}
		tvTelemetryConsole = TextView(this).apply {
id = 1
			layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			text = "LOG ENGINE ONLINE: Awaiting Thread Looper initialization..."
			textSize = 12f
			setTypeface(Typeface.MONOSPACE)
			setTextColor(Color.parseColor("#8BE9FD"))	// Telemetry Cyan
		}
		svConsoleScroll.addView(tvTelemetryConsole)
		with(llRoot) {
			addView(tvHeader)
			addView(btnDispatchPriority)
			addView(btnDispatchAnalysis)
			addView(pbSystemLoad)
			addView(svConsoleScroll)
		}
		setContentView(llRoot)
		// Initialize and Start the Custom Persistent Background Looper Thread
		thrOrchestrator = StateOrchestratorThread { hBackground ->
			// Callback interface intercepts the generated background loop handler instance
			hOrchestrator = hBackground
			appendConsoleLog("SUCCESS", "Decoupled Event Bus Thread Looper Pipeline Ignited Safely.")
		}
		thrOrchestrator.start()
		// Attach Action Event Listeners passing primitives directly down the bus line
		btnDispatchPriority.setOnClickListener {
			hOrchestrator?.let { handler ->
				val msg = handler.obtainMessage(MSG_PRIORITY_TASK).apply {
					arg1 = (1000..9999).random()	// Unique Task Transaction ID token
				}
				handler.sendMessage(msg)	// Completely non-blocking dispatch
				appendConsoleLog("QUEUE", "Dispatched High Priority Math Event #${msg.arg1}")
			}
		}
		btnDispatchAnalysis.setOnClickListener {
			hOrchestrator?.let { handler ->
				// Allocates zero new memory structures by reusing cached messages pulling directly from the global recycle pool
				val msg = Message.obtain(handler, MSG_LOG_ANALYSIS).apply {
					obj = "KERNEL_STREAM_DUMP_0x${(100..999).random()}"
				}
				handler.sendMessage(msg)
				appendConsoleLog("QUEUE", "Dispatched String Processing Stream Token payload.")
			}
		}
	}
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putString("KEY_CONSOLE_HISTORY", tvTelemetryConsole.text.toString())
	}
	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		super.onRestoreInstanceState(savedInstanceState)
		savedInstanceState.getString("KEY_CONSOLE_HISTORY")?.let { logs ->
			tvTelemetryConsole.text = logs
		}
	}
	// Pass lifecycle mutations safely down to the background log matrix engine
	override fun onResume() {
		super.onResume()
		forwardLifecycleSignal("ON_RESUME")
	}
	override fun onPause() {
		super.onPause()
		forwardLifecycleSignal("ON_PAUSE")
	}
	private fun forwardLifecycleSignal(stateName: String) {
		hOrchestrator?.let { handler ->
			val msg = handler.obtainMessage(MSG_LIFECYCLE_ALERT).apply { obj = stateName }
			handler.sendMessage(msg)
		}
	}
	private fun appendConsoleLog(tag: String, text: String) {
		val logAction = Runnable {
			val timeStamp = SimpleDateFormat("HH:mm:ss.SSS").format(Date())
			tvTelemetryConsole.append("[$timeStamp] [$tag] $text\n")
			// Auto Scroll to the absolute layout baseline so the stream stays real-time
			svConsoleScroll.fullScroll(ScrollView.FOCUS_DOWN)
		}
		if (Looper.myLooper() == Looper.getMainLooper())
			logAction.run()
		else
			hMain.post(logAction)
	}
	inner class StateOrchestratorThread(
		private val onLooperReady: (Handler) -> Unit
	) : Thread("IDroidStateOrchestratorThread") {
		override fun run() {
			Looper.prepare()	// Allocate the foundational system message loop structures inside this explicit thread
			// Access the underlying message queue of this background thread
			val queue: MessageQueue = Looper.myQueue()
			// Define an explicit IdleHandler to maintenance work when the queue is dry
			val maintenanceIdleHandler = MessageQueue.IdleHandler {
				// This block executes ONLY when the background thread runs out of messages to process
				appendConsoleLog("IDLE", "Background queue cleared. Running memory cache flush...")
				// Return true to keep this IdleHandler alive for future idle gaps.
				// Return false if you want it to execute exactly once and unregister itself automatically.
				true
			}
			// Attach it to the event stream architecture
			queue.addIdleHandler(maintenanceIdleHandler)
			// Initialzie the inner handler coupled permanently to this background thread's executionn timeline
			val hBackgroundWorker = object : Handler(Looper.myLooper()!!) {
				override fun handleMessage(msg: Message) {
					try {
						when (msg.what) {
							MSG_PRIORITY_TASK -> {
								val id = msg.arg1
								for (i in 1..5) {
									Thread.sleep(80)
									hMain.post { pbSystemLoad.apply { visibility = ProgressBar.VISIBLE; progress = i * 20 } }
								}
								hMain.post {
									pbSystemLoad.visibility = ProgressBar.INVISIBLE
								}
								appendConsoleLog("COMPUTE", "Math Array #${id} Compiled successfully off UI Thread.")
							}
							MSG_LOG_ANALYSIS -> {
								val streamData = msg.obj as String
								Thread.sleep(250)	// Simulation string parsing delay
								appendConsoleLog("DECODE", "String payload matrix complete: ${streamData.toLowerCase()}")
							}
							MSG_LIFECYCLE_ALERT -> {
								val lifecycleState = msg.obj as String
								appendConsoleLog("LIFECYCLE", "System State Orchestrator Sync: Activity moved to -> $lifecycleState")
							}
						}
					} catch (e: InterruptedException) {
						appendConsoleLog("SHUTDOWN", "Thread loop safely interrupted during sleep sequence. Exiting.")
						Thread.currentThread().interrupt()
					}
				}
			}
			onLooperReady(hBackgroundWorker)	// Expose the ready handler instance back to the foreground Activity scope context
			Looper.loop()	// Start pumping the FIFO MessageQueue infinitely until told to quit
		}
	}
	override fun onDestroy() {
		super.onDestroy()
		hMain.removeCallbacksAndMessages(null)	// Wipe out any pending frame events left inside the UI Messsage Queue pipeline
		hOrchestrator?.looper?.quit()				// Clear and stop the custom background loop safely using explicit platform quit hooks
	}
}
