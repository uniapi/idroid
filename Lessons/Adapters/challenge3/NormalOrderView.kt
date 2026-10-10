/* \uFDFD
 *					 \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *		\u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u0631\u0628\u064A\u0639 \u0671\u0644\u0622\u062E\u0631 \u06F2\u06F7
 */
package localhost.idroid.salamun

import android.util.Log
import android.content.Context
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import android.graphics.Color
import android.graphics.Typeface

class NormalOrderView(context: Context) : LinearLayout(context) {
	val tvInfo: TextView
	val tvProgress: TextView
	val btnStep: Button

	init {
		val pod12 = dpToPx(context, 12f)
		val pod14 = dpToPx(context, 14f)
		val pod38 = dpToPx(context, 38f)
		val pod75 = dpToPx(context, 75f)

		layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
		orientation = HORIZONTAL
		setGravity(Gravity.CENTER_VERTICAL)
		setPadding(pod14, pod12, pod14, pod12)
		setBackgroundColor(Color.WHITE)

		tvInfo = TextView(context).apply {
			layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
			textSize = 15f
			setTextColor(Color.BLACK)
		}
		tvProgress = TextView(context).apply {
			layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, pod12, 0)
			}
			textSize = 14f
			typeface = Typeface.DEFAULT_BOLD
			setTextColor(Color.parseColor("#1976D2"))	// Blue color
		}
		btnStep = Button(context).apply {
			layoutParams = LayoutParams(pod75, pod38)
			text = "+25%"
			textSize = 12f
			setPadding(0, 0, 0, 0)
		}
		addView(tvInfo)
		addView(tvProgress)
		addView(btnStep)
	}
}
