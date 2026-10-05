/* \uFDFD
 *                     \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *        \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u0631\u0628\u064A\u0639 \u0671\u0644\u0622\u062E\u0631 \u06F2\u06F4
 */
package localhost.idroid.salamun

import android.util.Log
import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.Gravity
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Button
import android.graphics.Color

class ThemeActivity : Activity() {
    private companion object {
        val TAG = "localhost.idroid.salamun"
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
		Log.d(TAG, "ThemeActivity: onCreate() fired")
        val rootLayout = LinearLayout(this).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            orientation = LinearLayout.VERTICAL
			setGravity(Gravity.CENTER)
			setPadding(48, 48, 48, 48)
			setBackgroundColor(Color.parseColor("#F5F5F5"))
        }
		val btnLightGreen = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 0, dpToPx(this@ThemeActivity, 16f))
			}
			text = "Mint Green"
			setOnClickListener {
				sendResultBack(Color.parseColor("#E8F5E9"))
			}
		}
		val btnLightBlue = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			text = "Sky Blue"
			setOnClickListener {
				sendResultBack(Color.parseColor("#E3F2FD"))
			}
		}
		with(rootLayout) {
			addView(btnLightGreen)
			addView(btnLightBlue)
		}
		setContentView(rootLayout)
	}
	private fun sendResultBack(colorHex: Int) {
		var returnIntent = Intent().apply {
			putExtra("EXTRA_COLOR", colorHex)
		}
		setResult(RESULT_OK, returnIntent)
		finish()	// closes the current screen and turns back the user
	}
	override fun onDestroy() {
		super.onDestroy()
		Log.d(TAG, "ThemeActivity: onDestroy() fired")
	}
}
