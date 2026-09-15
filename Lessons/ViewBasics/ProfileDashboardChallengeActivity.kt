/* \uFDFD
 *                   \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *      \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u0631\u0628\u064A\u0639 \u0671\u0644\u0622\u062E\u0631 \u06F7
 */
package localhost.idroid.salamun

import android.util.Log
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.Window
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.Space
import android.widget.Switch
import android.widget.TableLayout
import android.widget.TableRow
import android.widget.TextView

class ProfileDashboardActivity : Activity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		requestWindowFeature(Window.FEATURE_NO_TITLE)
		val rootLayout = LinearLayout(this).apply {
			layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
			orientation = LinearLayout.VERTICAL
			setPadding(48, 48, 48, 48)
			setBackgroundColor(Color.parseColor("#FAFAFA"))		// Off-white clean layout tint
		}
		val headerLayout = RelativeLayout(this).apply {
			layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
		}
		val profileImage = ImageView(this).apply {
			id = View.generateViewId()
			setBackgroundColor(Color.parseColor("#CCCCCC"))		// Neutral gray fallback block
			layoutParams = RelativeLayout.LayoutParams(160, 160)
		}
		val statusText = TextView(this).apply {
			id = View.generateViewId()
			text = "System Mode: Idle"
			textSize = 16f
			setTextColor(Color.DKGRAY)
		}
		val statusParams = RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
			addRule(RelativeLayout.RIGHT_OF, profileImage.id)	// Anchor directly to the right
			addRule(RelativeLayout.CENTER_VERTICAL)				// Align centrally matching image midpoints
			leftMargin = 32		// Flat layout pixel separation gap
		}
		with(headerLayout) {
			addView(profileImage)
			addView(statusText, statusParams)
		}
		val sectionSpacer = Space(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 48 /* Fixed empty spatial separation parameter */)
		}
		val tableLayout = TableLayout(this).apply {
			layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			isStretchAllColumns = true
		}
		val profileKeys = arrayOf("Username", "API Level", "Runtime")
		val profileValues = arrayOf("idroid_developer", "34", "Native ART")
		for ((index, key) in profileKeys.withIndex()) {
			val tableRowNode = TableRow(this).apply {
				layoutParams = TableLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			}
			tableRowNode.addView(TextView(this).apply {
				text = key
				textSize = 15f
				setPadding(16, 24, 16, 24)
				setTextColor(Color.GRAY)
			})
			tableRowNode.addView(TextView(this).apply {
				text = profileValues[index]
				textSize = 15f
				setPadding(16, 24, 16, 24)
				setTextColor(Color.BLACK)
			})
			tableLayout.addView(tableRowNode)
		}

		val systemSwitch = Switch(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			text = "Active Core Subsystems"
			textSize = 16f
			isChecked = false
		}
		systemSwitch.setOnCheckedChangeListener { _, isChecked ->
			when (isChecked) {
				true -> {
					statusText.text = "System Mode: ACTIVE"
					statusText.setTextColor(Color.parseColor("#2E7D32"))	// Dark green active layout state tint
				}
				false -> {
					statusText.text = "System Mode: Idle"
					statusText.setTextColor(Color.DKGRAY)
				}
			}
		}
		val controlSpacer = Space(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 64)
		}
		with(rootLayout) {
			addView(headerLayout)
			addView(sectionSpacer)
			addView(tableLayout)
			addView(controlSpacer)
			addView(systemSwitch)
		}
		setContentView(rootLayout)
	}
}
