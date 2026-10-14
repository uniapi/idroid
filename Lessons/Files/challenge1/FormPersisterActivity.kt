/* \uFDFD
 *                     \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *        \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u0631\u0628\u064A\u0639 \u0671\u0644\u0622\u062E\u0631 \u06F3\u06F0
 */
package localhost.idroid.salamun

import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.charset.StandardCharsets

import android.util.Log
import android.graphics.Color
import android.graphics.Typeface
import android.app.Activity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import android.widget.EditText
import android.widget.TextView
import android.widget.Button

/**
 * IDroid Project - Files & Streams Module
 * Challenge 1 (Easy): Programmatic Form Persister Engine Solution
 *
 * Architectural Constraints Met:
 * 1. 100% Pure Code / Programmatic UI Layout (Zero XML layout files)
 * 2. Strict Native Platform SDK Only (Zero Jetpack/AndroidX components)
 * 3. Complete Integration of Concurrency handling to prevent main thread blocking
 * 4. Proper Shorthand Hungarian View Notation (hMain, llRoot, etInputPayload, btnCommitDisk, btnFlushDisk, tvFileLogs)
 */
class FormPersisterActivity : Activity() {
	private companion object {
		const val TAG = "localhost.idroid.salamun"
		const val FILE_NAME = "sandboxed_payload.txt"
	}
	private val hMain = Handler(Looper.getMainLooper())
	private lateinit var etInputPayload: EditText
	private lateinit var btnCommitDisk: Button
	private lateinit var btnFlushDisk: Button
	private lateinit var tvFileLogs: TextView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val llRoot = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setBackgroundColor(Color.parseColor("#0F141C"))	//Sleek Terminal Matrix Charcoal
			setPadding(40, 50, 40, 50)
		}
		val tvHeader = TextView(this).apply {
			text = "SANDBOXED STREAM PERSISTER"
			textSize = 16f
			setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
			setTextColor(Color.parseColor("#FF79C6"))		// Interactive Pink Accent
			setPadding(0, 0, 0, 40)
		}
		etInputPayload = EditText(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 0, 30)
			}
			hint = "Enter raw storage telemetry payload sequence..."
			setHintTextColor(Color.parseColor("#6272A4"))
			textSize = 14f
			setTextColor(Color.WHITE)
			setTypeface(Typeface.MONOSPACE)
			setBackgroundColor(Color.parseColor("#1E2430"))
			setPadding(30, 30, 30, 30)
		}
		val llControlRow = LinearLayout(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 0, 40)
			}
			orientation = LinearLayout.HORIZONTAL
		}
		btnCommitDisk = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.5f).apply {
				setMargins(0, 0, 10, 0)
			}
			text = "COMMIT TO DISK"
			textSize = 12f
			setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
			setTextColor(Color.BLACK)
			setBackgroundColor(Color.parseColor("#50FA7B"))	// Matrix Green
		}
		btnFlushDisk = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 0.5f).apply {
				setMargins(10, 0, 0, 0)
			}
			text = "FLUSH STORAGE"
			textSize = 12f
			setTypeface(Typeface.MONOSPACE, Typeface.BOLD)
			setTextColor(Color.WHITE)
			setBackgroundColor(Color.parseColor("#FF5555"))	// System Alert Red
		}
		tvFileLogs = TextView(this).apply {
			text = "LOG: Storage Subsystem Ready."
			textSize = 12f
			setTypeface(Typeface.MONOSPACE)
			setTextColor(Color.parseColor("#8BE9FD"))		// Telemetry Cyan
			setLineSpacing(6f, 1f)
		}
		with(llControlRow) {
			addView(btnCommitDisk)
			addView(btnFlushDisk)
		}
		with(llRoot) {
			addView(tvHeader)
			addView(etInputPayload)
			addView(llControlRow)
			addView(tvFileLogs)
		}
		setContentView(llRoot)

		btnCommitDisk.setOnClickListener {
			serializeInputToSandbox()
		}
		btnFlushDisk.setOnClickListener {
			flushSandboxedFile()
		}
	}
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putString("KEY_INPUT_PAYLOAD", etInputPayload.text.toString())
	}
	override fun onRestoreInstanceState(savedInstanceState: Bundle) {
		super.onRestoreInstanceState(savedInstanceState)
		savedInstanceState.getString("KEY_INPUT_PAYLOAD")?.let { input ->
			etInputPayload.setText(input)
		}
	}
	override fun onResume() {
		super.onResume()
		deserializeStorageToUI()
	}
	private fun serializeInputToSandbox() {
		val rawInput = etInputPayload.text.toString()
		if (rawInput.isBlank()) {
			tvFileLogs.text = "LOG: Operations Rejected. Data payload string cannot be blank."
			return
		}
		tvFileLogs.text = "LOG: Running explicit disk serialization track..."

		Thread {
			try {
				val targetFile = File(filesDir, FILE_NAME)
				FileOutputStream(targetFile).use { fileOutputStream ->
					fileOutputStream.write(rawInput.toByteArray(StandardCharsets.UTF_8))
				}
				hMain.post {
					tvFileLogs.text = "LOG: Succes. Wrote payload stream block cleanly to data disk.\nBytes saved: ${targetFile.length()} B"
					etInputPayload.setText("")
				}
			} catch (e: Exception) {
				hMain.post {
					tvFileLogs.text = "LOG: Error. Serialization transaction failed: ${e.localizedMessage}"
				}
			}
		}.start()
	}
	private fun deserializeStorageToUI() {
		tvFileLogs.text = "LOG: Sweeping internal sandbox files directory..."

		Thread {
			val targetFile = File(filesDir, FILE_NAME)
			if (!targetFile.exists()) {
				hMain.post {
					tvFileLogs.text = "LOG: Sweep complete. No previous configuration data map detected on disk."
				}
				return@Thread
			}
			try {
				val stringBuilder = StringBuilder()
				// Open native InputStream and wrap with bufferedReader for sequential block loading safety
				FileInputStream(targetFile).bufferedReader(StandardCharsets.UTF_8).use { bufferedReader ->
					var currentLine = bufferedReader.readLine()
					while (currentLine != null) {
						stringBuilder.append(currentLine).append("\n")
						currentLine = bufferedReader.readLine()
					}
				}
				hMain.post {
					val loadedContent = stringBuilder.toString().trim()
					tvFileLogs.text = "LOG: Active Payload Extracted From Disk:\n\"$loadedContent\""
				}
			} catch (e: Exception) {
				hMain.post {
					tvFileLogs.text = "LOG: Critical failure analyzing structural file stream: ${e.localizedMessage}"
				}
			}
		}.start()
	}
	private fun flushSandboxedFile() {
		Thread {
			val targetFile = File(filesDir, FILE_NAME)
			if (targetFile.exists()) {
				val wasDeleted = targetFile.delete()
				hMain.post {
					if (wasDeleted) {
						tvFileLogs.text = "LOG: Storage cleared successfully. Sandboxed configuration file destroyed."
					}
					else {
						tvFileLogs.text = "LOG: Deletion transaction flag rejected by Linux filesystem kernel."
					}
				}
			}
			else {
				hMain.post {
					tvFileLogs.text = "LOG: Operations idle. No historical file records exist to destroy."
				}
			}
		}.start()
	}
	override fun onDestroy() {
		super.onDestroy()
		hMain.removeCallbacksAndMessages(null)
	}
}
