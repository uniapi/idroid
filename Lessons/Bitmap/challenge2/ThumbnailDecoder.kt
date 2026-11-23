/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0622\u062E\u0631\u0629 \u06F1\u06F2
 */
package localhost.idroid.salamun

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface

object ThumbnailDecoder {
	fun decodeSampledThumbnail(filePath: String, reqWidth: Int, reqHeight: Int): Bitmap? {
		val options = BitmapFactory.Options().apply {
			inJustDecodeBounds = true
		}
		BitmapFactory.decodeFile(filePath, options)
		if (options.outWidth <= 0 || options.outHeight <= 0) return null
		options.apply {
			inSampleSize = calculateInSampleSize(outWidth, outHeight, reqWidth, reqHeight)
			inJustDecodeBounds = false
			inPreferredConfig = Bitmap.Config.RGB_565	// Thumbnail don't need alpha; cuts RAM by 50%
		}
		val decoded = BitmapFactory.decodeFile(filePath, options) ?: return null
		return applyExifOrientation(filePath, decoded)
	}
	private fun calculateInSampleSize(rawWidth: Int, rawHeight: Int, reqWidth: Int, reqHeight: Int): Int {
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
	private fun applyExifOrientation(filePath: String, bitmap: Bitmap): Bitmap {
		val degrees = try {
			val exif = ExifInterface(filePath)
			when (exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL)) {
				ExifInterface.ORIENTATION_ROTATE_90 -> 90f
				ExifInterface.ORIENTATION_ROTATE_180 -> 180f
				ExifInterface.ORIENTATION_ROTATE_270 -> 270f
				else -> 0f
			}
		} catch (_: Exception) {
			0f
		}
		if (degrees == 0f) return bitmap
		val matrix = Matrix().apply { postRotate(degrees) }
		val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
		if (rotated !== bitmap) {
			bitmap.recycle()
		}
		return rotated
	}
}
