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

class StageOneActivity : Activity() {
    private companion object {
        const val TAG = "localhost.idroid.salamun"
		const val REQUEST_CODE_STAGE_TWO = 501
		const val KEY_STAGE_ONE_PAYLOAD = "KEY_STAGE_ONE_PAYLOAD"
    }
	private lateinit var tvSelectionSummary: TextView
	private var partialPayload = TaskPayload("", 0, "", false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
		Log.d(TAG, "StageOneActivity: onCreate() fired")
		if (savedInstanceState != null) {
			savedInstanceState.getParcelable<TaskPayload>(KEY_STAGE_ONE_PAYLOAD)?.let {
				partialPayload = it
			}
		}
        val rootLayout = LinearLayout(this).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            orientation = LinearLayout.VERTICAL
			setGravity(Gravity.CENTER)
			setPadding(48, 48, 48, 48)
			setBackgroundColor(Color.parseColor("#FAFAFA"))
        }
		tvSelectionSummary = TextView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 0, dpToPx(this@StageOneActivity, 24f))
			}
			textSize = 16f
			setTextColor(Color.BLACK)
			gravity = Gravity.CENTER
		}
		val btnSetMetaA = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 0, dpToPx(this@StageOneActivity, 12f))
			}
			text = "Deploy Server Patch (4 Hours)"
			setOnClickListener {
				partialPayload.taskTitle = "Deploy Server Patch"
				partialPayload.estimatedHours = 4
				updateSummaryText()
			}
		}
		val btnSetMetaB = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 0, dpToPx(this@StageOneActivity, 24f))
			}
			text = "Database Audit (12 Hours)"
			setOnClickListener {
				partialPayload.taskTitle = "Database Audit"
				partialPayload.estimatedHours = 12
				updateSummaryText()
			}
		}
		val btnNext = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			text = "Next Stage ->"
			setBackgroundColor(Color.parseColor("#2196F3"))
			setTextColor(Color.WHITE)
			setOnClickListener {
				if (partialPayload.taskTitle.isNotEmpty()) {
					val intent = Intent(this@StageOneActivity, StageTwoActivity::class.java).apply {
						putExtra("EXTRA_PARTIAL_PAYLOAD", partialPayload)
					}
					startActivityForResult(intent, REQUEST_CODE_STAGE_TWO)
				}
				else {
					Log.w(TAG, "StageOneActivity: Blocked forward routing - No configuration selected")
				}
			}
		}
		with(rootLayout) {
			addView(tvSelectionSummary)
			addView(btnSetMetaA)
			addView(btnSetMetaB)
			addView(btnNext)
		}
		setContentView(rootLayout)
		updateSummaryText()
	}
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		Log.d(TAG, "StageOneActivity: onActivityResult() intercepted data feedback loop")
		if (requestCode == REQUEST_CODE_STAGE_TWO && resultCode == RESULT_OK) {
			setResult(RESULT_OK, data)
			finish()
		}
	}
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		Log.d(TAG, "StageOneActivity: onSaveInstanceState() protecting partial data token")
		outState.putParcelable(KEY_STAGE_ONE_PAYLOAD, partialPayload)
	}
	override fun onDestroy() {
		super.onDestroy()
		Log.d(TAG, "StageOneActivity: onDestroy() executed completely")
	}
	private fun updateSummaryText() {
		tvSelectionSummary.text = if (partialPayload.taskTitle.isEmpty()) {
			"Stage 1/2: Please select meta target"
		}
		else {
			"Selected:\n${partialPayload.taskTitle} (${partialPayload.estimatedHours})"
		}
	}
}
