/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0622\u062E\u0631\u0629 \u06F1\u06F2
 */
package localhost.idroid.salamun

import java.util.concurrent.atomic.AtomicInteger

import android.util.LruCache
import android.graphics.Bitmap

class BitmapMemoryCache {
	// Alocate 1/8th of maximum available VM heap in kilobytes
	val maxCapacityKb: Int = (Runtime.getRuntime().maxMemory() / 1024L / 8L).toInt()
	private val hitCount = AtomicInteger(0)
	private val missCount = AtomicInteger(0)

	private val cache = object : LruCache<String, Bitmap>(maxCapacityKb) {
		override fun sizeOf(key: String, bitmap: Bitmap): Int {
			return (bitmap.allocationByteCount / 1024).coerceAtLeast(1)
		}
	}
	fun get(key: String): Bitmap? {
		val bitmap = cache.get(key)
		if (bitmap != null && !bitmap.isRecycled) {
			hitCount.incrementAndGet()
			return bitmap
		}
		missCount.incrementAndGet()
		return null
	}
	fun put(key: String, bitmap: Bitmap) {
		if (cache.get(key) == null && !bitmap.isRecycled) {
			cache.put(key, bitmap)
		}
	}
	fun evictAll() {
		cache.evictAll()
		hitCount.set(0)
		missCount.set(0)
	}
	fun currentSizeKb(): Int = cache.size()
	fun hits(): Int = hitCount.get()
	fun misses(): Int = missCount.get()
}
