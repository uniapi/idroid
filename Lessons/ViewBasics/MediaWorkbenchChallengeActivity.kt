/* \uFDFD
 *                     \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *        \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u0631\u0628\u064A\u0639 \u0671\u0644\u0622\u062E\u0631 \u06F1\u06F4
 */
package localhost.idroid.salamun

import android.util.Log
import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.HorizontalScrollView
import android.widget.ImageView
import android.widget.ToggleButton
import android.widget.Switch
import android.widget.Space
import android.widget.TextView
import android.widget.TextClock

class MediaWorkbenchChallengeActivity : Activity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val rootLayout = LinearLayout(this).apply {
			layoutParams = LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT
			)
			orientation = LinearLayout.VERTICAL
		}
		val carouselScroll = HorizontalScrollView(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT
			)
			isHorizontalScrollBarEnabled = false // Removes the standard horizontal scroll bar visual layer for a cleaner asset window appearance
		}
		val carouselLayout = LinearLayout(this).apply {
			layoutParams = FrameLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT
			)
			orientation = LinearLayout.HORIZONTAL
		}
		for (i in 1..7) {
			carouselLayout.addView(ImageView(this).apply {
				layoutParams = LinearLayout.LayoutParams(250, 250).apply {
					setMargins(16, 16, 16, 16)
				}
				setImageResource(android.R.drawable.ic_menu_gallery)
				scaleType = ImageView.ScaleType.CENTER_CROP
				setBackgroundColor(Color.LTGRAY)
			})
		}
		carouselScroll.addView(carouselLayout)
		val structuralSpacer = Space(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				32
			)
		}
		val tableLayout = TableLayout(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT
			)
			isShrinkAllColumns = true
			isStretchAllColumns = true
		}
		val metadata = listOf(
			"Format" to "EXR High Dynamic Range",
			"Resolution" to "3840 x 2160",
			"Color Space" to "ACEScg",
			"Compressed" to "True"
		)
		for ((meta, data) in metadata) {
			val metaView = TextView(this).apply {
				gravity = Gravity.START
				typeface = Typeface.DEFAULT
				text = meta
				textSize = 16f
			}
			val dataView = TextView(this).apply {
				gravity = Gravity.END
				typeface = Typeface.MONOSPACE
				text = data
				textSize = 16f
			}
			tableLayout.addView(TableRow(this).apply {
				layoutParams = TableLayout.LayoutParams(
					LayoutParams.MATCH_PARENT,
					LayoutParams.WRAP_CONTENT
				).apply { setMargins(0, 8, 0, 8) }
				addView(metaView)
				addView(dataView)
			})
		}
		val configSpacer = Space(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				64
			)
		}
		val configLayout = LinearLayout(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT
			)
			orientation = LinearLayout.VERTICAL
		}
		val stateLayout = LinearLayout(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT
			)
			orientation = LinearLayout.HORIZONTAL
		}
		val syncSwitch = Switch(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				0,
				LayoutParams.WRAP_CONTENT,
				1.0f	// Occupies remaining space cleanly pushing the secondary control outward
			)
			text = "Sync Cloud Metadata"
			textSize = 16f
		}
		val stateToggle = ToggleButton(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT
			)
			textOn = "ON"
			textOff = "OFF"
			text = "OFF"	// Set initial programmatic text presentation state strictly matching specifications
			isChecked = false
		}
		with(stateLayout) {
			addView(syncSwitch)
			addView(stateToggle)
		}
		configLayout.addView(stateLayout)
		val clockSpacer = Space(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				48
			)
		}
		val textClock = TextClock(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT
			)
			gravity = Gravity.CENTER_HORIZONTAL
			format24Hour = "HH:mm:ss"
			textSize = 24f
		}
		with(rootLayout) {
			addView(carouselScroll)
			addView(structuralSpacer)
			addView(tableLayout)
			addView(configSpacer)
			addView(configLayout)
			addView(clockSpacer)
			addView(textClock)
		}
		setContentView(rootLayout)
	}
}
