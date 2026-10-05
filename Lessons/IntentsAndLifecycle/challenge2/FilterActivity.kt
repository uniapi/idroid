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
import android.widget.Button
import android.graphics.Color

class FilterActivity : Activity() {
    private companion object {
        const val TAG = "localhost.idroid.salamun"
    }
	private lateinit var activeFilter: FilterState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
		Log.d(TAG, "FilterActivity: onCreate() fired")
		activeFilter = intent.getParcelableExtra<FilterState>("EXTRA_FILTER") ?: FilterState(0, 1000, "All")
        val rootLayout = LinearLayout(this).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            orientation = LinearLayout.VERTICAL
			setGravity(Gravity.CENTER)
			setPadding(48, 48, 48, 48)
			setBackgroundColor(Color.parseColor("#F5F5F5"))
        }
		val btnSetRange = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 0, dpToPx(this@FilterActivity, 16f))
			}
			text = "Set Budget Range ($10 - $150)"
			setOnClickListener {
				activeFilter.minPrice= 10
				activeFilter.maxPrice = 150
				Log.d(TAG, "FilterActivity: Budget range mutated locally")
			}
		}
		val btnSelectTechCategory = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 0, dpToPx(this@FilterActivity, 32f))
			}
			text = "Select Category: Tech"
			setOnClickListener {
				activeFilter.categoryName = "Tech"
				Log.d(TAG, "FilterActivity: Category mutated locally")
			}
		}
		val btnApply = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			text = "Apply and Return"
			setBackgroundColor(Color.parseColor("#4CAF50"))
			setTextColor(Color.WHITE)
			setOnClickListener {
				val returnIntent = Intent().apply {
					putExtra("EXTRA_FILTER", activeFilter)
				}
				setResult(RESULT_OK, returnIntent)
				finish()
			}
		}
		with(rootLayout) {
			addView(btnSetRange)
			addView(btnSelectTechCategory)
			addView(btnApply)
		}
		setContentView(rootLayout)
	}
	override fun onDestroy() {
		super.onDestroy()
		Log.d(TAG, "FilterActivity: onDestroy() fired")
	}
}
