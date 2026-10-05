# 🏆 Advanced Challenge: E-Commerce Catalog Filter Engine (Parcelable & States)

## 🎯 The Objective
Your goal is to build a high-performance **Catalog Filter Engine** completely in pure Kotlin code (zero XML, zero AndroidX). 

This challenge elevates your understanding of Intents and Lifecycles by forcing you to pass complex,
structured data states between a **Main Catalog Screen (`CatalogActivity`)** and a **Filter Configuration Screen (`FilterActivity`)**.
You will use native Android `Parcelable` serialization to bundle multiple non-sensitive criteria,
leverage `onRestart()` to capture screen exposure loops, and handle full state restoration during device rotation configurations.

---

## 📐 Extension: Type-Safe Data Passing via Native `Parcelable`

When passing a complex object containing multiple variables through an `Intent`, you cannot simply cast it.
You must implement the platform's native serialization interface: `android.os.Parcelable`. 

Study this boilerplate construction pattern carefully to avoid compile-time issues:

```kotlin
import android.os.Parcel
import android.os.Parcelable

data class FilterState(
    var minPrice: Int,
    var maxPrice: Int,
    var sortByPriceAscending: Boolean
) : Parcelable {
    
    // Read from parcel in the exact same order as written
    constructor(parcel: Parcel) : this(
        parcel.readInt(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte()
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeInt(minPrice)
        parcel.writeInt(maxPrice)
        parcel.writeByte(if (sortByPriceAscending) 1 else 0)
    }

    override fun describeContents(): Int = 0

    companion object CREATOR : Parcelable.Creator<FilterState> {
        override fun createFromParcel(parcel: Parcel): FilterState = FilterState(parcel)
        override fun newArray(size: Int): Array<FilterState?> = arrayOfNulls(size)
    }
}
```
*To pass this object:* `intent.putExtra("EXTRA_FILTER", myFilterState)`
*To extract this object:* `intent.getParcelableExtra<FilterState>("EXTRA_FILTER")`

---

## 🏗️ Step-by-Step Architectural Blueprint

To guarantee implementation success without getting stuck, follow this precise sequence of engineering steps:

### Step 1: Establish the State Model
Create a separate file or structure declaring the `FilterState` data object using the `Parcelable` contract detailed above.
It must track three safe public attributes: `minPrice: Int`, `maxPrice: Int`, and `categoryName: String`.

### Step 2: Design the Main Catalog UI (`CatalogActivity`)
1.  **Layout Construction**: Instantiate a vertical root layout containing:
    *   `tvFilterSummary`: A `TextView` (TextSize `16sp`, displaying the current criteria, e.g., `"Filters: 0 - 500 | Tech"`).
    *   `btnOpenFilters`: A `Button` (Text: `"Configure Search Filters"`).
2.  **State Initialization**: Maintain a local reference variable initialized with default parameters: `var currentFilter = FilterState(0, 1000, "All")`.
3.  **The Interceptor Lifecycle**: 
    *   Override `onRestart()`. Print a `Log.d` statement stating: `"CatalogActivity: User re-entered screen, re-validating cached data streams."`
    *   Override `startActivityForResult(..., 301)` inside `btnOpenFilters`'s listener,
	and remember to pass the *current* active `currentFilter` object inside the launch intent bundle so the filters screen opens with the previously selected values.

### Step 3: Architect the Filter Configuration UI (`FilterActivity`)
1.  **Layout Construction**: Set up a clean layout containing:
    *   `btnSetRange`: A `Button` (Text: `"Set Budget Range ($10 - $150)"`).
    *   `btnSelectTechCategory`: A `Button` (Text: `"Select Category: Tech"`).
    *   `btnApply`: A `Button` (Text: `"Apply and Return"`, background tint suggested).
2.  **Unpacking State**: Inside `onCreate()`, capture the incoming `FilterState` object sent by the parent activity.
3.  **Mutation Triggers**: 
    *   Tapping `btnSetRange` mutates the active local object parameters: `minPrice = 10` and `maxPrice = 150`.
    *   Tapping `btnSelectTechCategory` mutates `categoryName = "Tech"`.
    *   Tapping `btnApply` allocates a response intent, bundles the newly mutated `FilterState` object back into the extra parameters slot,
	marks `setResult(Activity.RESULT_OK, intent)`, and dispatches `finish()`.

### Step 4: Intercept and Persist Configurations
1.  Override `onActivityResult()` in `CatalogActivity`. Validate request code `301`, unpack the updated `FilterState` payload object,
assign it back to `currentFilter`, and update the text summary display field.
2.  Override `onSaveInstanceState(outState: Bundle)` to cache `currentFilter` under an explicit key string during destruction sequences.
3.  Inside `onCreate()`, read the `Parcelable` data back if the bundle state is present, ensuring total survival over configuration shifts.

---

## 🧪 Verification Matrix & Bug Elimination Checks

Before considering your advanced state-machine implementation production-ready, ensure it passes these functional testing vectors:

| Execution Vector | Expected Runtime Success Criteria | Architecture Verification Rule |
| :--- | :--- | :--- |
| **Bidirectional Complex Loop** | Launching filters from a customized state displays the active parameters inside the sub-panel seamlessly. | Intent bundle properly deserializes `Parcelable` structures. |
| **Transient Re-Exposure Monitor** | Pressing the device's system back button inside `FilterActivity` prints the specialized `onRestart()` indicator to Logcat. | Transitional visibility lifecycle hooks are correctly intercepted. |
| **Hard Memory Persistence Pass** | Modifying prices to `$10 - $150`, applying them, and turning the device sideways leaves the updated text description intact. | State allocation recovery layer handles `onSaveInstanceState` cycles. |

---
