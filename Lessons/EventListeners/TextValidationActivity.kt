/* \uFDFD
 *				   \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *	  \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u0631\u0628\u064A\u0639 \u0671\u0644\u0622\u062E\u0631 \u06F8
 */
package localhost.idroid.salamun

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class TextValidationActivity : Activity() {

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val rootLayout = LinearLayout(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.MATCH_PARENT
			)
			orientation = LinearLayout.VERTICAL
			setPadding(48, 48, 48, 48)
		}
		val inputField = EditText(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.MATCH_PARENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
			)
			hint = "Type a password (min 6 chars)"
		}
		val errorText = TextView(this).apply {
			layoutParams = LinearLayout.LayoutParams(
				LinearLayout.LayoutParams.WRAP_CONTENT,
				LinearLayout.LayoutParams.WRAP_CONTENT
			).apply { topMargin = 16 }
			text = "Too short"
			setTextColor(Color.RED)
		}
		// Connecting TextWatcher via an anonymous object implementation
		inputField.addTextChangedListener(object : TextWatcher {
			override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
				// Invoked immediately BEFORE the text changes in memory
			}
			override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
				// Invoked AS the characters are mutating.
				// We use safe calls 's?.length' due to platform nullability rules
				val currentLength = s?.length ?: 0
				if (currentLength >= 6) {
					errorText.text = "Password secure"
					errorText.setTextColor(Color.parseColor("#2E7D32")) // Green
				} else {
					errorText.text = "Too short (${6 - currentLength} characters left)"
					errorText.setTextColor(Color.RED)
				}
			}
			override fun afterTextChanged(s: Editable?) {
				// Invoked immediately AFTER modifications are applied to the editable buffer
			}
		})
		rootLayout.addView(inputField)
		rootLayout.addView(errorText)
		setContentView(rootLayout)
	}
}
