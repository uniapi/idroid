# Challenge 2: Advanced Structural Dashboard Matrix

In this challenge, you will step up your programmatic UI layout composition by moving away from linear listings and building a two-dimensional interface matrix.
You will focus on layout density, strict column span arrangements, structural spacing, and cell weight distributions.

## 🎯 Objective

Your goal is to build a code-only Android screen called **`DashboardMatrixChallengeActivity.kt`**. This screen simulates an industrial performance monitor cockpit.
It divides the screen into a top metric display card and a precise multi-column data grid below it.

## 📋 Constraints & Rules

1. **No XML**: Every widget and layout parameter must be declared natively in Kotlin code.
2. **No androidx / Jetpack**: Inherit strictly from the bare-metal platform `android.app.Activity` class.
3. **No Interactive Logic**: Focus entirely on layout architecture, view nesting, and design boundaries. Do not write any event or state change listeners.

## 📐 Layout Architecture Requirements

Your activity view hierarchy must mount into a main vertical layout and establish two complex structural sections separated by an empty `Space` utility view:

### 1. The Global Viewport Layer (`ScrollView`)
* Wrap the entire screen content inside a vertical scroll container to guarantee responsive scaling across low-resolution devices or landscape orientation.

### 2. High-Density Analytics Header (`LinearLayout`)
* Nest a horizontal `LinearLayout` inside the main container to act as a dual-pane header banner.
* **Left Sub-Pane (`ImageView`)**: A block representing an active hardware sensor array.
* **Right Sub-Pane (`LinearLayout` - Vertical)**: Stack a prominent title string reading `"Core Reactor Node: 01"` and a secondary status subtitle reading `"Status: Operational"`. 
* **Proportions Rule**: Utilize layout weights (`weightSum = 1.0f`) to allocate exactly `0.3f` of the horizontal space to the sensor image pane and `0.7f` to the text details pane.

### 3. Structural Matrix Grid (`GridLayout`)
* Separate the analytics header from the grid by an invisible `Space` component fixed at exactly **64 pixels**.
* Construct a 2-dimensional `GridLayout` configured with exactly **4 columns** and **4 rows**.
* **Cell Population Rule**: Build a multi-span matrix map matching this blueprint:
    * *Row 0 (Full Span)*: A main section divider `TextView` reading `"Primary Analytics Matrix"` spanning across all 4 columns cleanly.
    * *Row 1 & 2 (Standard Cells)*: Fill these grid spaces with structural text widgets
	representing system data (e.g., `"Temp"`, `"98°C"`, `"Pressure"`, `"1.2 atm"`, `"Load"`, `"42%"`, `"Uptime"`, `"148h"`).
    * *Row 3 (Double Span Footer)*: Place two wide buttons at the bottom. Button A must span columns 0-1, and Button B must span columns 2-3 using `GridLayout.spec`.

## 🚀 Verification Checklist

Before saving your progress, confirm:
1. [ ] The screen compiles using only platform `android.widget.*` packages.
2. [ ] Rotating the device to landscape reveals a functional scrollbar without clipping layout nodes.
3. [ ] Columns align into clean, un-nested vertical lines without using nested weight matrices.
