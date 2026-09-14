/* \uFDFD
 *                   \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *      \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0633\u0651\u0644\u0627\u0645
 *                         \u06F1\u06F4\u06F4\u06F7 \u0631\u062C\u0628 \u06F1\u06F8
 */
package localhost.idroid.salamun

import android.util.Log
import android.app.Activity
import android.os.Bundle
import android.view.Window
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.graphics.Color

class SalamActivity : Activity() {
	private companion object {
		val TAG = "localhost.idroid.salamun"
	}
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		requestWindowFeature(Window.FEATURE_NO_TITLE)
		val layout = LinearLayout(this).apply {
			layoutParams = LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT
			)
			orientation = LinearLayout.VERTICAL
		}
		val viewer = TextView(this).apply {
			text = "\uFDFD"
			layoutParams = LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT
			).apply { gravity = Gravity.CENTER }
			textSize = 64f
			setTextColor(Color.rgb(0xFF, 0xD7, 0x00))
		}
		with(layout) {
			addView(viewer)
		}
		setContentView(layout)
	}
}
