/* \uFDFD
 *                   \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *      \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0633\u0651\u0644\u0627\u0645
 *                         \u06F1\u06F4\u06F4\u06F7 \u0631\u062C\u0628 \u06F1\u06F8
 */
package localhost.idroid.salamun

import android.app.Activity
import android.content.res.Resources
import android.util.Log
import android.util.TypedValue
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.Window
import android.view.ViewGroup.LayoutParams
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.TextView

class SalamActivity : Activity() {
	private companion object {
		val TAG = "localhost.idroid.salamun"
	}
	init {
		val metrics = Resources.getSystem().getDisplayMetrics()
		Log.d(TAG, "\uFDFD")
		Log.d(TAG, "================ IDoid Display Metrics ================")
		Log.d(TAG, "Absolute Screen Width (Pixels): " + metrics.widthPixels)
		Log.d(TAG, "Absolute Screen Height (Pixels): " + metrics.heightPixels)
		Log.d(TAG, "DP Scaling Factor (density): " + metrics.density)
		Log.d(TAG, "Text Scaling Factor (scaledDensity): " + metrics.scaledDensity)
		Log.d(TAG, "Screen Density DPI Bucket (densityDpi): " + metrics.densityDpi)
		Log.d(TAG, "Exact Physical Screen DPI (Horizontal X-axis): " + metrics.xdpi)
		Log.d(TAG, "Exact Physical Screen DPI (Vertical Y-axis): " + metrics.ydpi)
		Log.d(TAG, "========================================================")
	}
	override fun onCreate(savedInstanceState: Bundle?) {
		Log.d(TAG, "SalamActivity: onCreate()")
		super.onCreate(savedInstanceState)
		val metrics = Resources.getSystem().getDisplayMetrics()
		requestWindowFeature(Window.FEATURE_NO_TITLE)
		window.setBackgroundDrawable(ColorDrawable(Color.WHITE))
		val root = FrameLayout(this).apply {
			layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
		}
		val marginInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 32f, resources.getDisplayMetrics()).toInt()
		val paddingInPx = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, metrics).toInt()
		val viewer = TextView(this).apply {
			text = "\uFDFD"
			layoutParams = FrameLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT
			).apply {
				setMargins(marginInPx, marginInPx, marginInPx, 0)	// left, top, right, bottom
			}
			setPadding(0, paddingInPx, 0, paddingInPx)	// left, top, right, bottom
			setTextSize(TypedValue.COMPLEX_UNIT_SP, 64f)
			gravity = Gravity.CENTER_HORIZONTAL
			setBackgroundColor(Color.BLACK)
			setTextColor(Color.rgb(0xFF, 0xD7, 0x00))
		}
		setContentView(root.apply { addView(viewer) })
	}
    override protected fun onRestart() {
        Log.d(TAG, "SalamActivity: onRestart()")
        super.onRestart()
    }
    override protected fun onStart() {
        Log.d(TAG, "SalamActivity: onStart()")
        super.onStart()
    }
    override protected fun onResume() {
        Log.d(TAG, "SalamActivity: onResume()")
        super.onResume()
    }
    override protected fun onPause() {
        Log.d(TAG, "SalamActivity: onPause()")
        super.onPause()
    }
    override protected fun onStop() {
        Log.d(TAG, "SalamActivity: onStop()")
        super.onStop()
    }
    override protected fun onDestroy() {
        Log.d(TAG, "SalamActivity: onDestroy()")
        super.onDestroy()
    }
}
