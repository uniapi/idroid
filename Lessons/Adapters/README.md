# 🔁 Android Adapters: The Complete Master Reference

This reference manual provides a complete architectural blueprint of the native **Android Adapter Ecosystem** within the standard AOSP SDK (`android.widget`, `android.database`). 

In a pure-code, zero-XML, no-AndroidX architecture, **Adapters** act as the essential structural bridge between complex underlying data sources (arrays, lists, database cursors)
and layout containers subclassing `AdapterView` (such as `ListView`, `GridView`, or `Spinner`). 

---

## 🌿 1. Complete Inheritance & Interface Hierarchy

The diagram below details exactly how native Adapter definitions, abstract wrappers, and concrete class implementations inherit framework contracts directly from the core platform interface layer.

```text
android.widget.Adapter (Core Interface)
 │
 ├── android.widget.ListAdapter (Interface for Lists)
 └── android.widget.SpinnerAdapter (Interface for Dropdowns)
      │
      └── android.widget.BaseAdapter (Abstract Implementation)
           │
           ├── android.widget.ArrayAdapter<T> (Object/Array Bindings)
           │
           ├── android.widget.CursorAdapter (Database Cursor Framework) [API 1]
           │    └── android.widget.ResourceCursorAdapter [API 1]
           │         └── android.widget.SimpleCursorAdapter (Map Cursors to Views) [API 1]
           │
           ├── android.widget.SimpleAdapter (Static Map-to-View Binder)
           │
           └── android.widget.HeaderViewListAdapter (Wrapper adding Headers/Footers)
```

---

## 📐 2. Structural Breakdown of Core Platforms & Wrappers

Every standard adapter class addresses specific data-binding mechanics, optimization scopes, or structural wrapping requirements.

*   **Adapter [API 1]** – The foundational interface. Manages data set observation, item count, view pool mapping, and lifecycle changes.
*   **BaseAdapter [API 1]** – An abstract, state-free implementation. Implements common core interface boilerplate. Serves as the primary root for writing highly optimized custom adapters.
*   **ArrayAdapter\<T\> [API 1]** – A concrete, type-safe wrapper built specifically to bind standard arrays or instance structures of `java.util.List` to raw string or component view containers.
*   **SimpleAdapter [API 1]** – Maps static configurations of key-value maps (e.g., `Map<String, *>`) directly into a target view layout using predefined column mappings.
*   **SimpleCursorAdapter [API 1]** – Binds background SQLite database `Cursor` query arrays directly onto view rows. Requires database columns to include a unique integer `_id` column.
*   **HeaderViewListAdapter [API 1]** – A special structural wrapper used by `ListView` when header views (`addHeaderView`) or footer views (`addFooterView`) are added to an active container.
	It encapsulates your inner layout adapter while managing row allocation offsets.

---

## ⚡ 3. The Mechanics of View Recycling (`convertView`)

To prevent heavy memory allocation, Android does not destroy off-screen row layouts when a user scrolls.
Instead, it moves detached views into a **recycling pool** and returns them via the `convertView` argument inside the `getView()` loop. 

### Core Architectural Rules:
1.  **Check for Nullability**: If `convertView` is `null`, allocate and build a brand new view tree layout using pure Kotlin instantiation.
2.  **Reuse Explicit Instances**: If `convertView` is **not** `null`, skip view creation completely. Cast the instance directly to update its properties with the current index payload.
3.  **Encapsulate State via ViewHolders**: When building UI code programmatically, design a custom compound view class that holds explicit references to subcomponents.
	This completely bypasses expensive `findViewById()` lookups.

---

## 🛠 4. Comprehensive Production Implementation (No XML / No AndroidX)

Here is a complete, production-grade implementation of a non-XML custom list layout using a high-performance programmatic **ViewHolder-pattern Custom View** bound to a `BaseAdapter`.

```kotlin
import android.app.Activity
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*

// 1. Data Model Definition
data class Product(val id: Long, val name: String, val price: Double, var isChecked: Boolean = false)

// 2. High-Performance Programmatic View Layout (Bypasses findViewById)
class ProductItemView(context: Context) : LinearLayout(context) {
    val cbBox: CheckBox
    val tvDescr: TextView
    val tvPrice: TextView
    val ivImage: ImageView

    init {
        layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        )
        orientation = HORIZONTAL
        setPadding(dpToPx(16), dpToPx(12), dpToPx(16), dpToPx(12))
        setGravity(Gravity.CENTER_VERTICAL)

        // CheckBox Initialization
        cbBox = CheckBox(context).apply {
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT)
        }
        addView(cbBox)

        // Vertical Content Container Layout
        val innerVerticalLayout = LinearLayout(context).apply {
            layoutParams = LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                leftMargin = dpToPx(12)
                rightMargin = dpToPx(12)
            }
            orientation = VERTICAL
        }

        tvDescr = TextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            textSize = 18f
            setTextColor(Color.BLACK)
        }
        innerVerticalLayout.addView(tvDescr)

        tvPrice = TextView(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT).apply {
                topMargin = dpToPx(4)
            }
            textSize = 14f
            setTextColor(Color.GRAY)
        }
        innerVerticalLayout.addView(tvPrice)
        addView(innerVerticalLayout)

        // ImageView Initialization
        ivImage = ImageView(context).apply {
            layoutParams = LayoutParams(dpToPx(48), dpToPx(48))
            setImageResource(android.R.drawable.sym_def_app_icon)
        }
        addView(ivImage)
    }

    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
}

// 3. Custom BaseAdapter Implementation
class BoxAdapter(
    private val context: Context,
    private val products: List<Product>
) : BaseAdapter() {

    override fun getCount(): Int = products.size
    override fun getItem(position: Int): Product = products[position]
    override fun getItemId(position: Int): Long = products[position].id

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        // Enforce view recycling via programmatic types
        val itemView = if (convertView != null) {
            convertView as ProductItemView
        } else {
            ProductItemView(context)
        }

        val currentProduct = getItem(position)

        // Clean, direct property binding without findViewById overhead
        itemView.tvDescr.text = currentProduct.name
        itemView.tvPrice.text = "$${currentProduct.price}"
        
        // Temporarily clear listener to prevent execution during cycling state restores
        itemView.cbBox.setOnCheckedChangeListener(null)
        itemView.cbBox.isChecked = currentProduct.isChecked

        itemView.cbBox.setOnCheckedChangeListener { _, isChecked ->
            currentProduct.isChecked = isChecked
        }

        return itemView
    }
}

// 4. Executing Host Activity Environment
class SalamActivity : Activity() {
    private companion object {
        const val TAG = "localhost.idroid.salamun"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Generate Pure Code Layout Structure
        val rootLayout = LinearLayout(this).apply {
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(Color.WHITE)
        }

        val listView = ListView(this).apply {
            layoutParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT)
        }
        rootLayout.addView(listView)
        setContentView(rootLayout)

        // Mock Data Generation Pass
        val productList = listOf(
            Product(1, "Laptop Pro", 1299.99),
            Product(2, "Wireless Mouse", 49.99),
            Product(3, "Mechanical Keyboard", 119.50),
            Product(4, "4K Monitor 27\"", 380.00),
            Product(5, "USB-C Hub Multipoint", 35.00)
        )

        // Adapter Configuration & Binding
        val boxAdapter = BoxAdapter(this, productList)
        listView.adapter = boxAdapter

        // Item Navigation Selection Interaction Handler
        listView.setOnItemClickListener { parent, view, position, id ->
            val clickedItem = parent.getItemAtPosition(position) as Product
            Toast.makeText(this, "Selected product: ${clickedItem.name}", Toast.LENGTH_SHORT).show()
        }
    }
}
```

---

## 📝 5. Key Takeaways

* **View Recycling State Glitches**: Always clear active interaction listeners (`setOnCheckedChangeListener(null)`) on views inside recycled cells *before* assigning updated checkbox values.
Otherwise, reusing the view will execute obsolete event blocks mapped to other index rows.
* **The Power of `Any?` in `getItem()`**: While `BaseAdapter` defaults `getItem()` to return Java's `Object` (`Any`),
you can override it to return your explicit model type (`Product`) directly, making your internal adapter code clean and type-safe.
* **Data Mutation**: When items are added or removed from the underlying data source collection, calling `adapter.notifyDataSetChanged()`
instantly signals the bound `AdapterView` container to drop cached view structures and redraw active rows.
