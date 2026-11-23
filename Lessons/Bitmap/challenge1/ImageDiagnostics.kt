/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0622\u062E\u0631\u0629 \u06F1\u06F1
 */
package localhost.idroid.salamun

import android.graphics.Bitmap

data class ImageDiagnostics(
	val rawWidth: Int,
	val rawHeight: Int,
	val mimeType: String,
	val exifOrientationLabel: String,
	val exifDegrees: Float,
	val sampleSize: Int,
	val configUsed: Bitmap.Config,
	val decodedWidth: Int,
	val decodedHeight: Int,
	val unscaledArgb8888Bytes: Long,
	val actualAllocatedBytes: Int
) {
	val savedPercentage: Double
		get() {
			if (unscaledArgb8888Bytes <= 0L) return 0.0
			val saved = unscaledArgb8888Bytes - actualAllocatedBytes
			return (saved.toDouble() / unscaledArgb8888Bytes.toDouble()) * 100.0
		}
}
