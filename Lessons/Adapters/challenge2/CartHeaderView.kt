/* \uFDFD
 *					 \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *		\u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u0631\u0628\u064A\u0639 \u0671\u0644\u0622\u062E\u0631 \u06F2\u06F6
 */
package localhost.idroid.salamun

import android.util.Log
import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Gravity
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import android.graphics.Color
import android.graphics.Typeface

class CartHeaderView(context: Context) : LinearLayout(context) {
	val tvTitle: TextView

	init {
		val pod10 = dpToPx(context, 10f)
		val pod16 = dpToPx(context, 16f)

		layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
		orientation = HORIZONTAL
		setBackgroundColor(Color.parseColor("#F5F5F5"))
		setPadding(pod16, pod10, pod16, pod10)

		tvTitle = TextView(context).apply {
			layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			textSize = 14f
			setTextColor(Color.parseColor("#757575"))
			typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
		}
		addView(tvTitle)
	}
}
