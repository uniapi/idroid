/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0622\u062E\u0631\u0629 \u06F1\u06F3
 */
package localhost.idroid.salamun

import java.io.File
import java.io.FileOutputStream

import android.content.Context
import android.graphics.Bitmap

object BitmapExporter {
	data class ExportReport(
		val pngBytes: Long,
		val jpegHighBytes: Long,
		val jpegLowBytes: Long,
		val outputDirPath: String
	) {
		fun formatSummary(): String {
			return String.format(
				"PNG(Lossless): %.1fKB | JPG(85): %.1fKB | JPG(25): %.1fKB",
				pngBytes / 1024.0,
				jpegHighBytes / 1024.0,
				jpegLowBytes / 1024.0
			)
		}
	}
	fun exportAndCompare(context: Context, bitmap: Bitmap): ExportReport {
		val exportDir = File(context.cacheDir, "filter_exports").apply { mkdirs() }
		val pngFile = File(exportDir, "export_lossless.png")
		val jpgHighFile = File(exportDir, "export_q85.jpg")
		val jpgLowFile = File(exportDir, "export_q25.jpg")
		FileOutputStream(pngFile).use { out ->
			bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
		}
		FileOutputStream(jpgHighFile).use { out ->
			bitmap.compress(Bitmap.CompressFormat.JPEG, 85, out)
		}
		FileOutputStream(jpgLowFile).use { out ->
			bitmap.compress(Bitmap.CompressFormat.JPEG, 25, out)
		}
		return ExportReport(
			pngBytes = pngFile.length(),
			jpegHighBytes = jpgHighFile.length(),
			jpegLowBytes = jpgLowFile.length(),
			outputDirPath = exportDir.absolutePath
		)
	}
}
