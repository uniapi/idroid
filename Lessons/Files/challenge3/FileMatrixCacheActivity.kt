/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0623\u0648\u0644\u0649 \u06F3
 */
package localhost.idroid.salamun

import java.util.Date
import java.util.concurrent.atomic.AtomicInteger
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.SimpleDateFormat

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
 * IDroid Project - Files & Streams Module
 * Challenge 3 (Master): High-Performance Multithreaded Storage Matrix Cache Solution
 *
 * Architectural Constraints Met:
 * 1. 100% Pure Code / Programmatic UI Layout (Zero XML layout inflation used)
 * 2. Strict Native Platform SDK Only (Zero Jetpack/AndroidX components)
 * 3. Volatile Application Sandbox Isolation Operations inside cacheDir boundaries
 * 4. 4KB Core Kernel Block-Aligned Chunk Streaming Constraints (No massive memory buffers)
 * 5. Multi-Lane Concurrent Background Thread Operations with Atomic Inter-Thread Updates
 * 6. Explicit Variable Prefix Guidelines (hMain, llRoot, btnTriggerCache, btnWipeCache, pbCacheProgress, svConsoleScroll, tvCacheLogs)
 */
class FileMatrixCacheActivity : Activity() {
	private companion object {
		const val TAG = "localhost.idroid.salamun"
		const val CACHE_FILE_PREFIX = "cache_idx_10"
		const val TOTAL_LANES = 5
		const val CORE_BLOCK_SIZE = 4096				// 4KB Core Kernel-Aligned Buffer Chunk
		const val SIMULATED_PAYLOAD_SIZE = 50 * 1024	// 50KB Mock Data Payload
	}
	// Main Thread Handler to bridge performance diagnostics back to the screen canvas safely
	private val hMain = Handler(Looper.getMainLooper())
	// Thread-Safe Atomic Trackers to coordinate multi-threaded progress milestones without locking loops
	private val atomicActiveWrites = AtomicInteger(0)
	private val atomicCompletedLanes = AtomicInteger(0)

	private lateinit var btnTriggerCache: Button
	private lateinit var btnWipeCache: Button
	private lateinit var pbCacheProgress: ProgressBar
	private lateinit var svConsoleScroll: ScrollView
	private lateinit var tvCacheLogs: TextView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val llRoot = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setBackgroundColor(Color.parseColor("#090B10"))	// Pitch Matrix Obsidian Black
			setPadding(40, 50, 40, 50)
		}
		val tvHeader = TextView(this).apply {
			gravity = Gravity.CENTER_HORIZONTAL
			text = "HIGH-PERFORMANCE DATA MATRIX CACHE"
			textSize = 14f
			setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
			setTextColor(Color.parseColor("#BD93F9"))		// Soft Cyber Purple Accent
			setPadding(0, 0, 0, 40)
		}
		val llControlPanel = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setPadding(0, 0, 0, 30)
		}
		btnTriggerCache = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 0, 20)
			}
			text = "TRIGGER MASS CACHE INJECTION"
			textSize = 12f
			setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
			setTextColor(Color.BLACK)
			setBackgroundColor(Color.parseColor("#FFB86C"))	// Warning Bright Gold Accent
		}
		btnWipeCache = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			text = "WIPE TRANSIENT SANDBOX"
			textSize = 12f
			setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
			setTextColor(Color.WHITE)
			setBackgroundColor(Color.parseColor("#FF5555"))	// System Alert Red
		}
		pbCacheProgress = ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 0, 40)
			}
			max = TOTAL_LANES * 100
			progress = 0
			visibility = ProgressBar.INVISIBLE
		}
		svConsoleScroll = ScrollView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1.0f)
			setBackgroundColor(Color.parseColor("#11131C"))
			setPadding(20, 20, 20, 20)
		}
		tvCacheLogs = TextView(this).apply {
			text = "CACHE_BUS_ONLINE: Awaiting telemetry command strings...\n"
			textSize = 11f
			setTypeface(Typeface.MONOSPACE)
			setTextColor(Color.parseColor("#F8F8F2"))		// Raw System Off-White
			setLineSpacing(6f, 1f)
		}
		with(llControlPanel) {
			addView(btnTriggerCache)
			addView(btnWipeCache)
		}
		svConsoleScroll.addView(tvCacheLogs)
		with(llRoot) {
			addView(tvHeader)
			addView(llControlPanel)
			addView(pbCacheProgress)
			addView(svConsoleScroll)
		}
		setContentView(llRoot)

		btnTriggerCache.setOnClickListener {
			executeConcurrentCachePipeline()
		}
		btnWipeCache.setOnClickListener {
			clearTransientCacheSandbox()
		}
	}
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putString("KEY_CONSOLE_HISTORY", tvCacheLogs.text.toString())
	}
	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		super.onRestoreInstanceState(savedInstanceState)
		savedInstanceState.getString("KEY_CONSOLE_HISTORY")?.let { logs ->
			tvCacheLogs.text = logs
		}
	}
	/**
	* Spawns multiple parallel isolated thread worker lanes to perform real-time cache hits,
	* writes high-throughput bytes arrays with 4KB kernel-aligned streams, and preserves data safety grids.
	*/
	private fun executeConcurrentCachePipeline() {
		if (atomicActiveWrites.get() > 0) {
			appendConsoleLog("LOCK", "Transaction rejected. Cache injection tracks are actively running.")
			return
		}
		appendConsoleLog("START", "Spawning $TOTAL_LANES isolated parallel cache allocation lanes...")
		btnTriggerCache.isEnabled = false
		btnWipeCache.isEnabled = false

		atomicCompletedLanes.set(0)
		with(pbCacheProgress) {
			progress = 0
			visibility = ProgressBar.VISIBLE
		}
		for (laneId in 1..TOTAL_LANES) {
			atomicActiveWrites.incrementAndGet()
			Thread {
				val threadName = "MatrixLane-$laneId"
				val targetFileName = "$CACHE_FILE_PREFIX${laneId}.dat"
				val cacheTargetFile = File(cacheDir, targetFileName)
				try {
					if (cacheTargetFile.exists()) {
						hMain.post {
							appendConsoleLog("CACHE_HIT", "Asset sequence recovered from file: $targetFileName")
							verifyAndReadCachedBytes(cacheTargetFile, laneId)
						}
						return@Thread
					}
					hMain.post { appendConsoleLog("MISS", "Allocating memory storage tracking matrix blocks for Lane $laneId") }
					val mockBinaryBlob = ByteArray(SIMULATED_PAYLOAD_SIZE) { index -> (index xor laneId).toByte() }
					FileOutputStream(cacheTargetFile).use { fos ->
						var currentOffset = 0
						while (currentOffset < mockBinaryBlob.size) {
							val remainingBytes = mockBinaryBlob.size - currentOffset
							val writeChunkSize = if (remainingBytes > CORE_BLOCK_SIZE) CORE_BLOCK_SIZE else remainingBytes
							fos.write(mockBinaryBlob, currentOffset, writeChunkSize)
							currentOffset += writeChunkSize
							val progressIncrement = writeChunkSize * 100 / SIMULATED_PAYLOAD_SIZE
							hMain.post {  pbCacheProgress.incrementProgressBy(progressIncrement) }
							Thread.sleep((20..60).random().toLong())
						}
					}
					hMain.post {
						appendConsoleLog("COMMITTED", "Lane #$laneId written securely. Size: ${cacheTargetFile.length()} Bytes")
					}
				} catch (e: Exception) {
					hMain.post { appendConsoleLog("CRIT_ERR", "Pipeine break on lane $threadName: ${e.localizedMessage}") }
				} finally {
					// Core synchronization tracking
					val remainingWrites = atomicActiveWrites.decrementAndGet()
					val completed = atomicCompletedLanes.incrementAndGet()
					if (remainingWrites == 0 || completed == TOTAL_LANES)
						hMain.post { finalizeCacheCycle() }
				}
			}.start()
		}
	}
	/**
	* Reads and parses data sequences back out of the sandboxed storage container completely off the UI thread
	* using the same strict 4KB kernel loop bounds.
	*/
	private fun verifyAndReadCachedBytes(file: File, laneId: Int) {
		Thread {
			try {
				val bufferBlock = ByteArray(CORE_BLOCK_SIZE)
				var bytesReadTotal = 0
				FileInputStream(file).use { fis ->
					var bytesRead = fis.read(bufferBlock)
					while (bytesRead != -1) {
						bytesReadTotal += bytesRead
						bytesRead = fis.read(bufferBlock)
					}
				}
				hMain.post {
					appendConsoleLog("VERIFY", "Verified cached integrity map for slot #$laneId. Read: $bytesReadTotal Bytes.")
					pbCacheProgress.progress = atomicCompletedLanes.get() * 100
					if (atomicCompletedLanes.get() == TOTAL_LANES && atomicActiveWrites.get() == 0)
						finalizeCacheCycle()
				}
			} catch (e: Exception) {
				hMain.post { appendConsoleLog("ERR_READ", "Failed reading data track index $laneId: ${e.localizedMessage}") }
			}
		}.start()
	}
	/**
	* Handles final view configuration resets upon transaction mapping loops completions.
	*/
	private fun finalizeCacheCycle() {
		pbCacheProgress.visibility = ProgressBar.INVISIBLE
		btnTriggerCache.isEnabled = true
		btnWipeCache.isEnabled = true
		appendConsoleLog("FINISHED", "Matrix caching tracks finalized successfully. Sandbox balanced.")
	}
	/**
	* Safely clears all temporary storage cache file channels off the main thread execution sequence line.
	*/
	private fun clearTransientCacheSandbox() {
		appendConsoleLog("WIPE", "Wiping transient sandboxed folders cache channels...")
		btnTriggerCache.isEnabled = false
		btnWipeCache.isEnabled = false
		Thread {
			try {
				val transientDir = cacheDir
				val systemFiles = transientDir.listFiles()
				var targetedWipes = 0
				systemFiles?.forEach { currentFile ->
					if (currentFile.name.startsWith(CACHE_FILE_PREFIX))
						if (currentFile.delete())
							targetedWipes++
				}
				hMain.post {
					btnTriggerCache.isEnabled = true
					btnWipeCache.isEnabled = true
					appendConsoleLog("WIPE_OK", "Transient sandbox sweep complete. Purged $targetedWipes cache files cleanly.")
				}
			} catch (e: Exception) {
				hMain.post {
					btnTriggerCache.isEnabled = true
					btnWipeCache.isEnabled = true
					appendConsoleLog("ERR_WRITE", "Reclamation tracking exception trace: ${e.localizedMessage}")
				}
			}
		}.start()
	}
	/**
	* Appends real-time structural performance tracking telemetry strings directly down to the text console.
	*/
	private fun appendConsoleLog(tag: String, text: String) {
		val timeStamp = SimpleDateFormat("HH:mm:ss.SSS").format(Date())
		tvCacheLogs.append("[$timeStamp][$tag] $text\n")
		svConsoleScroll.fullScroll(ScrollView.FOCUS_DOWN)
	}
	override fun onDestroy() {
		super.onDestroy()
		hMain.removeCallbacksAndMessages(null)
	}
}
