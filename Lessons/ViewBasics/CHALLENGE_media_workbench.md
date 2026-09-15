# Challenge 3: Multi-Directional Media Workbench

In this challenge, you will tackle the ultimate test of pure-code layout management:
nesting scroll viewports in different geometric directions while binding image scale types, tabular alignments, and compound toggles into a uniform workspace.

## 🎯 Objective

Your goal is to build a code-only Android screen called **`MediaWorkbenchActivity.kt`**.
This screen mimics a sophisticated media asset manager workspace, requiring strict mastery over view layer boundaries and text typography constraints.

## 📋 Constraints & Rules

1. **No XML**: Absolute reliance on programmatic Kotlin construction.
2. **No androidx / Jetpack**: Use standard framework platform view classes exclusively.
3. **No Interactive Logic**: This is a pure presentation layout challenge. Do not attach any click listeners or checked change hooks.

## 📐 Layout Architecture Requirements

Build a multi-layered interface nesting deep layout nodes within a root vertical container:

### 1. The Horizontal Carousel Viewport (`HorizontalScrollView`)
* Place a horizontal scrolling lane at the very top of your workspace.
* Inside this carousel, nest a horizontal `LinearLayout` hosting a row of exactly **5 independent graphical frames (`ImageView`)**.
* Give each image frame an explicit programmatic width and height of **250 pixels** and apply a margin spacing of **16 pixels** between them.
Configure their inner scaling using `ImageView.ScaleType.CENTER_CROP`.

### 2. Tabular Metadata Workspace (`TableLayout`)
* Directly below the carousel, allocate an invisible structural spacer (`Space`) of exactly **32 pixels**.
* Introduce a clean `TableLayout` to frame file property metadata. 
* Add exactly **4 rows (`TableRow`)**, mapping asset definitions:
    * *Row 1*: Format ➔ EXR High Dynamic Range
    * *Row 2*: Resolution ➔ 3840 x 2160
    * *Row 3*: Color Space ➔ ACEScg
    * *Row 4*: Compressed ➔ True
* Ensure all label descriptions are left-aligned and value strings are right-aligned within the table rows.

### 4. Configuration Controls Block (`LinearLayout` - Vertical)
* Complete the workspace layout by adding a configuration dashboard block at the bottom.
* Add an embedded horizontal line containing an active state control widget matrix:
    * A standard platform layout toggle (`Switch`) reading `"Sync Cloud Metadata"`.
    * A secondary text-button toggle (`ToggleButton`) configured with an explicit `"OFF"` text string state.
* Add an atomic system text clock view node (`TextClock`) set to standard `24-hour` formatting (`"HH:mm:ss"`) centered at the bottom of the cockpit layout.

## 🚀 Verification Checklist

Before declaring victory on this ViewBasics lesson, confirm:
1. [ ] The top carousel scrolls horizontally completely independent of the rest of the screen elements.
2. [ ] Table rows stretch evenly to fill out the width of the smartphone screen container context.
3. [ ] No rendering clipping or overlap anomalies manifest on standard device resolutions.
