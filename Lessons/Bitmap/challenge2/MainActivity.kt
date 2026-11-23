/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0622\u062E\u0631\u0629 \u06F1\u06F3
 */
package localhost.idroid.salamun

import java.util.concurrent.Executors

import android.util.Log
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import android.widget.GridView
import android.widget.TextView
import android.widget.Button
import android.graphics.Color
import android.graphics.Typeface

class MainActivity : Activity() {
	companion object {
		private const val TAG = "localhost.idroid.salamun"
		private const val GRID_COLUMNS = 3
	}
	private lateinit var tvTelemetry: TextView
	private lateinit var gridView: GridView
	private lateinit var memoryCache: BitmapMemoryCache
	private lateinit var imageLoader: AsyncImageLoader
	private val scanExecutor = Executors.newSingleThreadExecutor()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		memoryCache = BitmapMemoryCache()
		imageLoader = AsyncImageLoader(memoryCache) {
			updateTelemetryBanner()
		}
		buildUi()
		updateTelemetryBanner()
		loadGalleryAsync()
	}
	private fun buildUi() {
		val density = resources.displayMetrics.density
		fun dp(value: Int): Int = (value * density).toInt()
		val dp4 = dp(4)
		val dp8 = dp(8)
		val dp12 = dp(12)
		val llRoot = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setBackgroundColor(Color.parseColor("#121212"))
		}
		val header = LinearLayout(this).apply {
			orientation = LinearLayout.VERTICAL
			setBackgroundColor(Color.parseColor("#1E1E1E"))
			setPadding(dp12, dp12, dp12, dp12)
		}
		tvTelemetry = TextView(this).apply {
			setTextColor(Color.parseColor("#80CBC4"))
			typeface = Typeface.MONOSPACE
			textSize = 12f
		}
		val buttonRow = LinearLayout(this).apply {
			orientation = LinearLayout.HORIZONTAL
			setPadding(0, dp8, 0, 0)
		}
		val btnEvict = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
			text = "Evict Cache"
			setOnClickListener {
				memoryCache.evictAll()
				updateTelemetryBanner()
				gridView.invalidateViews()
			}
		}
		val btnReload = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
			text = "Reload Gallery"
			setOnClickListener { loadGalleryAsync() }
		}
		gridView = GridView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f)
			numColumns = GRID_COLUMNS
			horizontalSpacing = dp4
			verticalSpacing = dp4
			setPadding(dp4, dp4, dp4, dp4)
			setClipToPadding(false)
		}
		with(buttonRow) {
			addView(btnEvict)
			addView(btnReload)
		}
		with(header) {
			addView(tvTelemetry)
			addView(buttonRow)
		}
		with(llRoot) {
			addView(header)
			addView(gridView)
		}
		setContentView(llRoot)
	}
	private fun loadGalleryAsync() {
		val screenWidth = resources.displayMetrics.widthPixels
		val cellSizePx = screenWidth / GRID_COLUMNS
		scanExecutor.submit {
			val files = GalleryRepository.loadImageFiles(this)
			runOnUiThread {
				gridView.adapter = ImageGridAdapter(this, files, cellSizePx, imageLoader)
				updateTelemetryBanner()
			}
		}
	}
	private fun updateTelemetryBanner() {
		val usedMb = memoryCache.currentSizeKb() / 1024.0
		val maxMb = memoryCache.maxCapacityKb / 1024.0
		tvTelemetry.text = String.format(
			"LruCache: %.2fMB / %.2fMB | Hits: %d | Misses: %d",
			usedMb,
			maxMb,
			memoryCache.hits(),
			memoryCache.misses()
		)
	}
	override fun onDestroy() {
		scanExecutor.shutdownNow()
		imageLoader.shutdown()
		super.onDestroy()
	}
}
