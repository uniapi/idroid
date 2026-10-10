/* \uFDFD
 *					 \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *		\u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u0631\u0628\u064A\u0639 \u0671\u0644\u0622\u062E\u0631 \u06F2\u06F6
 */
package localhost.idroid.salamun

import android.util.Log
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.TextView
import android.graphics.Color
import android.graphics.Typeface

class CartActivity : Activity() {
	private companion object {
		const val TAG = "localhost.idroid.salamun"
	}
	private lateinit var tvTotalizer: TextView
	private val cartDataset = mutableListOf<CartItem>()

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val rootLayout = LinearLayout(this).apply {
			layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
			orientation = LinearLayout.VERTICAL
			setBackgroundColor(Color.WHITE)
		}
		tvTotalizer = TextView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			textSize = 20f
			setTextColor(Color.WHITE)
			setBackgroundColor(Color.parseColor("#3F51B5"))	// Deep Blue Indigo
			setPadding(64, 48, 64, 48)
			typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
		}
		val listView = ListView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
		}
		with(rootLayout) {
			addView(tvTotalizer)
			addView(listView)
		}
		setContentView(rootLayout)
		generateMockData()
		val cartAdapter = CartAdapter(this, cartDataset) {
			updateGrandTotalVisuals()
		}
		listView.adapter = cartAdapter
		updateGrandTotalVisuals()
	}
	private fun generateMockData() {
		cartDataset.add(CartItem.Header("Electronics"))
		cartDataset.add(CartItem.Product(101, "Bluetooth Headphones", 89.99, 1))
		cartDataset.add(CartItem.Product(102, "Power Bank 20000mAh", 29.50, 2))
		cartDataset.add(CartItem.Header("Groceries"))
		cartDataset.add(CartItem.Product(201, "Organic Coffee Beans", 15.00, 1))
		cartDataset.add(CartItem.Product(202, "Extra Virgin Olive Oil", 24.00, 1))
	}
	private fun updateGrandTotalVisuals() {
		var grandTotal = 0.0
		for (item in cartDataset)
			if (item is CartItem.Product)
				grandTotal += item.unitPrice * item.quantity
		val formattedSum = String.format("%.2f", grandTotal)
		tvTotalizer.text = "Total Balance: $$formattedSum"
		Log.d(TAG, "CartActivity: Total balance sheet updated to $$formattedSum")
	}
}
