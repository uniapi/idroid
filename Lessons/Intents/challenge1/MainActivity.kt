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

class MainActivity : Activity() {
    private companion object {
        const val TAG = "localhost.idroid.salamun"
		const val REQUEST_CODE_THEME = 201
		const val KEY_BACKGROUND_COLOR = "KEY_BACKGROUND_COLOR"
    }
	private lateinit var rootLayout: LinearLayout
	private lateinit var tvCurrentTheme: TextView
	private var currentBackgroundColor = Color.WHITE
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
		Log.d(TAG, "MainActivity: onCreate() fired")
		if (savedInstanceState != null)
			currentBackgroundColor = savedInstanceState.getInt(KEY_BACKGROUND_COLOR, Color.WHITE)

        rootLayout = LinearLayout(this).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            orientation = LinearLayout.VERTICAL
			setGravity(Gravity.CENTER_HORIZONTAL)
			setPadding(48, 64, 48, 64)
			setBackgroundColor(currentBackgroundColor)
        }
		tvCurrentTheme = TextView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 0, dpToPx(this@MainActivity, 32f))
			}
			text = if (currentBackgroundColor == Color.WHITE) "Active Theme: Default (White)" else "Active Theme: Custom Colored"
			textSize = 18f
			setTextColor(Color.BLACK)
			gravity = Gravity.CENTER
		}
		val btnChangeTheme = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			text = "Select Custom Background"
			setOnClickListener {
				val targetIntent = Intent(this@MainActivity, ThemeActivity::class.java)
				startActivityForResult(targetIntent, REQUEST_CODE_THEME)
			}
		}
		with(rootLayout) {
			addView(tvCurrentTheme)
			addView(btnChangeTheme)
		}
		setContentView(rootLayout)
	}
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		Log.d(TAG, "MainActivity: onActivityResult() intercepted")
		if (requestCode == REQUEST_CODE_THEME && resultCode == RESULT_OK && data != null) {
			currentBackgroundColor = data.getIntExtra("EXTRA_COLOR", Color.WHITE)
			rootLayout.setBackgroundColor(currentBackgroundColor)
			tvCurrentTheme.text = "Active Theme: Custom Colored"
		}
	}
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		Log.d(TAG, "MainActivity: onSaveInstanceState() saving background color state")
		outState.putInt(KEY_BACKGROUND_COLOR, currentBackgroundColor)
	}
	override fun onStart() {
		super.onStart()
		Log.d(TAG, "MainActivity: onStart() fired")
	}
	override fun onResume() {
		super.onResume()
		Log.d(TAG, "MainActivity: onResume() fired")
	}
	override fun onPause() {
		super.onPause()
		Log.d(TAG, "MainActivity: onPause() fired")
	}
	override fun onStop() {
		super.onStop()
		Log.d(TAG, "MainActivity: onStop() fired")
	}
	override fun onRestart() {
		super.onRestart()
		Log.d(TAG, "MainActivity: onRestart() fired - User returned from picker")
	}
	override fun onDestroy() {
		super.onDestroy()
		Log.d(TAG, "MainActivity: onDestroy() fired")
	}
}
