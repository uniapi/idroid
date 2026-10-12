/* \uFDFD
 *                     \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *        \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u0631\u0628\u064A\u0639 \u0671\u0644\u0622\u062E\u0631 \u06F2\u06F9
 */
package localhost.idroid.salamun

import java.util.concurrent.atomic.AtomicInteger

import android.util.Log
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
 * Challenge 2 (Advanced): Custom Thread Pool Progress Matrix Solution
 *
 * Architectural Constraints Met:
 * 1. 100% Pure Code / Programmatic UI Layout (Zero XML inflation)
 * 2. Strict Native Platform SDK Only (Zero AndroidX dependencies)
 * 3. Race Condition Shielding via Atomic Mutex Patterns (No high-level ExecutorServices)
 * 4. Multi-Lane Concurrent Progress Rendering with Strict Hungarian Notation View Prefixes (hMain, tvSummaryMetrics, btnSpawnWorker, llDynamicLaneContainer)
 */
class ThreadPoolMatrixActivity : Activity() {
	private companion object {
		const val TAG = "localhost.idroid.salamun"
	}
	private val hMain = Handler(Looper.getMainLooper())
	private lateinit var tvSummaryMetrics: TextView
	private lateinit var btnSpawnWorker: Button
	private lateinit var llDynamicLaneContainer: LinearLayout
	// Thread-Safe Atomic Trackers to guard shared states against parallel race conditions
	private val activeWorkersCount = AtomicInteger(0)
	private val totalProcessedUnits = AtomicInteger(0)
	// Structural limits
	private val maxConcurrentThreads = 4

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val llRoot = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setBackgroundColor(Color.parseColor("#0F111A"))	// Sleek Deep Shadow Palette
			setPadding(40, 50, 40, 50)
		}
		val llControlPanel = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setPadding(0, 0, 0, 40)
		}
		tvSummaryMetrics = TextView(this).apply {
			gravity = Gravity.CENTER_HORIZONTAL
			text = "ACTIVE THREADS: 0/$maxConcurrentThreads | ACCUMULATED STEPS: 0"
			textSize = 14f
			setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
			setTextColor(Color.parseColor("#82AAFF"))		// Cyber Blue Telemetry
			setPadding(0, 0, 0, 30)
		}
		btnSpawnWorker = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			text = "SPAWN BACKGROUND WORKER LANE"
			textSize = 15f
			setTypeface(Typeface.DEFAULT_BOLD)
			setTextColor(Color.WHITE)
			setBackgroundColor(Color.parseColor("#44475A"))	// Dormant Slate State
		}
		val svContainer = ScrollView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
		}
		llDynamicLaneContainer = LinearLayout(this).apply {
			layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
			orientation = LinearLayout.VERTICAL
		}
		// Add views to containers before applying dynamic mutations
		with(llControlPanel) {
			addView(tvSummaryMetrics)
			addView(btnSpawnWorker)
		}
		svContainer.addView(llDynamicLaneContainer)
		with(llRoot) {
			addView(llControlPanel)
			addView(svContainer)
		}
		updateSpawnButtonState()	// Adjust color based on atomic bounds initial check
		setContentView(llRoot)

		btnSpawnWorker.setOnClickListener {
			initializeDynamicWorkerLane()
		}

	}
	private fun initializeDynamicWorkerLane() {
		val currentActive = activeWorkersCount.get()
		if (currentActive >= maxConcurrentThreads)
			return
		// Atomically increment the thread tracking matrix
		val workerId = activeWorkersCount.incrementAndGet()
		updateSpawnButtonState()
		updateGlobalTelemetryDisplay()

		val llRowLayout = LinearLayout(this).apply {
			orientation = LinearLayout.HORIZONTAL
			setGravity(Gravity.CENTER_VERTICAL)
			setPadding(0, 20, 0, 20)
		}
		val tvLaneLabel = TextView(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.35f) // Weighted layout alignment constraints
			text = "Lane #$workerId [000%]"
			textSize = 14f
			setTypeface(Typeface.MONOSPACE)
			setTextColor(Color.parseColor("#EEFFFF"))
		}
		val pbLaneProgress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.65f)
			max = 100
			progress = 0
		}
		with(llRowLayout) {
			addView(tvLaneLabel)
			addView(pbLaneProgress)
		}
		llDynamicLaneContainer.addView(llRowLayout)

		Thread {
			val computationalDelay = (40..100).random().toLong()
			for (progressStep in 1..100) {
				try {
					Thread.sleep(computationalDelay)
				} catch (e: InterruptedException) {
					Thread.currentThread().interrupt()
					return
				}
				totalProcessedUnits.incrementAndGet()	// Accumulate progress atomically into the global shared metrics state engine

				hMain.post {
					pbLaneProgress.progress = progressStep
					tvLaneLabel.text = String.format("Lane #%d [%03d%%]", workerId, progressStep)
					updateGlobalTelemetryDisplay()
				}
			}
			hMain.post {
				tvLaneLabel.text = "Lane #$workerId [DONE]"
				tvLaneLabel.setTextColor(Color.parseColor("#50FA7B"))	// Matrix Green on Completion

				activeWorkersCount.decrementAndGet()	// Atomically decrement the running pool size
				updateSpawnButtonState()
				updateGlobalTelemetryDisplay()
			}
		}.start()
	}
	private fun updateSpawnButtonState() {
		val active = activeWorkersCount.get()
		with(btnSpawnWorker) {
			if (active >= maxConcurrentThreads) {
				isEnabled = false
				text = "POOL CAPACITY EXCEEDED"
				setBackgroundColor(Color.parseColor("#FF5555"))
			}
			else {
				isEnabled = true
				text = "SPAWN BACKGROUND WORKER LANE"
				setBackgroundColor(Color.parseColor("#FF79C6"))
			}
		}
	}
	private fun updateGlobalTelemetryDisplay() {
		tvSummaryMetrics.text = "ACTIVE THREADS: ${activeWorkersCount.get()}/$maxConcurrentThreads | ACCUMULATED STEPS: ${totalProcessedUnits.get()}"
	}
	override fun onDestroy() {
		super.onDestroy()
		hMain.removeCallbacksAndMessages(null)
	}
}
