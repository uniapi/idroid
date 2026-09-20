/* \uFDFD
 *                   \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *      \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u0631\u0628\u064A\u0639 \u0671\u0644\u0622\u062E\u0631 \u06F8
 */
package localhost.idroid.salamun

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.Space
import android.widget.TextView
import android.widget.Toast

class ReactiveControlsChallengeActivity : Activity(), View.OnClickListener {
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val rootLayout = LinearLayout(this).apply {
			layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
			orientation = LinearLayout.VERTICAL
			setPadding(64, 96, 64, 96)
		}
		val titleView = TextView(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LayoutParams.WRAP_CONTENT,
				LayoutParams.WRAP_CONTENT
			)
			text = "Security Validation Terminal"
			textSize = 24f
			setPadding(0, 0, 0, 48)
		}
		val secureTapTarget = TextView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 250).apply {
				setMargins(0, 0, 0, 48)
			}
			text = "Secure Tap Target"
			textSize = 15f
			gravity = Gravity.CENTER
			setBackgroundColor(Color.TRANSPARENT)
			setTextColor(Color.GRAY)
		}
		val firstSpacer = Space(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 48)
		}
		val verificationInput = EditText(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			hint = "Enter validation token..."
		}
		val secondSpacer = Space(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 48)
		}
		val actionButton = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			text = "Synchronize Context"
			isEnabled = false
		}
		// Crash-proof Touch handler with strict explicit nullable parameter mappings
		secureTapTarget.setOnTouchListener { v: View?, motionEvent: MotionEvent? ->
			// Use safe-call (?.) access pattern to safely intercept the event
			when(motionEvent?.action) {
				MotionEvent.ACTION_DOWN -> {
					with(secureTapTarget) {
						setBackgroundColor(Color.DKGRAY)
						setTextColor(Color.WHITE)
						text = "TAP TARGET ACTIVE"
					}
					true
				}
				MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
					with(secureTapTarget) {
						setBackgroundColor(Color.TRANSPARENT)
						setTextColor(Color.GRAY)
						text = "Secure Tap Target"
					}
					true
				}
				else -> false
			} ?: false
		}
		verificationInput.addTextChangedListener(object : TextWatcher {
			override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
				// Not utilized for this challenge verification logic
			}
			override fun onTextChanged(s: CharSequence?, start: Int, before: Int, coun: Int) {
				// Capture character transformations safely converting native CharSequence to String
				val currentText = s?.toString() ?: ""
				when(currentText) {
					"IDroid" -> { actionButton.isEnabled = true }
					else -> { actionButton.isEnabled = false }
				}
			}
			override fun afterTextChanged(s: Editable?) {
				// Not utilized for this challenge verification logic
			}
		})
		actionButton.setOnClickListener(this)
		with(rootLayout) {
			addView(titleView)
			addView(secureTapTarget)
			addView(firstSpacer)
			addView(verificationInput)
			addView(secondSpacer)
			addView(actionButton)
		}
		setContentView(rootLayout)
	}
	override fun onClick(v: View?) {
		Toast.makeText(this, "System Context Synchronized!", Toast.LENGTH_SHORT).show()
	}
}
