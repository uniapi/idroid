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
import android.graphics.Typeface
import android.graphics.Color

class StageHomeActivity : Activity() {
    private companion object {
        const val TAG = "localhost.idroid.salamun"
		const val REQUEST_CODE_WIZARD = 500
		const val KEY_SAVED_PAYLOAD = "KEY_SAVED_PAYLOAD"
    }
	private lateinit var tvDashboard: TextView
	private var finalTaskPayload: TaskPayload? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
		Log.d(TAG, "StageHomeActivity: onCreate() fired")
		if (savedInstanceState != null)
			finalTaskPayload = savedInstanceState.getParcelable(KEY_SAVED_PAYLOAD)

        val rootLayout = LinearLayout(this).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            orientation = LinearLayout.VERTICAL
			setGravity(Gravity.CENTER)
			setPadding(48, 64, 48, 64)
			setBackgroundColor(Color.WHITE)
        }
		tvDashboard = TextView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 0, dpToPx(this@StageHomeActivity, 32f))
			}
			textSize = 15f
			setTextColor(Color.DKGRAY)
			typeface = Typeface.MONOSPACE
			gravity = Gravity.CENTER
		}
		val btnCreateTask = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			text = "Initialize New Task Provisioning"
			setOnClickListener {
				val intent = Intent(this@StageHomeActivity, StageOneActivity::class.java)
				startActivityForResult(intent, REQUEST_CODE_WIZARD)
			}
		}
		with(rootLayout) {
			addView(tvDashboard)
			addView(btnCreateTask)
		}
		setContentView(rootLayout)
		renderDashboard()
	}
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		Log.d(TAG, "StageHomeActivity: onActivityResult() received cascade signal")
		if (requestCode == REQUEST_CODE_WIZARD && resultCode == RESULT_OK && data != null) {
			finalTaskPayload = data?.getParcelableExtra("EXTRA_FINAL_PAYLOAD")
			renderDashboard()
		}
	}
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		Log.d(TAG, "StageHomeActivity: onSaveInstanceState() caching payload state")
		outState.putParcelable(KEY_SAVED_PAYLOAD, finalTaskPayload)
	}
	override fun onDestroy() {
		super.onDestroy()
		Log.d(TAG, "StageHomeActivity: onDestroy() cleanly disbanded view state")
	}
	private fun renderDashboard() {
		val payload = finalTaskPayload
		if (payload != null) {
			tvDashboard.text = "🎯 DISPATCHED TASK STATE:\n\n" +
				"Title: ${payload.taskTitle}\n" +
				"Est. Time: ${payload.estimatedHours} Hours\n" +
				"Dept. ${payload.allocationDepartment}\n" +
				"Urgent: ${if (payload.isUrgentPriority) "YES 🚨" else "NO"}"
		}
		else {
			tvDashboard.text = "NO ACTIVE TASK DISPATCHED"
		}
	}
}
