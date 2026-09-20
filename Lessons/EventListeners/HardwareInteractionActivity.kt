/* \uFDFD
 *                     \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *        \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u0631\u0628\u064A\u0639 \u0671\u0644\u0622\u062E\u0631 \u06F1\u06F0
 */
package localhost.idroid.salamun

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class HardwareInteractionActivity : Activity() {
	private lateinit var statusDisplay: TextView
	private lateinit var volumeDisplay: TextView

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		// 1. Initialize the Root Structural Container
		val rootLayout = LinearLayout(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT).apply {
				gravity = Gravity.CENTER_HORIZONTAL
			}
			orientation = LinearLayout.VERTICAL
			setPadding(48, 48, 48, 48)
			setBackgroundColor(Color.parseColor("#FAFAFA"))
		}
		// 2. Screen Title View
		val titleText = TextView(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT
			).apply { bottomMargin = 64 }
			text = "Hardware Event Handlers"
			textSize = 22f
			setTextColor(Color.parseColor("#212121"))
		}
		// 3. Text Input Field (Listens to OnKeyListener)
		val inputField = EditText(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT
			).apply { bottomMargin = 32 }
			hint = "Press hard/soft 'Enter' key here"
			textSize = 16f
			isFocusable = true
			isFocusableInTouchMode = true
			setTextColor(Color.BLACK)
			setHintTextColor(Color.GRAY)
		}
		// 4. Text View for Keyboard Log Output
		statusDisplay = TextView(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT
			).apply { bottomMargin = 64 }
			text = "Key Event Logs: Waiting for input..."
			textSize = 14f
			setTextColor(Color.GRAY)
		}
		// 5. Text View for Volume Button Interception Logs
		volumeDisplay = TextView(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LayoutParams.MATCH_PARENT,
				LayoutParams.WRAP_CONTENT
			)
			text = "Volume Buttons: Not pressed yet"
			textSize = 14f
			setTextColor(Color.parseColor("#00796B"))
		}
		// 6. View.OnKeyListener Implementation via Kotlin SAM Conversion
		// The underscore '_' is used to drop the unused View reference parameter safely
		inputField.setOnKeyListener { view: View?, keyCode: Int, event: KeyEvent? ->
			if (event?.action == KeyEvent.ACTION_DOWN && keyCode == KeyEvent.KEYCODE_ENTER) {
				statusDisplay.text = "Status: 'Enter' key detected! [ACTION_DOWN]"
				statusDisplay.setTextColor(Color.parseColor("#E65100"))
				true
			}
			else {
				false
			}
		}
		with(rootLayout) {
			addView(titleText)
			addView(inputField)
			addView(statusDisplay)
			addView(volumeDisplay)
		}
		setContentView(rootLayout)
	}
	/**
     * 8. Intercept Global Hardware Buttons at the Activity Level.
	 * Ideal for overriding volume or hardware peripheral button mappings.
	 */
	override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
		return when (keyCode) {
			KeyEvent.KEYCODE_VOLUME_UP -> {
				volumeDisplay.text = "Volume: UP pressed! (Intercepted by Activity)"
				volumeDisplay.setTextColor(Color.parseColor("#2E7D32"))
				true // Blocks the standard system volume HUD and behavior
			}
			KeyEvent.KEYCODE_VOLUME_DOWN -> {
				volumeDisplay.text = "Volume: DOWN pressed! (Intercepted by Activity)"
				volumeDisplay.setTextColor(Color.parseColor("#C62828"))
				true // Blocks the standard system volume HUD and behavior
			}
			else -> super.onKeyDown(keyCode, event) // All other hardware keys behave normally
		}
	}
}
