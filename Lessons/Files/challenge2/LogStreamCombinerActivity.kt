/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F1
 */
package localhost.idroid.salamun

import java.util.Date
import java.text.SimpleDateFormat
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream

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
import android.graphics.Color
import android.graphics.Typeface

/**
 * IDroid Project - Files & Streams Module
 * Challenge 2 (Advanced): Multi-Track Binary Log Stream Combiner Solution
 *
 * Architectural Constraints Met:
 * 1. 100% Pure Code / Programmatic UI Layout (Zero XML)
 * 2. Strict Native Platform SDK Only (Zero Jetpack/AndroidX components)
 * 3. Raw Byte Stream Processing (FileInputStream/FileOutputStream with explicit buffer allocations)
 * 4. Asynchronous I/O Handling (No main-thread disk blockages)
 * 5. Explicit Variable Prefix Guidelines (hMain, llRoot, llControlPanel, btnGenerateFrags, btnCombineTracks, pbMergeProgress, svConsoleScroll, tvLogConsole)
 */
class LogStreamCombinerActivity : Activity() {
	private companion object {
		const val TAG = "localhost.idroid.salamun"
		const val FRAG_1 = "part_1.bin"
		const val FRAG_2 = "part_2.bin"
		const val FRAG_3 = "part_3.bin"
		const val MASTER_LOG = "master_output.bin"
	}
	// Main Thread Handler Bridge to post streaming data diagnostics safely to the views
	private val hMain = Handler(Looper.getMainLooper())

	private lateinit var btnGenerateFrags: Button
	private lateinit var btnCombineTracks: Button
	private lateinit var pbMergeProgress: ProgressBar
	private lateinit var svConsoleScroll: ScrollView
	private lateinit var tvLogConsole: TextView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val llRoot = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setBackgroundColor(Color.parseColor("#0B0E14"))	// Cyberpunk Deep Night
			setPadding(40, 50, 40, 50)
		}
		val tvHeader = TextView(this).apply {
			gravity = Gravity.CENTER_HORIZONTAL
			text = "BINARY STREAM LOG CONSOLIDATOR"
			textSize = 15f
			setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
			setTextColor(Color.parseColor("#50FA7B"))		// Matrix Hacker Green Accent
			setPadding(0, 0, 0, 40)
		}
		val llControlPanel = LinearLayout(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 0, 30)
			}
			orientation = LinearLayout.HORIZONTAL
		}
		btnGenerateFrags = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.5f).apply {
				setMargins(0, 0, 10, 0)
			}
			text = "WRITE TRACKS"
			textSize = 11f
			setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
			setTextColor(Color.WHITE)
			setBackgroundColor(Color.parseColor("#FF5555"))	// Warning Orange/Red State
		}
		btnCombineTracks = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.5f).apply {
				setMargins(10, 0, 0, 0)
			}
			text = "MERGE BYTES"
			textSize = 11f
			setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
			setTextColor(Color.BLACK)
			setBackgroundColor(Color.parseColor("#82AAFF"))	// Cobalt System Blue
		}
		pbMergeProgress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 40, 0)
			}
			max = 100
			progress = 0
			visibility = ProgressBar.INVISIBLE
		}
		svConsoleScroll = ScrollView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f)
			setBackgroundColor(Color.parseColor("#121620"))
			setPadding(20, 20, 20, 20)
		}
		tvLogConsole = TextView(this).apply {
			text = "SYSTEM READY: Awaiting user action request triggers...\n"
			textSize = 12f
			setTypeface(Typeface.MONOSPACE)
			setTextColor(Color.parseColor("#F1FA8C"))	// Informational Pale Yellow
			setLineSpacing(5f, 1f)
		}
		with(llControlPanel) {
			addView(btnGenerateFrags)
			addView(btnCombineTracks)
		}
		svConsoleScroll.addView(tvLogConsole)
		with(llRoot) {
			addView(tvHeader)
			addView(llControlPanel)
			addView(pbMergeProgress)
			addView(svConsoleScroll)
		}
		setContentView(llRoot)

		btnGenerateFrags.setOnClickListener {
			generateBinaryFragments()
		}
		btnCombineTracks.setOnClickListener {
			mergeBinaryTracksPipeline()
		}
	}
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putString("KEY_CONSOLE_HISTORY", tvLogConsole.text.toString())
	}
	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		super.onRestoreInstanceState(savedInstanceState)
		savedInstanceState.getString("KEY_CONSOLE_HISTORY")?.let { logs ->
			tvLogConsole.text = logs
		}
	}
	private fun generateBinaryFragments() {
		appendConsoleLog("SYS", "Initializing structural data segment generation track...")
		Thread {
			try {
				// Fragment 1: Header Bytes Verification Sequence
				val file1 = File(filesDir, FRAG_1)
				FileOutputStream(file1).use { fos ->
					fos.write(byteArrayOf(0x7F, 0x49, 0x44, 0x52, 0x4F, 0x49, 0x44, 0x0A))	// ELF-like IDROID header
				}
				// Fragment 2: Heavy simulated metrics binary buffer frames
				val file2 = File(filesDir, FRAG_2)
				FileOutputStream(file2).use { fos ->
					val dummyStatePayload = ByteArray(2048) { index -> (index % 256).toByte() }
					fos.write(dummyStatePayload)
				}
				// Fragment 3: System confirmation hash footprint verification code
				val file3 = File(filesDir, FRAG_3)
				FileOutputStream(file3).use { fos ->
					fos.write(byteArrayOf(0xAA.toByte(), 0xBB.toByte(), 0xCC.toByte(), 0xDD.toByte(), 0x00))
				}
				hMain.post {
					appendConsoleLog("IO_OK", "3 structural telemetry data track fragments built safely on disk.")
					appendConsoleLog("INFO", "Part 1: ${file1.length()} B | Part 2: ${file2.length()} B | Part 3: ${file3.length()} B")
				}
			} catch (e: Exception) {
				hMain.post { appendConsoleLog("ERROR", "Binary generation aborted: ${e.localizedMessage}") }
			}
		}.start()
	}
	private fun mergeBinaryTracksPipeline() {
		val targetFragments = arrayOf(FRAG_1, FRAG_2, FRAG_3)
		for (fileName in targetFragments) {
			if (!File(filesDir, fileName).exists()) {
				appendConsoleLog("FAIL", "Missing source element fragment: $fileName. Aborting consolidate script.")
				return
			}
		}
		appendConsoleLog("MERGE", "Ignited stream matrix consolidation pipeline...")
		btnCombineTracks.isEnabled = false
		with(pbMergeProgress) {
			progress = 0
			visibility = ProgressBar.VISIBLE
		}
		Thread {
			try {
				val masterFile = File(filesDir, MASTER_LOG)
				// Open raw master payload output capture stream channel
				FileOutputStream(masterFile).use { fosMaster ->
					// Allocate tight 1024 byte chunk constraint array mapping layer
					val byteBuffer = ByteArray(1024)
					targetFragments.forEachIndexed { index, fragName ->
						val currentFragFile = File(filesDir, fragName)

						hMain.post { appendConsoleLog("STREAM", "Injecting data tracks from track container: $fragName") }

						FileInputStream(currentFragFile).use { fisFrag ->
							var bytesRead = fisFrag.read(byteBuffer)
							while (bytesRead != -1) {
								// Write exact slice blocks to master directly
								fosMaster.write(byteBuffer, 0, bytesRead)
								bytesRead = fisFrag.read(byteBuffer)
							}
						}
						// Emulate progressive UI rendering flow steps
						val targetProgress = (index + 1) * 100 / targetFragments.size
						hMain.post { pbMergeProgress.progress = targetProgress }
						try {
							Thread.sleep(150)	// short trace pause for visual metrics validation
						} catch (e: InterruptedException) {
							Thread.currentThread().interrupt()
						}
					}
				}
				hMain.post {
					pbMergeProgress.visibility = ProgressBar.INVISIBLE
					btnCombineTracks.isEnabled = true
					appendConsoleLog("SUCCESS", "Consolidation complete! Unified payload sequence committed cleanly.")
					appendConsoleLog("MASTER", "Master Map Coordinates: $MASTER_LOG\nTotal Dimensions: ${masterFile.length()} Bytes")
				}
			}
			catch (e: Exception) {
				hMain.post {
					pbMergeProgress.visibility = ProgressBar.INVISIBLE
					btnCombineTracks.isEnabled = true
					appendConsoleLog("CRIT_ERR", "Consolidation crash routine: ${e.localizedMessage}")
				}
			}
		}.start()
	}
	private fun appendConsoleLog(tag: String, text: String) {
		val timestamp = SimpleDateFormat("HH:mm:ss:SSS").format(Date())
		tvLogConsole.append("[$timestamp] [$tag] $text\n")
		svConsoleScroll.fullScroll(ScrollView.FOCUS_DOWN)
	}
	override fun onDestroy() {
		super.onDestroy()
		hMain.removeCallbacksAndMessages(null)
	}
}
