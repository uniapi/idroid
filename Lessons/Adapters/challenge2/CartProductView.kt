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

class CartProductView(context: Context) : LinearLayout(context) {
	val tvName: TextView
	val tvPrice: TextView
	val btnMinus: Button
	val tvQuantity: TextView
	val btnPlus: Button
	val btnDelete: Button

	init {
		val pod4 = dpToPx(context, 4f)
		val pod12 = dpToPx(context, 12f)
		val pod16 = dpToPx(context, 16f)
		val pod36 = dpToPx(context, 36f)

		layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
		orientation = HORIZONTAL
		setGravity(Gravity.CENTER_HORIZONTAL)
		setPadding(pod16, pod12, pod16, pod12)
		setBackgroundColor(Color.WHITE)

		val infoContainer = LinearLayout(context).apply {
			layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
			orientation = VERTICAL
		}
		tvName = TextView(context).apply {
			layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			textSize = 18f
			setTextColor(Color.BLACK)
		}
		tvPrice = TextView(context).apply {
			layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, pod4, 0, 0)
			}
			textSize = 14f
			setTextColor(Color.parseColor("#4CAF50"))
		}
		with(infoContainer) {
			addView(tvName)
			addView(tvPrice)
		}
		btnMinus = Button(context).apply {
			layoutParams = LayoutParams(pod36, pod36)
			text = "-"
			setPadding(0, 0, 0, 0)
		}
		tvQuantity = TextView(context).apply {
			layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(pod12, 0, pod12, 0)
			}
			textSize = 16f
			gravity = Gravity.CENTER
		}
		btnPlus = Button(context).apply {
			layoutParams = LayoutParams(pod36, pod36)
			text = "+"
			setPadding(0, 0, 0, 0)
		}
		btnDelete = Button(context).apply {
			layoutParams = LayoutParams(pod36, pod36).apply {
				setMargins(pod12, 0, 0, 0)
			}
			text = "X"
			setTextColor(Color.RED)
			setPadding(0, 0, 0, 0)
		}
		addView(infoContainer)
		addView(btnMinus)
		addView(tvQuantity)
		addView(btnPlus)
		addView(btnDelete)
	}
}
