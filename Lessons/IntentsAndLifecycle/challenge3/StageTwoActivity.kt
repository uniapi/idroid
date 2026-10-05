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

class StageTwoActivity : Activity() {
    private companion object {
        const val TAG = "localhost.idroid.salamun"
		const val KEY_STAGE_TWO_PAYLOAD = "KEY_STAGE_TWO_PAYLOAD"
    }
	private lateinit var tvFinalReview: TextView
	private lateinit var activePayload: TaskPayload

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
		Log.d(TAG, "StageTwoActivity: onCreate() fired")
		activePayload = savedInstanceState?.getParcelable<TaskPayload>(KEY_STAGE_TWO_PAYLOAD)
			?: intent.getParcelableExtra<TaskPayload>("EXTRA_PARTIAL_PAYLOAD")
			?: TaskPayload("", 0, "", false)

        val rootLayout = LinearLayout(this).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            orientation = LinearLayout.VERTICAL
			setGravity(Gravity.CENTER)
			setPadding(48, 48, 48, 48)
			setBackgroundColor(Color.parseColor("#F5F5F5"))
        }
		tvFinalReview = TextView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 0, dpToPx(this@StageTwoActivity, 24f))
			}
			textSize = 16f
			setTextColor(Color.BLACK)
			gravity = Gravity.CENTER
		}
		val btnSelectOps = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 0, dpToPx(this@StageTwoActivity, 12f))
			}
			text = "Assign to Ops Department"
			setOnClickListener {
				activePayload.allocationDepartment = "Ops Infrastructure"
				updateReviewText()
			}
		}
		val btnMarkUrgent = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 0, dpToPx(this@StageTwoActivity, 24f))
			}
			text = "Toggle Urgent Priority Flag"
			setOnClickListener {
				activePayload.isUrgentPriority = !activePayload.isUrgentPriority
				updateReviewText()
			}
		}
		val btnSubmitFinal = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
			text = "Commit Task and Disband Wizard"
			setBackgroundColor(Color.parseColor("#4CAF50"))
			setTextColor(Color.WHITE)
			setOnClickListener {
				if (activePayload.allocationDepartment.isNotEmpty()) {
					val returnIntent = Intent().apply {
						putExtra("EXTRA_FINAL_PAYLOAD", activePayload)
					}
					setResult(RESULT_OK, returnIntent)
					finish()
				}
				else {
					Log.w(TAG, "StageTwoActivity: Blocked final commit - Department mapping unassigned")
				}
			}
		}
		with(rootLayout) {
			addView(tvFinalReview)
			addView(btnSelectOps)
			addView(btnMarkUrgent)
			addView(btnSubmitFinal)
		}
		setContentView(rootLayout)
		updateReviewText()
	}
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		Log.d(TAG, "StageTwoActivity: onSaveInstanceState() protecting operational payload attributes")
		outState.putParcelable(KEY_STAGE_TWO_PAYLOAD, activePayload)
	}
	override fun onDestroy() {
		super.onDestroy()
		Log.d(TAG, "StageTwoActivity: onDestroy() wizard stack tear-down successful")
	}
	private fun updateReviewText() {
		val dept = if (activePayload.allocationDepartment.isEmpty()) "Unassigned" else activePayload.allocationDepartment
		tvFinalReview.text = "Stage 2/2: Resource Allocation\n\n" +
			"Title: ${activePayload.taskTitle}\n" +
			"Department: $dept\n" +
			"Urgent State: ${activePayload.isUrgentPriority}"
	}
}
