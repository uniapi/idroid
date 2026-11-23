/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0622\u062E\u0631\u0629 \u06F1\u06F1
 */
package localhost.idroid.salamun

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface

object BitmapInspector {
	data class DecodeResult(
		val bitmap: Bitmap,
		val diagnostics: ImageDiagnostics
	)
	fun inspectAndDecode(filePath: String, reqWidth: Int, reqHeight: Int, useRgb565: Boolean): DecodeResult? {
		// Pass 1: Zero-allocation metadata bounds check
		val boundsOptions = BitmapFactory.Options().apply {
			inJustDecodeBounds = true
		}
		BitmapFactory.decodeFile(filePath, boundsOptions)
		val rawWidth = boundsOptions.outWidth
		val rawHeight = boundsOptions.outHeight
		if (rawWidth <= 0 || rawHeight <= 0) return null
		val mimeType = boundsOptions.outMimeType ?: "unknown"
		val unscaledBytes = rawWidth.toLong() * rawHeight.toLong() * 4L
		// Inspect EXIF rotation metadata
		val (exifLabel, exifDegrees) = readExifOrientation(filePath)
		// Compute power-of-2 subsampling factor
		val sampleSize = calculateInSampleSize(rawWidth, rawHeight, reqWidth, reqHeight)
		val targetConfig = if (useRgb565) Bitmap.Config.RGB_565 else Bitmap.Config.ARGB_8888
		// Pass 2: Decode subsampled pixel buffer
		val decodeOptions = BitmapFactory.Options().apply {
			inJustDecodeBounds = false
			inSampleSize = sampleSize
			inPreferredConfig = targetConfig
		}
		val rawBitmap = BitmapFactory.decodeFile(filePath, decodeOptions) ?: return null
		// Apply EXIF rotation if needed and recycle intermediate bitmap
		val finalBitmap = applyExifRotation(rawBitmap, exifDegrees)
		val diagnostics = ImageDiagnostics(
			rawWidth = rawWidth,
			rawHeight = rawHeight,
			mimeType = mimeType,
			exifOrientationLabel = exifLabel,
			exifDegrees = exifDegrees,
			sampleSize = sampleSize,
			configUsed = finalBitmap.config ?: targetConfig,
			decodedWidth = finalBitmap.width,
			decodedHeight = finalBitmap.height,
			unscaledArgb8888Bytes = unscaledBytes,
			actualAllocatedBytes = finalBitmap.allocationByteCount
		)
		return DecodeResult(finalBitmap, diagnostics)
	}
	fun calculateInSampleSize(rawWidth: Int, rawHeight: Int, reqWidth: Int, reqHeight: Int): Int {
		var inSampleSize = 1
		if (rawHeight > reqHeight || rawWidth > reqWidth) {
			val halfHeight = rawHeight / 2
			val halfWidth = rawWidth / 2
			while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
				inSampleSize *= 2
			}
		}
		return inSampleSize
	}
	private fun readExifOrientation(filePath: String): Pair<String, Float> {
		return try {
			val exif = ExifInterface(filePath)
			when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
				ExifInterface.ORIENTATION_ROTATE_90 -> "ROTATE_90 (Corrected)" to 90f
				ExifInterface.ORIENTATION_ROTATE_180 -> "ROTATE_180 (Corrected)" to 180f
				ExifInterface.ORIENTATION_ROTATE_270 -> "ROTATE_270 (Corrected)" to 270f
				else -> "NORMAL (0°)" to 0f
			}
		} catch (_: Exception) {
			"UNAVAILABLE" to 0f
		}
	}
	private fun applyExifRotation(source: Bitmap, degrees: Float): Bitmap {
		if (degrees == 0f) return source
		val matrix = Matrix().apply { postRotate(degrees) }
		val rotated = Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
		if (rotated !== source) {
			source.recycle()
		}
		return rotated
	}
	fun formatBytes(bytes: Long): String {
		return when {
			bytes >= 1024 * 1024 -> String.format("%.2f MB", bytes / (1024.0 * 1024.0))
			bytes >= 1024 -> String.format("%.2f KB", bytes / 1024.0)
			else -> "$bytes B"
		}
	}
}
