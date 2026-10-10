/* \uFDFD
 *					   \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *		  \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u0631\u0628\u064A\u0639 \u0671\u0644\u0622\u062E\u0631 \u06F2\u06F7
 */
package localhost.idroid.salamun

import android.util.Log
import android.content.Context
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.AdapterView

class ActiveOrdersAdapter(
	private val context: Context,
	private val activeOrders: MutableList<ActiveOrder>,
	private val onOrderStateChanged: (order: ActiveOrder, isRemoved: Boolean) -> Unit
) : BaseAdapter() {
	override fun getViewTypeCount(): Int = 2
	override fun getItemViewType(position: Int): Int {
		return when (activeOrders[position].priority) {
			Priority.NORMAL -> 0
			Priority.URGENT -> 1
		}
	}
	override fun getCount(): Int = activeOrders.size
	override fun getItem(position: Int): ActiveOrder = activeOrders[position]
	override fun getItemId(position: Int): Long = activeOrders[position].orderId
	override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
		val viewType = getItemViewType(position)
		val order = getItem(position)
		if (viewType == 0) {
			val itemView = if (convertView != null) convertView as NormalOrderView else NormalOrderView(context)
			with(itemView) {
				tvInfo.text = "Order #${order.orderId}: ${order.name}"
				tvProgress.text = "${order.progress}%"
				btnStep.setOnClickListener(null)
				btnStep.setOnClickListener { view ->
					val adapterView = parent as? AdapterView<*> ?: return@setOnClickListener
					val actualPosition = adapterView.getPositionForView(view)
					if (actualPosition != AdapterView.INVALID_POSITION) {
						val currentOrder = activeOrders[actualPosition]
						currentOrder.progress += 25
						val isDone = currentOrder.progress >= 100
						if (isDone) {
							activeOrders.removeAt(actualPosition)	// Removing from the list on 100% ready
						}
						notifyDataSetChanged()
						onOrderStateChanged(currentOrder, isDone)
					}
				}
			}
			return itemView
		}
		else {
			val itemView = if (convertView != null) convertView as UrgentOrderView else UrgentOrderView(context)
			with(itemView) {
				tvInfo.text = "🚨 URGENT #${order.orderId}: ${order.name}"
				btnComplete.setOnClickListener(null)
				btnComplete.setOnClickListener { view ->
					val adapterView = parent as? AdapterView<*> ?: return@setOnClickListener
					val actualPosition = adapterView.getPositionForView(view)
					if (actualPosition != AdapterView.INVALID_POSITION) {
						val currentOrder = activeOrders[actualPosition]
						activeOrders.removeAt(actualPosition)		// Instant removal from the queue
						notifyDataSetChanged()
						onOrderStateChanged(currentOrder, true)
					}
				}
			}
			return itemView
		}
	}
}
