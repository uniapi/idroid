/* \uFDFD
 *                     \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *        \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u0631\u0628\u064A\u0639 \u0671\u0644\u0622\u062E\u0631 \u06F1\u06F1
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
import android.widget.GridLayout
import android.widget.ScrollView
import android.widget.ImageView
import android.widget.Button
import android.widget.EditText
import android.widget.Space
import android.widget.TextView

class DashboardMatrixChallengeActivity : Activity() {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val globalScroll = ScrollView(this).apply {
			layoutParams = LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT
			)
			isFillViewport = true
		}
		val mainLayout = LinearLayout(this).apply {
			layoutParams = LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT
			)
			orientation = LinearLayout.VERTICAL
		}
		val headerLayout = LinearLayout(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT
			).apply { gravity = Gravity.CENTER_VERTICAL }
			weightSum = 1.0f
			orientation = LinearLayout.HORIZONTAL
		}
		val sensorView = ImageView(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				0,
				LayoutParams.WRAP_CONTENT,
				0.3f
			)
			setImageResource(android.R.drawable.ic_menu_compass)
			scaleType = ImageView.ScaleType.FIT_CENTER
		}
		val titleView = LinearLayout(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				0,
				LayoutParams.WRAP_CONTENT,
				0.7f	// Exactly 70% width allocation
			)
			orientation = LinearLayout.VERTICAL
		}.also {
			it.addView(TextView(this).apply {
				text = "Core Reactor Node: 01"
				textSize = 24f
			})
			it.addView(TextView(this).apply {
				text = "Status: Operational"
				textSize = 16f
				setTextColor(Color.GREEN)
			})
		}
		with(headerLayout) {
			addView(sensorView)
			addView(titleView)
		}
		val headerSpacer = Space(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				64
			)
		}
		val matrixGrid = GridLayout(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT
			).apply { gravity = Gravity.CENTER_HORIZONTAL }
			rowCount = 4
			columnCount = 4
			alignmentMode = GridLayout.ALIGN_BOUNDS
			useDefaultMargins = true
		}
		val analyticsHeader = TextView(this).apply {
			layoutParams = GridLayout.LayoutParams(
				GridLayout.spec(0, 1),
				GridLayout.spec(0, 4, GridLayout.FILL)
			)
			text = "Primary Analytics Matrix"
			textSize = 24f
		}
		matrixGrid.addView(analyticsHeader)
		val telemetryData = listOf("Temp" to "98°C", "Pressure" to  "1.2 atm", "Load" to "42%", "Uptime" to "148h")
		for ((title, data) in telemetryData) {
			val titleView = TextView(this).apply {
				typeface = Typeface.DEFAULT
				text = title
				textSize = 16f
				setPadding(32, 0, 0, 0)
			}
			val dataView = TextView(this).apply {
				typeface = Typeface.MONOSPACE
				text = data
				textSize = 16f
				setTextColor(Color.GREEN)
				setPadding(16, 0, 0, 0)
			}
			with(matrixGrid) {
				addView(titleView)
				addView(dataView)
			}
		}
		val actionA = Button(this).apply {
			layoutParams = GridLayout.LayoutParams(
				GridLayout.spec(3, 1),
				GridLayout.spec(0, 2, GridLayout.FILL)
			)
			text = "SYS RES"
			textSize = 16f
		}
		val actionB = Button(this).apply {
			layoutParams = GridLayout.LayoutParams(
				GridLayout.spec(3, 1),
				GridLayout.spec(2, 2, GridLayout.FILL)
			)
			text = "DUMP LOGS"
			textSize = 16f
		}
		with(matrixGrid) {
			addView(actionA)
			addView(actionB)
		}
		with(mainLayout) {
			addView(headerLayout)
			addView(headerSpacer)
			addView(matrixGrid)
		}
		globalScroll.addView(mainLayout)
		setContentView(globalScroll)
	}
}
