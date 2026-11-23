/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0622\u062E\u0631\u0629 \u06F1\u06F3
 */
package localhost.idroid.salamun

import android.graphics.Bitmap
import kotlin.math.atan2
import kotlin.math.hypot

object PatternGenerator {
	enum class PatternType(val label: String) {
		XOR_FRACTAL("XOR Fractal"),
		RADIAL_GRID("Radial Grid")
	}
	fun generate(width: Int, height: Int, type: PatternType): Bitmap {
		val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
		val pixels = IntArray(width * height)
		when (type) {
			PatternType.XOR_FRACTAL -> fillXorFractal(pixels, width, height)
			PatternType.RADIAL_GRID -> fillRadialGrid(pixels, width, height)
		}
		bitmap.setPixels(pixels, 0, width, 0, 0, width, height)
		return bitmap
	}
	private fun fillXorFractal(pixels: IntArray, width: Int, height: Int) {
		var idx = 0
		for (y in 0 until height) {
			for (x in 0 until width) {
				val r = (x xor y) and 0xFF
				val g = ((x * 2) xor y) and 0xFF
				val b = (x xor (y * 2)) and 0xFF
				pixels[idx++] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
			}
		}
	}
	private fun fillRadialGrid(pixels: IntArray, width: Int, height: Int) {
		val cx = width / 2.0
		val cy = height / 2.0
		val maxDist= hypot(cx, cy)
		var idx = 0
		for (y in 0 until height) {
			val dy = y - cy
			val checkerY = (y / 32) and 1
			for (x in 0 until width) {
				val dx = x - cx
				val distRatio = (hypot(dx, dy) / maxDist).coerceIn(0.0, 1.0)
				val angleNorm = (atan2(dy, dx) + Math.PI)/ (2.0 * Math.PI)
				val checker = ((x / 32) and 1) xor checkerY
				val baseBoost = if (checker == 1) 45 else 0
				val r = ((angleNorm * 210.0) + baseBoost).toInt().coerceIn(0, 255)
				val g = (((1.0 - distRatio) * 210.0) + baseBoost).toInt().coerceIn(0, 255)
				val b = ((distRatio * 210.0) + baseBoost).toInt().coerceIn(0, 255)
				pixels[idx++] = (0xFF shl 24) or (r shl 16) or (g shl 8) or b
			}
		}
	}
}
