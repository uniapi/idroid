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
import android.view.Gravity
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.ToggleButton
import android.widget.ImageView
import android.graphics.Color
import android.graphics.Typeface

class TrackItemView(context: Context) : LinearLayout(context) {
	val ivPlayPause: ImageView
	val tvTitle: TextView
	val tvDuration: TextView
	val tbFavorite: ToggleButton

	init {
		val pod4 = dpToPx(context, 4f)
		val pod12 = dpToPx(context, 12f)
		val pod16 = dpToPx(context, 16f)
		val pod40 = dpToPx(context, 40f)
		val pod48 = dpToPx(context, 48f)
		layoutParams = ViewGroup.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
		orientation = HORIZONTAL
		setGravity(Gravity.CENTER_HORIZONTAL)
		setPadding(pod16, pod12, pod16, pod12)
		setBackgroundColor(Color.WHITE)

		ivPlayPause = ImageView(context).apply {
			layoutParams = LayoutParams(pod40, pod40)
			setImageResource(android.R.drawable.ic_media_play)
		}
		addView(ivPlayPause)

		val textContainer = LinearLayout(context).apply {
			layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
				setMargins(pod16, 0, pod16, 0)
			}
			orientation = VERTICAL
		}
		tvTitle = TextView(context).apply {
			layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			textSize = 18f
			setTextColor(Color.BLACK)
			typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
		}
		textContainer.addView(tvTitle)

		tvDuration = TextView(context).apply {
			layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, pod4, 0, 0)
			}
			textSize = 14f
			setTextColor(Color.GRAY)
		}
		textContainer.addView(tvDuration)
		addView(textContainer)

		tbFavorite = ToggleButton(context).apply {
			layoutParams = LayoutParams(pod48, pod48)
			textOn = "★"
			textOff = "☆"
			text = "☆"
		}
		addView(tbFavorite)
	}
}
