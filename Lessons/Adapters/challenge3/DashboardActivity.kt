/* \uFDFD
 *					 \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *		\u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u0631\u0628\u064A\u0639 \u0671\u0644\u0622\u062E\u0631 \u06F2\u06F7
 */
package localhost.idroid.salamun

import android.util.Base64
import android.util.Log
import android.app.Activity
import android.os.Parcel
import android.os.Bundle
import android.view.View
import android.view.Gravity
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.ArrayAdapter
import android.widget.LinearLayout
import android.widget.AbsListView
import android.widget.ListView
import android.widget.TextView
import android.widget.Button
import android.graphics.Color
import android.graphics.Typeface

class DashboardActivity : Activity() {
	private companion object {
		const val TAG = "localhost.idroid.salamun"
		const val PREFS_NAME = "kitchen_prefs"
		const val KEY_ORDER_PREFIX = "order_"
		const val KEY_ORDER_SEQUENCE = "order_sequence"
	}
	private lateinit var tvMetricsBar: TextView
	private lateinit var lvCatalog: ListView
	private lateinit var lvActiveOrders: ListView
	private val catalogItems = mutableListOf<CatalogItem>()
	private val activeOrdersDataset = mutableListOf<ActiveOrder>()
	private lateinit var activeOrdersAdapter: ActiveOrdersAdapter
	private var orderIdSequence = 1001L

	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		populateCatalogData()
		if (savedInstanceState != null) {
			val savedOrders = savedInstanceState.getParcelableArrayList<ActiveOrder>("KEY_ACTIVE_ORDERS")
			savedOrders?.let {
				activeOrdersDataset.clear()
				activeOrdersDataset.addAll(savedOrders)
			}
			orderIdSequence = savedInstanceState.getLong("KEY_ORDER_SEQUENCE", 1001L)
		}
		else {
			loadOrdersFromDisk()
		}
		val rootLayout = LinearLayout(this).apply {
			layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
			orientation = LinearLayout.VERTICAL
			setBackgroundColor(Color.parseColor("#EEEEEE"))
		}
		tvMetricsBar = TextView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
			textSize = 15f
			setTextColor(Color.WHITE)
			setBackgroundColor(Color.parseColor("#212121"))		// Graphite black
			setPadding(48, 36, 48, 36)
			setGravity(Gravity.CENTER_HORIZONTAL)
			typeface = Typeface.MONOSPACE
		}
		val workspaceSplitter = LinearLayout(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, 0, 1f)
			orientation = LinearLayout.HORIZONTAL
		}
		// Left column: Incoming meu
		val leftWrapper = LinearLayout(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
			orientation = LinearLayout.VERTICAL
			setBackgroundColor(Color.WHITE)
			setPadding(16, 16, 8, 16)
		}
		val tvLeftTitle = TextView(this).apply {
			text = "🛒 INCOMING MENU"
			textSize = 14f
			typeface = Typeface.DEFAULT_BOLD
			setTextColor(Color.BLACK)
			setPadding(0, 0, 0, 12)
		}
		lvCatalog = ListView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
			setBackgroundColor(Color.WHITE)
		}
		with(leftWrapper) {
			addView(tvLeftTitle)
			addView(lvCatalog)
		}
		// Right column: Kitchen execution queue
		val rightWrapper = LinearLayout(this).apply {
			layoutParams = LinearLayout.LayoutParams(0, LayoutParams.MATCH_PARENT, 1f)
			orientation = LinearLayout.VERTICAL
			setPadding(8, 16, 16, 16)
		}
		val tvRightTitle = TextView(this).apply {
			text = "⚡ KITCHEN QUEUE"
			textSize = 14f
			typeface = Typeface.DEFAULT_BOLD
			setTextColor(Color.BLACK)
			setPadding(0, 0, 0, 12)
		}
		lvActiveOrders = ListView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
			setBackgroundColor(Color.WHITE)
			divider = null		// Disabling the default bars since urgent cards have their own border
		}
		with(rightWrapper) {
			addView(tvRightTitle)
			addView(lvActiveOrders)
		}
		with(workspaceSplitter) {
			addView(leftWrapper)
			addView(rightWrapper)
		}
		with(rootLayout) {
			addView(tvMetricsBar)
			addView(workspaceSplitter)
		}
		setContentView(rootLayout)
		// Binding items through Adapters
		// Left panel: Classical text ArrayAdapter
		val pod12 = dpToPx(this, 12f)
		val pod18 = dpToPx(this, 18f)
		val catalogStrings = catalogItems.map { "${it.name} (${it.basePrepTimeMinutes}m)" }
//		val catalogAdapter = ArrayAdapter<String>(this, android.R.layout.simple_list_item_1, catalogStrings)
		val catalogAdapter = object : ArrayAdapter<String>(this, 0 /*android.R.layout.simple_list_item_1*/, catalogStrings) {
			override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
				val textView = (convertView as? TextView) ?: TextView(this@DashboardActivity).apply {
					layoutParams = AbsListView.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
					textSize = 18f
					setTextColor(Color.BLACK)
					setBackgroundColor(Color.WHITE)
					setPadding(pod12, pod18, pod12, pod18)
				}
				textView.text = getItem(position)
				return textView
			}
		}
		lvCatalog.adapter = catalogAdapter
		// Right panel: Out custom heterogeneous ActiveOrdersAdapter
		activeOrdersAdapter = ActiveOrdersAdapter(this, activeOrdersDataset) { order: ActiveOrder, isRemoved: Boolean ->
			syncDashboardMetrics()	// Triggers reactively when row status change
			if (isRemoved)
				deleteSingleOrderFromDisk(order.orderId)
			else
				saveSingleOrderToDisk(order)
		}
		lvActiveOrders.adapter = activeOrdersAdapter
		// INTERFACE BRIDGE: Clicking on the left adds an item to the queue on the right
		lvCatalog.setOnItemClickListener { _, _, position, _ ->
			val selectedProduct = catalogItems[position]
			// Business rule: If preparation time is ≥ 15 minutes, the order automatically becomes URGENT
			val priority = if (selectedProduct.basePrepTimeMinutes >= 15) Priority.URGENT else Priority.NORMAL
			val newOrder = ActiveOrder(
				orderId = orderIdSequence++,
				name = selectedProduct.name,
				progress = 0,
				priority = priority
			)
			activeOrdersDataset.add(newOrder)
			activeOrdersAdapter.notifyDataSetChanged()	// Requesting the right list to redraw
			syncDashboardMetrics()
            saveSingleOrderToDisk(newOrder)
            saveOrderSequenceToDisk()
		}
		syncDashboardMetrics()
	}
	override fun onSaveInstanceState(outState: Bundle) {
		super.onSaveInstanceState(outState)
		outState.putParcelableArrayList("KEY_ACTIVE_ORDERS", ArrayList(activeOrdersDataset))
		outState.putLong("KEY_ORDER_SEQUENCE", orderIdSequence)
	}
	private fun saveOrderSequenceToDisk() {
		getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
			.edit()
			.putLong(KEY_ORDER_SEQUENCE, orderIdSequence)
			.apply()
	}
	private fun saveSingleOrderToDisk(order: ActiveOrder) {
		val sPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
		try {
			val parcel = Parcel.obtain()
			order.writeToParcel(parcel, 0)
			val bytes = parcel.marshall()
			parcel.recycle()
			val base64String = Base64.encodeToString(bytes, Base64.DEFAULT)
			sPrefs.edit()
				.putString("$KEY_ORDER_PREFIX${order.orderId}", base64String)
				.apply()
		}
		catch (e: Exception) {
			e.printStackTrace()
		}
	}
	private fun deleteSingleOrderFromDisk(orderId: Long) {
		val sPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
		sPrefs.edit()
			.remove("$KEY_ORDER_PREFIX$orderId")
            .apply()
	}
    private fun loadOrdersFromDisk() {
        val sPrefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        orderIdSequence = sPrefs.getLong(KEY_ORDER_SEQUENCE, 1000L)
        val allEntries = sPrefs.all
        activeOrdersDataset.clear()
        try {
            for ((key, value) in allEntries) {
                if (key.startsWith(KEY_ORDER_PREFIX) && value is String) {
                    val bytes = Base64.decode(value, Base64.DEFAULT)
                    val parcel = Parcel.obtain()
                    parcel.unmarshall(bytes, 0, bytes.size)
                    parcel.setDataPosition(0)
                    val order = ActiveOrder(parcel)
                    activeOrdersDataset.add(order)
                    parcel.recycle()
                }
            }
            activeOrdersDataset.sortBy { it.orderId }    // Restoring chronological order
        }
        catch (e: Exception) {
            e.printStackTrace()
        }
    }
	private fun populateCatalogData() {
		catalogItems.add(CatalogItem(1, "Espresso Shot", 2))
		catalogItems.add(CatalogItem(2, "Club Sandwich", 8))
		catalogItems.add(CatalogItem(3, "Margherita Pizza Large", 18))	// Triggers URGENT status
		catalogItems.add(CatalogItem(4, "Grilled Salmon", 22))			// Triggers URGENT status
		catalogItems.add(CatalogItem(5, "French Fries Bowl", 5))
	}
	private fun syncDashboardMetrics() {
		val normalCount = activeOrdersDataset.count { it.priority == Priority.NORMAL }
		var urgentCount = activeOrdersDataset.count { it.priority == Priority.URGENT }
		tvMetricsBar.text = "TOTAL ACTIVE: ${activeOrdersDataset.size}|NORMAL: $normalCount|🚨 URGENT: $urgentCount"
		Log.d(TAG, "Dashboard synchronized. Active items count: ${activeOrdersDataset.size}")
	}
}
