# 🏆 Master Challenge: Interactive Multi-Adapter Dashboard

## 🎯 The Objective
Your goal is to build an interactive, high-performance **Order Management Dashboard** entirely in pure Kotlin code (zero XML, zero AndroidX). 

This architecture must feature a single dashboard window housing **two independent ListView scrollable zones with different Adapters**.
Tapping items in the first list must dynamically add, remove, or mutate items in the second list, while complex event listeners calculate metrics in a real-time summary widget panel.

---

## 📐 Step-by-Step Architectural Blueprint

### Step 1: Establish the State Models & Polymorphic Data
```kotlin
enum class Priority { NORMAL, URGENT }
data class CatalogItem(val id: Long, val name: String, val basePrepTimeMinutes: Int)
data class ActiveOrder(val orderId: Long, val name: String, var progress: Int, val priority: Priority)
```

### Step 2: Build the Left Catalog Engine (Standard Adapter)
*   Create a collection of `CatalogItem` options and bind them to the left-side `ListView` via a standard `ArrayAdapter`.
*   Assign an `setOnItemClickListener`. When an item is tapped,
it must trigger a state mutation function in the Activity to append a new `ActiveOrder` record into the right-side list's mutable collection dataset.

### Step 3: Architect the Right-Side Multi-Type View System (`ActiveOrdersAdapter`)
Extend `BaseAdapter` to support **two distinct row layouts** based on order priority:
*   **Type 0 (Normal Priority)**: Standard clean text layout, white background, a text field for percentage state, and a step button `[+25%]`.
*   **Type 1 (Urgent Priority)**: Thick bold crimson text warning borders, light red background, and an immediate action completion button `[COMPLETE]`.
*   Remember to perform **STRICT EVENT CLEANUP** inside `getView()` by resetting click listeners to `null` before binding data variables to reused views.

### Step 4: Assemble the Central Dashboard Reactor Loop (`DashboardActivity`)
1.  **Layout Construction**: Use a root vertical layout. Place the top summary bar inside it.
Below that, place a horizontal `LinearLayout` using weight parameters (`layout_weight="1f"`) to split the screen estate equally between the two `ListView` instances.
2.  **Thread-Safe Synchronizer Function**: Implement a central orchestrator routine inside your Activity class to scan active states, compute aggregates, and update the metrics bar strings.
3.  Pass a functional pointer reference (`{ syncDashboardMetrics() }`) directly into the constructor arguments of your custom right-side adapter.
