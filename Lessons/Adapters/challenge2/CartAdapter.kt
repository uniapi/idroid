/* \uFDFD
 *					 \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *		\u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u0631\u0628\u064A\u0639 \u0671\u0644\u0622\u062E\u0631 \u06F2\u06F6
 */
package localhost.idroid.salamun

import android.util.Log
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter

class CartAdapter(
	private val context: Context,
	private val items: MutableList<CartItem>,
	private val onCartChanged: () -> Unit		// Callback for Activity synchronization
) : BaseAdapter() {
	private companion object {
		const val TAG = "localhost.idroid.salamun"
	}
	override fun getViewTypeCount(): Int = 2	// Two isolated recycling pools
	// The Markup type identifier for a specific position
	override fun getItemViewType(position: Int): Int {
		return when (items[position]) {
			is CartItem.Header -> 0
			is CartItem.Product -> 1
		}
	}
	override fun getCount(): Int = items.size
	override fun getItem(position: Int): CartItem = items[position]
	override fun getItemId(position: Int): Long = position.toLong()
	override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
		val viewType = getItemViewType(position)
		if (viewType == 0) {
		// Handling Type 0: Cart Header
			val itemView = if (convertView != null) convertView as CartHeaderView else CartHeaderView(context)
			val headerData = getItem(position) as CartItem.Header
			itemView.tvTitle.text = headerData.categoryName.toUpperCase()
			return itemView
		}
		else {
		// Handling Type 1: Cart Product
			val itemView = if (convertView != null) convertView as CartProductView else CartProductView(context)
			val productData = getItem(position) as CartItem.Product
			with(itemView) {
				tvName.text = productData.name
				tvPrice.text = "$${String.format("%.2f", productData.unitPrice)}"
				tvQuantity.text = productData.quantity.toString()
				// Completely clear old listeners before data binding
				btnPlus.setOnClickListener(null)
				btnMinus.setOnClickListener(null)
				btnDelete.setOnClickListener(null)
				// Binding new actions
				btnPlus.setOnClickListener {
					productData.quantity++
					tvQuantity.text = productData.quantity.toString()
					onCartChanged()
				}
				btnMinus.setOnClickListener {
					if (productData.quantity > 1) {
						productData.quantity--
						tvQuantity.text = productData.quantity.toString()
						onCartChanged()
					}
				}
				btnDelete.setOnClickListener {
					Log.d(TAG, "CartAdapter: Removing item at position: $position")
					items.removeAt(position)
					notifyDataSetChanged()		// Redraw the list
					onCartChanged()
				}
			}
			return itemView
		}
	}
}
