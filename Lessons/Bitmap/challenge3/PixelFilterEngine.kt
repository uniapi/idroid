/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0622\u062E\u0631\u0629 \u06F1\u06F3
 */
package localhost.idroid.salamun

import android.graphics.Bitmap

object PixelFilterEngine {
	enum class FilterType(val label: String) {
		ORIGINAL("Reset"),
		GRAYSCALE("Grayscale"),
		INVERT("Invert"),
		RED_ONLY("Red Channel"),
		GREEN_ONLY("Green Channel"),
		BLUE_ONLY("Blue Channel"),
		BOX_BLUR("3x3 Box Blur"),
		EDGE_DETECT("3x3 Edge Detect")
	}
	data class FilterResult(
		val bitmap: Bitmap,
		val elapsedMs: Double
	)
	fun process(
		source: Bitmap,
		filter: FilterType,
		brightnessOffset: Int,	// -100..+100
		contrastFactor: Float	// 0.5f..2.0f
	): FilterResult {
		val width = source.width
		val height = source.height
		val totalPixels = width * height
		val srcPixels = IntArray(totalPixels)
		source.getPixels(srcPixels, 0, width, 0, 0, width, height)
		val startNs = System.nanoTime()
		// 1. Apply primary filter pass
		val filteredPixels = when (filter) {
			FilterType.ORIGINAL -> srcPixels
			FilterType.GRAYSCALE -> applyGrayscale(srcPixels)
			FilterType.INVERT -> applyInvert(srcPixels)
			FilterType.RED_ONLY -> applyChannelMask(srcPixels, 0x00FF0000)
			FilterType.GREEN_ONLY -> applyChannelMask(srcPixels, 0x0000FF00)
			FilterType.BLUE_ONLY -> applyChannelMask(srcPixels, 0x000000FF)
			FilterType.BOX_BLUR -> apply3x3Convolution(
				src = srcPixels,
				width = width,
				height = height,
				kernel = intArrayOf(
					1, 1, 1,
					1, 1, 1,
					1, 1, 1
				),
				divisor = 9,
				bias = 0
			)
			FilterType.EDGE_DETECT -> apply3x3Convolution(
				src = srcPixels,
				width = width,
				height = height,
				kernel = intArrayOf(
					-1, -1, -1,
					-1,  8, -1,
					-1, -1, -1
				),
				divisor = 1,
				bias = 0
			)
		}
		// 2. Apply Brightness & Contrast adjustments if sliders are non-neutral
		val finalPixels = if (brightnessOffset != 0 || contrastFactor != 1.0f) {
			applyBrightnessContrast(filteredPixels, brightnessOffset, contrastFactor)
		} else {
			filteredPixels
		}
		val elapsedMs = (System.nanoTime() - startNs) / 1_000_000.0
		val outBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
		outBitmap.setPixels(finalPixels, 0, width, 0, 0, width, height)
		return FilterResult(outBitmap, elapsedMs)
	}
	private fun applyGrayscale(src: IntArray): IntArray {
		val out = IntArray(src.size)
		for (i in src.indices) {
			val c = src[i]
			val a = (c shr 24) and 0xFF
			val r = (c shr 16) and 0xFF
			val g = (c shr 8) and 0xFF
			val b = c and 0xFF
			val luma = (0.299f * r + 0.587f * g + 0.114f * b).toInt().coerceIn(0, 255)
			out[i] = (a shl 24) or (luma shl 16) or (luma shl 8) or luma
		}
		return out
	}
	private fun applyInvert(src: IntArray): IntArray {
		val out = IntArray(src.size)
		for (i in src.indices) {
			// XOR lower 24 RGB bits with 0xFFFFFF while keeping Alpha bits untouched
			out[i] = src[i] xor 0x00FFFFFF
		}
		return out
	}
	private fun applyChannelMask(src: IntArray, rgbMask: Int): IntArray {
		val out = IntArray(src.size)
		val fullMask = 0xFF000000.toInt() or rgbMask
		for (i in src.indices) {
			out[i] = src[i] and fullMask
		}
		return out
	}
	private fun applyBrightnessContrast(
		src: IntArray,
		brightness: Int,
		contrast: Float
	): IntArray {
		val out = IntArray(src.size)
		for (i in src.indices) {
			val c = src[i]
			val a = (c shr 24) and 0xFF
			var r = (c shr 16) and 0xFF
			var g = (c shr 8) and 0xFF
			var b = c and 0xFF
			r = (((r - 128) * contrast) + 128 + brightness).toInt().coerceIn(0, 255)
			g = (((g - 128) * contrast) + 128 + brightness).toInt().coerceIn(0, 255)
			b = (((b - 128) * contrast) + 128 + brightness).toInt().coerceIn(0, 255)
			out[i] = (a shl 24) or (r shl 16) or (g shl 8) or b
		}
		return out
	}
	private fun apply3x3Convolution(
		src: IntArray,
		width: Int,
		height: Int,
		kernel: IntArray,
		divisor: Int,
		bias: Int
	): IntArray {
		val out = IntArray(src.size)
		for (y in 0 until height) {
			val rowOffset = y * width
			for (x in 0 until width) {
				var sumR = 0
				var sumG = 0
				var sumB = 0
				var kIdx = 0
				for (ky in -1..1) {
					val sampleY = (y + ky).coerceIn(0, height - 1)
					val sampleRowOffset = sampleY * width
					for (kx in -1..1) {
						val sampleX = (x + kx).coerceIn(0, width -1)
						val pixel = src[sampleRowOffset + sampleX]
						val weight = kernel[kIdx++]
						sumR += ((pixel shr 16) and 0xFF) * weight
						sumG += ((pixel shr 8) and 0xFF) * weight
						sumB += (pixel and 0xFF) * weight
					}
				}
				val alpha = (src[rowOffset + x] shr 24) and 0xFF
				val r = (sumR / divisor + bias).coerceIn(0, 255)
				val g = (sumG / divisor + bias).coerceIn(0, 255)
				val b = (sumB / divisor + bias).coerceIn(0, 255)
				out[rowOffset + x] = (alpha shl 24) or (r shl 16) or (g shl 8) or b
			}
		}
		return out
	}
}
