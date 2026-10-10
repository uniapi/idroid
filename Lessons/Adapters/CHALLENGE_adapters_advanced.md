# 🏆 Advanced Challenge: Expandable E-Commerce Cart Manager

## 🎯 The Objective
Your goal is to build an advanced, high-performance **E-Commerce Shopping Cart** completely in pure Kotlin code (zero XML, zero AndroidX). 

This cart must support **heterogeneous row layouts** (two completely distinct types of views in the same list),
dynamic item quantity modification that instantly recalculates totals, and row suppression (deletion) with proper recycling state management.

---

## 🏗️ Step-by-Step Architectural Blueprint

### Step 1: Design the Polymorphic Data Hierarchy
Since the list contains two different kinds of data, create a sealed class structure to unify them safely:
```kotlin
sealed class CartItem {
    data class Header(val categoryName: String) : CartItem()
    data class Product(val id: Long, val name: String, val unitPrice: Double, var quantity: Int) : CartItem()
}
```

### Step 2: Build the Two Programmatic Custom Views (ViewHolders)
Create two distinct classes extending `LinearLayout` to serve as your clean, `findViewById`-free containers:
1.  **`CartHeaderView(context)`**: Orientation: `HORIZONTAL`. Background: Light Gray (`Color.LTGRAY`). Padding: `12dp`. Holds a single `TextView`.
2.  **`CartProductView(context)`**: Orientation: `HORIZONTAL`. Gravity: `CENTER_VERTICAL`. Padding: `16dp`. Holds `tvName`, `btnMinus` (`"-"`), `tvQuantity`, `btnPlus` (`"+"`), and `btnDelete` (`"X"`).

### Step 3: Master Heterogeneous Adapter Virtualization (`CartAdapter`)
Override these methods inside your custom adapter to tell the platform how to partition the view recycling pools:
```kotlin
override fun getViewTypeCount(): Int = 2
override fun getItemViewType(position: Int): Int = when (items[position]) {
    is CartItem.Header -> 0
    is CartItem.Product -> 1
}
```
*   **Inside `getView()`**: Inspect `convertView` type safety based on `getItemViewType(position)`. If it is null, instantiate the *specific* custom view class needed for that type.
*   **CRITICAL LISTENER RESET**: Before binding data to a `CartProductView`, completely clear out old click triggers on `btnPlus`, `btnMinus`, and `btnDelete`
by setting them to `null` to avoid recycling artifacts.

### Step 4: Setup the Host Application and Live Totalizer (`CartActivity`)
1.  Add a top banner component: a `TextView` dedicated to acting as a **Live Totalizer Balance Sheet** (e.g., displaying `Total: $1,240.00`).
2.  Add the `ListView` below it to occupy the remaining layout workspace.
3.  Populate a mutable list structure (`MutableList<CartItem>`) mixed with Headers and Products.
4.  Provide a functional callback interface from your Activity to your Adapter.
Every time a button changes the quantity or deletes a row, recalculate the sum of all items and call `adapter.notifyDataSetChanged()`.
