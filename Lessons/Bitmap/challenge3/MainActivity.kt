/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0622\u062E\u0631\u0629 \u06F1\u06F3
 */
package localhost.idroid.salamun

import android.util.Log
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import android.widget.HorizontalScrollView
import android.widget.ScrollView
import android.widget.LinearLayout
import android.widget.ImageView
import android.widget.SeekBar
import android.widget.TextView
import android.widget.Button
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface

class MainActivity : Activity() {
	companion object {
		private const val TAG = "localhost.idroid.salamun"
		private const val BITMAP_SIZE = 512
	}
	private lateinit var ivPreview: ImageView
	private lateinit var brightnessLabel: TextView
	private lateinit var contrastLabel: TextView
	private lateinit var tvTelemetry: TextView
	private var baseBitmap: Bitmap? = null
	private var displayedBitmap: Bitmap? = null
	private var currentPattern = PatternGenerator.PatternType.XOR_FRACTAL
	private var currentFilter = PixelFilterEngine.FilterType.ORIGINAL
	private var brightnessOffset = 0
	private var contrastFactor = 1.0f
	private var lastExecutionMs = 0.0
	private var lastExportSummary = "Not exported yet"

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		buildUi()
		regenerateBasePattern(PatternGenerator.PatternType.XOR_FRACTAL)
	}
	private fun buildUi() {
		val density = resources.displayMetrics.density
		fun dp(value: Int): Int = (value * density).toInt()
		val dp10 = dp(10)
		val dp12 = dp(12)
		val dp14 = dp(14)
		val svRoot = ScrollView(this).apply {
			setBackgroundColor(Color.parseColor("#121212"))
		}
		val container = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setPadding(dp14, dp14, dp14, dp14)
		}
		// 1. Pattern Selector Row
		val patternRow = LinearLayout(this).apply {
			orientation = LinearLayout.HORIZONTAL
		}
		PatternGenerator.PatternType.values().forEach { pattern ->
			val btn = Button(this).apply {
				layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
				text = pattern.label
				setOnClickListener { regenerateBasePattern(pattern) }
			}
			patternRow.addView(btn)
		}
		// 2. 512x512 Preview ImageView
		ivPreview = ImageView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(280)).apply {
				topMargin = dp10
				bottomMargin = dp10
			}
			scaleType = ImageView.ScaleType.FIT_CENTER
			setBackgroundColor(Color.parseColor("#1E1E1E"))
		}
		// 3. Horizontal Scrollable Filter Strip
		val filterScroll = HorizontalScrollView(this).apply {
			isHorizontalScrollBarEnabled = false
		}
		val filterStrip = LinearLayout(this).apply {
			orientation = LinearLayout.HORIZONTAL
		}
		PixelFilterEngine.FilterType.values().forEach { filter ->
			val btn = Button(this).apply {
				text = filter.label
				setOnClickListener {
					currentFilter = filter
					applyCurrentPipeline()
				}
			}
			filterStrip.addView(btn)
		}
		// 4. Brightness Slider (-100..+100)
		brightnessLabel = TextView(this).apply {
			text = "Brightness: +0"
			setTextColor(Color.WHITE)
			setPadding(0, dp10, 0, dp(2))
		}
		val brightnessSeekBar = SeekBar(this).apply {
			max = 200
			progress = 100
			setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
				override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
					brightnessOffset = progress - 100
					brightnessLabel.text = String.format("Brightness: %+d", brightnessOffset)
					applyCurrentPipeline()
				}
				override fun onStartTrackingTouch(sb: SeekBar?) {}
				override fun onStopTrackingTouch(sb: SeekBar?) {}
			})
		}
		// 5. Contrast Slider (0.50x..2.00x)
		contrastLabel = TextView(this).apply {
			text = "Contrast: 1.00x"
			setTextColor(Color.WHITE)
			setPadding(0, dp(8), 0, dp(2))
		}
		val contrastSeekBar = SeekBar(this).apply {
			max = 150
			progress = 50	// 0.50 + 0.50 = 1.00x
			setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
				override fun onProgressChanged(sb: SeekBar?, progress: Int, fromUser: Boolean) {
					contrastFactor = 0.5f + (progress / 100f)
					contrastLabel.text = String.format("Contrast: %.2fx", contrastFactor)
					applyCurrentPipeline()
				}
				override fun onStartTrackingTouch(sb: SeekBar?) {}
				override fun onStopTrackingTouch(sb: SeekBar?) {}
			})
		}
		// 6. Compression Export Button
		val btnExport = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				topMargin = dp10
				bottomMargin = dp10
			}
			text = "Export & Compare PNG vs JPEG Sizes"
			setOnClickListener {
				val bmp = displayedBitmap ?: return@setOnClickListener
				val report = BitmapExporter.exportAndCompare(this@MainActivity, bmp)
				lastExportSummary = report.formatSummary()
				updateTelemetry()
			}
		}
		// 7. Benchmark & Export Telemetry Panel
		tvTelemetry = TextView(this).apply {
			setTextColor(Color.parseColor("#FFCC80"))
			typeface = Typeface.MONOSPACE
			textSize = 12f
			setBackgroundColor(Color.parseColor("#1E1E1E"))
			setPadding(dp12, dp12, dp12, dp12)
		}
		filterScroll.addView(filterStrip)
		with(container) {
			addView(patternRow)
			addView(ivPreview)
			addView(filterScroll)
			addView(brightnessLabel)
			addView(brightnessSeekBar)
			addView(contrastLabel)
			addView(contrastSeekBar)
			addView(btnExport)
			addView(tvTelemetry)
		}
		svRoot.addView(container)
		setContentView(svRoot)
	}
	private fun regenerateBasePattern(pattern: PatternGenerator.PatternType) {
		currentPattern = pattern
		val oldBase = baseBitmap
		baseBitmap = PatternGenerator.generate(BITMAP_SIZE, BITMAP_SIZE, pattern)
		applyCurrentPipeline()
		if (oldBase != null && !oldBase.isRecycled) {
			oldBase.recycle()
		}
	}
	private fun applyCurrentPipeline() {
		val base = baseBitmap ?: return
		val result = PixelFilterEngine.process(
			source = base,
			filter = currentFilter,
			brightnessOffset = brightnessOffset,
			contrastFactor = contrastFactor
		)
		lastExecutionMs = result.elapsedMs
		val previousDisplayed = displayedBitmap
		displayedBitmap = result.bitmap
		ivPreview.setImageBitmap(result.bitmap)
		if (previousDisplayed != null && previousDisplayed !== result.bitmap && !previousDisplayed.isRecycled) {
			previousDisplayed.recycle()
		}
		updateTelemetry()
	}
	private fun updateTelemetry() {
		val totalPixels = BITMAP_SIZE * BITMAP_SIZE
		val megaPixelsPerSec = if (lastExecutionMs > 0.0) {
			(totalPixels / 1_000_000.0) / (lastExecutionMs / 1000.0)
		} else {
			0.0
		}
		tvTelemetry.text = buildString {
			appendln("----------------------------------------")
			appendln("Base Pattern   : ${currentPattern.label} (${BITMAP_SIZE}x${BITMAP_SIZE})")
			appendln("Active Filter  : ${currentFilter.label}")
			appendln("Buffer Size    : ${totalPixels} px (1.00 MB ARGB_8888)")
			appendln(
				String.format(
					"CPU Filter Time: %.2f ms (%.1f MPixels/sec)",
					lastExecutionMs,
					megaPixelsPerSec
				)
			)
			appendln("Disk Export   : $lastExportSummary")
		}
	}
	override fun onDestroy() {
		ivPreview.setImageBitmap(null)
		displayedBitmap?.let { if (!it.isRecycled) it.recycle() }
		baseBitmap?.let { if (!it.isRecycled) it.recycle() }
		displayedBitmap = null
		baseBitmap = null
		super.onDestroy()
	}
}
