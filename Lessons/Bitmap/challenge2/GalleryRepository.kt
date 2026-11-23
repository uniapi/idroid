/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0622\u062E\u0631\u0629 \u06F1\u06F2
 */
package localhost.idroid.salamun

import java.io.File
import java.io.FileOutputStream

import android.content.Context
import android.provider.MediaStore
import android.graphics.Bitmap
import android.graphics.Color

object GalleryRepository {
	/**
	 * Queries MediaStore for device images. If none exist (e.g. fresh emulator),
	 * generates 45 high-resolution synthetic JPEGs in cacheDir so the grid works out of the box.
	 */
	fun loadImageFiles(context: Context): Array<File> {
		val deviceFiles = queryMediaStoreImages(context)
		if (deviceFiles.isNotEmpty()) {
			return deviceFiles.toTypedArray()
		}
		return ensureSyntheticGallery(context)
	}
	private fun queryMediaStoreImages(context: Context): List<File> {
		val result = mutableListOf<File>()
		val projection = arrayOf(MediaStore.Images.Media.DATA)
		val sortOrder = "${MediaStore.Images.Media.DATE_ADDED} DESC"
		try {
			context.contentResolver.query(
				MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
				projection,
				null,
				null,
				sortOrder
			)?.use { cursor ->
				val dataColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
				while (cursor.moveToNext()) {
					val path = cursor.getString(dataColumn) ?: continue
					val file = File(path)
					if (file.exists() && file.canRead() && file.length() > 0L) {
						result.add(file)
					}
				}
			}
		} catch (_: SecurityException) {
			// Permission denied -> fall back to synthetic dataset
		}
		return result
	}
	private fun ensureSyntheticGallery(context: Context): Array<File> {
		val galleryDir = File(context.cacheDir, "synthetic_gallery").apply { mkdirs() }
		val existing = galleryDir.listFiles()?.filter { it.extension == "jpg" }?.sortedBy { it.name }
		if (!existing.isNullOrEmpty() && existing.size >= 45) {
			return existing.toTypedArray()
		}
		val width = 1200
		val height = 1200
		val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
		val rowPixels = IntArray(width)
		val files = Array(45) { index ->
			val file = File(galleryDir, String.format("sample_%02d.jpg", index + 1))
			val hueShift = (index * 37) % 255
			for (y in 0 until height) {
				val g = ((y * 255) / height + hueShift) and 0xFF
				for (x in 0 until width) {
					val r = ((x * 255) / width + index * 13) and 0xFF
					val b = ((x xor y) + index * 29) and 0xFF
					rowPixels[x] = Color.rgb(r, g, b)
				}
				bitmap.setPixels(rowPixels, 0, width, 0, y, width, 1)
			}
			FileOutputStream(file).use { out ->
				bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
			}
			file
		}
		bitmap.recycle()
		return files
	}
}
