/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0622\u062E\u0631\u0629 \u06F1\u06F1
 */
package localhost.idroid.salamun

import java.io.File
import java.io.FileOutputStream

import android.content.Context
import android.net.Uri
import android.graphics.Bitmap
import android.graphics.Color

object SampleImageProvider {
	/**
	 * Generates a synthetic 3000x2000 JPEG on disk so the app works immediately
	 * even before the user picks an external photo.
	 */
	fun ensureSyntheticSampleFile(context: Context): File {
		val sampleFile = File(context.cacheDir, "synthetic_3000x2000.jpg")
		if (sampleFile.exists() && sampleFile.length() > 0L) {
			return sampleFile
		}
		val width = 3000
		val height = 2000
		val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
		val rowPixels = IntArray(width)
		for (y in 0 until height) {
			val green = (y * 255) / height
			for (x in 0 until width) {
				val red = (x * 255) / width
				val blue = ((x xor y) and 0xFF)
				rowPixels[x] = Color.rgb(red, green, blue)
			}
			bitmap.setPixels(rowPixels, 0, width, 0, y, width, 1)
		}
		FileOutputStream(sampleFile).use { out ->
			bitmap.compress(Bitmap.CompressFormat.JPEG, 90, out)
		}
		bitmap.recycle()
		return sampleFile
	}
	/**
	 * Copies a content:// URI picked via ACTION_OPEN_DOCUMENT into cacheDir
	 * so ExifInterface and BitmapFactory.decodeFile can read it directly.
	 */
	fun copyUriToCache(context: Context, uri: Uri): File? {
		return try {
			val destFile = File(context.cacheDir, "picked_image_${System.currentTimeMillis()}.jpg")
			context.contentResolver.openInputStream(uri)?.use { input ->
				FileOutputStream(destFile).use { output ->
					input.copyTo(output)
				}
			}
			destFile
		} catch (_: Exception) {
			null
		}
	}
}
