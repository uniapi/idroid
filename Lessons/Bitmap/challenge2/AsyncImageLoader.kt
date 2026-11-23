/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0622\u062E\u0631\u0629 \u06F1\u06F2
 */
package localhost.idroid.salamun

import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import java.util.Collections
import java.io.File

import android.os.Handler
import android.os.Looper
import android.widget.ImageView
import android.graphics.Color

class AsyncImageLoader(
	val memoryCache: BitmapMemoryCache,
	private val onStatsUpdated: () -> Unit
) {
	private val executor: ExecutorService = Executors.newFixedThreadPool(4)
	private val mainHandler = Handler(Looper.getMainLooper())
	// Tracks paths currently being decoded to avoid duplicate disk reads
	private val inFlightPaths = Collections.newSetFromMap(ConcurrentHashMap<String, Boolean>())

	fun loadThumbnail(file: File, targetView: ImageView, targetSizePx: Int) {
		val path = file.absolutePath
		targetView.tag = path
		// 1. Fast path: synchronous LruCache hit
		val cachedBitmap = memoryCache.get(path)
		if (cachedBitmap != null) {
			targetView.setImageBitmap(cachedBitmap)
			onStatsUpdated()
			return
		}
		// 2. Cache miss: clear recycled ImageView with neutral placeholder
		targetView.setImageBitmap(null)
		targetView.setBackgroundColor(Color.parseColor("#2A2A2A"))
		onStatsUpdated()
		if (!inFlightPaths.add(path)) {
			// Already decoding; still schedule a check or let active worker populate cache
		}
		executor.submit {
			try {
				val bitmap = memoryCache.get(path)
					?: ThumbnailDecoder.decodeSampledThumbnail(path, targetSizePx, targetSizePx)?.also {
						memoryCache.put(path, it)
					}
				mainHandler.post {
					// Verify recycled ImageView hasn' been reassigned to a different row/cell
					if (targetView.tag == path && bitmap != null && !bitmap.isRecycled) {
						targetView.setImageBitmap(bitmap)
					}
					onStatsUpdated()
				}
			} finally {
				inFlightPaths.remove(path)
			}
		}
	}
	fun shutdown() {
		executor.shutdownNow()
		memoryCache.evictAll()
	}
}
