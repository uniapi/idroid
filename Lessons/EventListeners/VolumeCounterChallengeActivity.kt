/* \uFDFD
 *                     \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *        \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u0631\u0628\u064A\u0639 \u0671\u0644\u0622\u062E\u0631 \u06F1\u06F0
 */
package localhost.idroid.salamun

import android.app.Activity
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.text.InputType

class VolumeCounterChallengeActivity : Activity() {
	private lateinit var counterDisplay: TextView
	private var counter = 0
	private var activeStep = 1
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val rootLayout = LinearLayout(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.MATCH_PARENT
			).apply { gravity = Gravity.CENTER_HORIZONTAL }
			orientation = LinearLayout.VERTICAL
			setPadding(48, 48, 48, 48)
		}
		val titleView = TextView(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT
			).apply { bottomMargin = 96 }
			text = "Volume Counter Challenge"
			textSize = 24f
		}
		val inputField = EditText(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT
			).apply { bottomMargin = 48 }
			hint = "Enter increment step..."
			textSize = 16f
			isFocusable = true
			isFocusableInTouchMode = true
			inputType = InputType.TYPE_CLASS_NUMBER
		}
		counterDisplay = TextView(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT
			).apply { topMargin = 96; bottomMargin = 96 }
			gravity = Gravity.CENTER
			typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
			text = "0"
			textSize = 32f
		}
		inputField.setOnKeyListener { view: View?, keyCode: Int, event: KeyEvent? ->
			if (event?.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
				activeStep = try { inputField.text.toString().toInt() } catch(e: Exception) { 1 }
				inputField.clearFocus()
				true
			}
			else {
				false
			}
		}

		with(rootLayout) {
			addView(titleView)
			addView(inputField)
			addView(counterDisplay)
		}
		setContentView(rootLayout)
	}
	override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
		return when(keyCode) {
			KeyEvent.KEYCODE_VOLUME_UP -> { counter += activeStep; counterDisplay.text = "$counter"; true }
			KeyEvent.KEYCODE_VOLUME_DOWN -> { counter -= activeStep; counterDisplay.text = "$counter"; true }
			else -> super.onKeyDown(keyCode, event)
		}
	}
}
