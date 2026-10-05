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

class CatalogActivity : Activity() {
    private companion object {
        const val TAG = "localhost.idroid.salamun"
		const val REQUEST_CODE_FILTER = 301
		const val KEY_FILTER_STATE = "KEY_FILTER_STATE"
    }
	private lateinit var tvFilterSummary: TextView
	private var currentFilter = FilterState(0, 1000, "All")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
		Log.d(TAG, "CatalogActivity: onCreate() fired")
		if (savedInstanceState != null) {
			savedInstanceState.getParcelable<FilterState>(KEY_FILTER_STATE)?.let{
				currentFilter = it
			}
		}
        val rootLayout = LinearLayout(this).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
            orientation = LinearLayout.VERTICAL
			setGravity(Gravity.CENTER)
			setPadding(48, 64, 48, 64)
			setBackgroundColor(Color.WHITE)
        }
		tvFilterSummary = TextView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
				setMargins(0, 0, 0, dpToPx(this@CatalogActivity, 32f))
			}
			textSize = 16f
			setTextColor(Color.BLACK)
			gravity = Gravity.CENTER
		}
		val btnOpenFilters = Button(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			text = "Configure Search Filters"
			setOnClickListener {
				val targetIntent = Intent(this@CatalogActivity, FilterActivity::class.java).apply {
					putExtra("EXTRA_FILTER", currentFilter)
				}
				startActivityForResult(targetIntent, REQUEST_CODE_FILTER)
			}
		}
		with(rootLayout) {
			addView(tvFilterSummary)
			addView(btnOpenFilters)
		}
		setContentView(rootLayout)
		updateFilterText()
	}
	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		super.onActivityResult(requestCode, resultCode, data)
		Log.d(TAG, "CatalogActivity: onActivityResult() intercepted")
		if (requestCode == REQUEST_CODE_FILTER && resultCode == RESULT_OK && data != null) {
			data.getParcelableExtra<FilterState>("EXTRA_FILTER")?.let {
				currentFilter = it
				updateFilterText()
			}
		}
	}
	override fun onRestart() {
		super.onRestart()
		Log.d(TAG, "CatalogActivity: onRestart() fired - User re-entered screen, re-validating cached data streams.")
	}
	// saving complex object while Activity is to be destroyed
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		Log.d(TAG, "CatalogActivity: onSaveInstanceState() caching current filter state")
		outState.putParcelable(KEY_FILTER_STATE, currentFilter)
	}
	private fun updateFilterText() {
		tvFilterSummary.text = "Active Filters:\nRange: $${currentFilter.minPrice} - $${currentFilter.maxPrice}\nCategory: ${currentFilter.categoryName}"
	}
}
