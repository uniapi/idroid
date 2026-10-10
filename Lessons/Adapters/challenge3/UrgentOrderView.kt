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
import android.graphics.drawable.GradientDrawable
import android.graphics.Typeface

class UrgentOrderView(context: Context) : LinearLayout(context) {
	val tvInfo: TextView
	val btnComplete: TextView

	init {
		val pod2 = dpToPx(context, 2f)
		val pod4 = dpToPx(context, 4f)
		val pod12 = dpToPx(context, 12f)
		val pod14 = dpToPx(context, 14f)
		val pod38 = dpToPx(context, 38f)
		val pod90 = dpToPx(context, 90f)

		layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
		orientation = HORIZONTAL
		setGravity(Gravity.CENTER_VERTICAL)
		setPadding(pod14, pod12, pod14, pod12)
		// Half-transparent red background
		setBackgroundDrawable(GradientDrawable().apply {
			setColor(Color.parseColor("#FFEBEE"))		// Light red
			setStroke(pod2, Color.parseColor("#EF5350"))// Red frame
			setCornerRadius(pod4.toFloat())
		})
		tvInfo = TextView(context).apply {
			layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
			textSize = 15f
			setTextColor(Color.parseColor("#C62828"))	// Dark red text
			typeface = Typeface.DEFAULT_BOLD
		}
		btnComplete = Button(context).apply {
			layoutParams = LayoutParams(pod90, pod38)
			text = "Done"
			textSize = 12f
			setTextColor(Color.WHITE)
			setBackgroundColor(Color.parseColor("#D32F2F"))	// Bright red button background
			setPadding(0, 0, 0, 0)
		}
		addView(tvInfo)
		addView(btnComplete)
	}
}
