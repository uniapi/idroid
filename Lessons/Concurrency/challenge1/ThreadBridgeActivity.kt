/* \uFDFD
 *                     \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *        \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u0631\u0628\u064A\u0639 \u0671\u0644\u0622\u062E\u0631 \u06F2\u06F9
 */
package localhost.idroid.salamun

import android.util.Log
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import android.graphics.Color

/**
 * IDroid Project - Concurrency & Handlers Module
 * Challenge 1 (Easy): Pure-Code Progress Counter Engine Solution
 *
 * Architectural Constraints Met:
 * 1. 100% Pure Code / Programmatic UI Layout (Zero XML inflation used)
 * 2. Strict Native Platform SDK Only (Zero AndroidX dependencies)
 * 3. Strict Hungarian Notation View Prefixes (hMain, tvStatus, btnStart, llRoot)
 */
class ThreadBridgeActivity : Activity() {
	private companion object {
		const val TAG = "localhost.idroid.salamun"
	}
	private val hMain = Handler(Looper.getMainLooper())
	private lateinit var tvStatus: TextView
	private lateinit var btnStart: Button
	@Volatile	// Thread Safety Sentinel Flag to prevent concurrent overlapping thread initialization
	private var isTaskRunning = false

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val llRoot = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setGravity(Gravity.CENTER)
			setBackgroundColor(Color.parseColor("#121212"))	// Dark IDroid Matrix Style
			setPadding(40, 40, 40, 40)
		}
		tvStatus = TextView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 0, 80)
			}
			gravity = Gravity.CENTER
			text = "Engine Idle. Awaiting Start Script."
			textSize = 20f
			setTextColor(Color.parseColor("#E0E0E0"))
		}
		btnStart = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			text = "EXECUTE WORKER ENGINE"
			textSize = 16f
			setTextColor(Color.BLACK)
			setBackgroundColor(Color.parseColor("#00FF66"))	// Distinct Hacker Green
			setOnClickListener {
				executeBackgroundWorkerPipeline()
			}
		}
		with(llRoot) {
			addView(tvStatus)
			addView(btnStart)
		}
		setContentView(llRoot)
	}
	private fun executeBackgroundWorkerPipeline() {
		if (isTaskRunning)
			return
		isTaskRunning = true
		btnStart.isEnabled = false
		btnStart.setBackgroundColor(Color.parseColor("#555555"))
		tvStatus.text = "Initializing Isolated Thread..."

		Thread {
			for (progress in 1..100) {
				try {
					Thread.sleep(100)
				} catch (e: InterruptedException) {
					Thread.currentThread().interrupt()	// Gracefully break execution if thread is interrupted
					return
				}
				hMain.post {
					tvStatus.text = "Processing Telemetry Data: $progress%"
				}
			}
			hMain.post {
				tvStatus.text = "Task Complete! Core Processed Cleanly. ✅"
				btnStart.isEnabled = true
				btnStart.setBackgroundColor(Color.parseColor("#00FF66"))
				isTaskRunning = false
			}
		}.start()
	}
	override fun onDestroy() {
		super.onDestroy()
		// Wipe out any un-executed Runnables or hanging messages left in the UI MessageQueue buffer
		hMain.removeCallbacksAndMessages(null)
	}
}
