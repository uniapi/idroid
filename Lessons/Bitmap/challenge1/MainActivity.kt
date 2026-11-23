/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0622\u062E\u0631\u0629 \u06F1\u06F1
 */
package localhost.idroid.salamun

import java.io.File

import android.util.Log
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.Switch
import android.widget.TextView
import android.widget.ImageView
import android.widget.Button
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.Typeface

class MainActivity : Activity() {
	companion object {
		private const val TAG = "localhost.idroid.salamun"
		private const val REQUEST_PICK_IMAGE = 1001
	}
	private lateinit var ivPreview: ImageView
	private lateinit var tvTelemetry: TextView
	private lateinit var rgb565Switch: Switch
	private var currentImageFile: File? = null
	private var currentBitmap: Bitmap? = null
	private var targetViewportPx: Int = 256

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		buildUi()
		currentImageFile = SampleImageProvider.ensureSyntheticSampleFile(this)
		refreshPreview()
	}
	private fun buildUi() {
		val density = resources.displayMetrics.density
		fun dp(value: Int): Int = (value * density).toInt()
		val dp12 = dp(12)
		val dp16 = dp(16)
		val svRoot = ScrollView(this).apply {
			setBackgroundColor(Color.parseColor("#121212"))
		}
		val container = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setPadding(dp16, dp16, dp16, dp16)
		}
		// Top File Picker Button
		val btnPick = Button(this).apply {
			text = "Pick Image File (ACTION_OPEN_DOCUMENT)"
			setOnClickListener { openDocumentPicker() }
		}
		ivPreview = ImageView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, dp(260)).apply {
				topMargin = dp12
				bottomMargin = dp12
			}
			scaleType = ImageView.ScaleType.FIT_CENTER
			setBackgroundColor(Color.parseColor("#1E1E1E"))
		}
		val tvSizeLabel = TextView(this).apply {
			text = "Target Viewport Box:"
			setTextColor(Color.WHITE)
			setTypeface(null, Typeface.BOLD)
		}
		val sizeRow = LinearLayout(this).apply {
			orientation = LinearLayout.HORIZONTAL
			setGravity(Gravity.CENTER_VERTICAL)
		}
		listOf(128, 256, 512, 1024).forEach { size ->
			val btn = Button(this).apply {
				layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
				text = "${size}px"
				setOnClickListener {
					targetViewportPx = size
					refreshPreview()
				}
			}
			sizeRow.addView(btn)
		}
		// RGB_565 Toggle Switch
		rgb565Switch = Switch(this).apply {
			text = "Use RGB_565 (16-bit, No Alpha)"
			setTextColor(Color.WHITE)
			setPadding(0, dp12, 0, dp12)
			setOnCheckedChangeListener { _, _ -> refreshPreview() }
		}
		// Telemetry Output Panel
		tvTelemetry = TextView(this).apply {
			setTextColor(Color.parseColor("#A5D6A7"))
			typeface = Typeface.MONOSPACE
			textSize = 13f
			setBackgroundColor(Color.parseColor("#1E1E1E"))
			setPadding(dp12, dp12, dp12, dp12)
		}
		with(container) {
			addView(btnPick)
			addView(ivPreview)
			addView(tvSizeLabel)
			addView(sizeRow)
			addView(rgb565Switch)
			addView(tvTelemetry)
		}
		svRoot.addView(container)
		setContentView(svRoot)
	}
	private fun openDocumentPicker() {
		val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
			addCategory(Intent.CATEGORY_OPENABLE)
			type = "image/*"	/**/
		}
		startActivityForResult(intent, REQUEST_PICK_IMAGE)
	}
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		if (requestCode == REQUEST_PICK_IMAGE && resultCode == RESULT_OK) {
			val uri = data?.data ?: return
			val copiedFile = SampleImageProvider.copyUriToCache(this, uri)
			if (copiedFile != null) {
				currentImageFile = copiedFile
				refreshPreview()
			}
		}
	}
	private fun refreshPreview() {
		val file = currentImageFile ?: return
		val result = BitmapInspector.inspectAndDecode(
			filePath = file.absolutePath,
			reqWidth = targetViewportPx,
			reqHeight = targetViewportPx,
			useRgb565 = rgb565Switch.isChecked
		) ?: return
		// Swap bitmap into ImageView BEFORE recycling the old one
		val previousBitmap = currentBitmap
		currentBitmap = result.bitmap
		ivPreview.setImageBitmap(result.bitmap)
		if (previousBitmap != null && previousBitmap !== result.bitmap && !previousBitmap.isRecycled) {
			previousBitmap.recycle()
		}
		renderTelemetry(result.diagnostics)
	}
	private fun renderTelemetry(d: ImageDiagnostics) {
		val savedPct = String.format("%.2f%%", d.savedPercentage)
		tvTelemetry.text = """
			DIAGNOSTICS TELEMETRY
			----------------------------------------
			Target Box        : $targetViewportPx x $targetViewportPx px
			Raw Dimensions    : ${d.rawWidth} x ${d.rawHeight} (${d.mimeType})
			EXIF Orientation  : ${d.exifOrientationLabel}
			Unscaled ARGB8888 : ${BitmapInspector.formatBytes(d.unscaledArgb8888Bytes)}
			Computed Sample   : inSampleSize = ${d.sampleSize}
			Decoded Size      : ${d.decodedWidth} x ${d.decodedHeight} (${d.configUsed})
			Actual Allocation : ${BitmapInspector.formatBytes(d.actualAllocatedBytes.toLong())}
			Memory Saved      : $savedPct
		""".trimIndent()
	}
	override fun onDestroy() {
		ivPreview.setImageBitmap(null)
		currentBitmap?.let {
			if (!it.isRecycled) it.recycle()
		}
		currentBitmap = null
		super.onDestroy()
	}
}
