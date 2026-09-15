# 🌿Android View Basics
This document serves as an exhaustive structural reference for native Android UI components (Views and ViewGroups)
available in the standard Android Open Source Project (AOSP) SDK (android.view, android.widget, android.webkit).
It is engineered specifically for developers building user interfaces entirely in pure Kotlin code, bypassing XML entirely.

------------------------------
## 📐 1. Structural Layout Containers (ViewGroups)
These are layout engines that extend `ViewGroup`. Their primary purpose is to hold, arrange, and manage structural child view relationships.

* **`LinearLayout` [API 1]** – Arranges its children sequentially in a single row (horizontal) or a single column (vertical).
* **`RelativeLayout` [API 1]** – Positions child views relative to each other or relative to the parent container boundaries using rule anchors.
* **`FrameLayout` [API 1]** – A utility stack container designed to block out a region on the screen.
It displays elements layered on top of each other, typically holding a single primary child or switching between fragments.
* **`GridLayout` [API 14]** – A powerful grid engine that arranges its children into a structural matrix of rows and columns.
It supports row/column spanning concurrently and eliminates the nested-weight performance issues of legacy layouts.
* **`TableLayout` [API 1]** – A specialized extension of `LinearLayout` that organizes elements into clean rows using structural tabular alignment.
* **`TableRow` [API 1]** – A dedicated layout companion container used exclusively inside a `TableLayout` to represent a single horizontal row data matrix.
* **`Space` [API 14]** – An ultra-lightweight, empty utility `View` designed specifically for generating blank spaces or margins inside layout architectures.
It overrides `onDraw` to perform zero rendering operations, keeping measurement passes optimal.
* **`RadioGroup` [API 1]** – A structural extension of `LinearLayout` used to group a set of `RadioButton` elements, ensuring that only one button within the group can be selected at any given time.
* **`TabHost` [API 1]** – A **legacy** container structure used to coordinate a tabbed window interface.
* **`TabWidget` [API 1]** – A layout container specifically responsible for displaying the physical row of tabs used in combination with a `TabHost`.
* **`AbsoluteLayout` [API 1]** **(Deprecated)** – A legacy container that allowed positioning elements using exact, hardcoded X/Y coordinates.
_Avoid using this as it fails to scale across different screen resolutions_.

------------------------------
## 📜 2. Scrollable Viewport Containers
ViewGroups designed specifically to manage child layouts that exceed the physical bounds of the device's hardware viewport.

* **`ScrollView` [API 1]** – A specialized `FrameLayout` wrapper that allows a single direct child layout hierarchy to be scrolled vertically.
* **`HorizontalScrollView` [API 1]** – A specialized `FrameLayout` wrapper engineered specifically to allow horizontal scrolling of a single layout child tree.

------------------------------
## 📦 3. Basic Content Views (Displaying Data)
Atomic UI elements extending `View` whose core purpose is to present imagery, complex graphics, web nodes, or calendar structures to the user.

* **`ImageView` [API 1]** – Displays graphical resource files, vector drawings, bitmaps, or remote web images.
* **`TextView` [API 1]** – A fundamental text-rendering node equipped with comprehensive typography controls, styling engines, and formatting rules.
* **`TextClock` [API 17]** – A specialized extension of `TextView` that hooks directly into the system calendar ticks
to output the exact current time and date in a customized format pattern string (e.g., `hh:mm:ss` a).
* **`WebView` [API 1]** – An embedded high-performance engine node based on Chromium that renders full HTML/CSS/JavaScript web pages directly inside the local application framework.
* **`CalendarView` [API 11]** – A self-contained, fully interactive calendar grid layout that lets users cycle through months and select distinct dates.
* **`SurfaceView` [API 1]** – Provides a dedicated drawing surface embedded directly inside the view hierarchy. It passes drawing calls to a secondary execution thread,
bypassing the main UI thread to render fast animations, games, or video feeds.
* **`TextureView` [API 14]** – A modern graphics surface engine engineered to host real-time rendering layers (like hardware-accelerated video frames, camera viewfinders, or OpenGL contexts).
Unlike `SurfaceView`, it behaves like a standard `View` node—meaning it can be smoothly animated, scaled, rotated, or blended with alpha opacity.
* **`ProgressBar` [API 1]** – A visual indicator showing the progress of an operation (supports both spinning indeterminate loops or horizontal determinate loading lines).
* **`DatePicker` [API 1]** – A dedicated composite widget that allows users to select a month, day, and year.
* **`TimePicker` [API 1]** – A dedicated composite widget that allows users to select a time of day in either 12-hour or 24-hour formats.

------------------------------
## 🔘 4. Interactive Input Controls (User Actions)
Atomic elements that inherit from `View` or `Button` used to capture precise user interactions, text input, or state selections.

* **`EditText` [API 1]** – An editable extension of `TextView` that acts as the standard system component for entering text, characters, or passwords via soft/hard keyboards.
* **`Button` [API 1]** – A standard text-clickable button component that triggers defined callback logic when tapped.
* **`CompoundButton` [API 1]** – An abstract button subclass that maintains a binary checked/unchecked state engine.
* **`CheckBox` [API 1]** – A specialized standard instance of a `CompoundButton` displaying a classic square checkmark indicator—typically used for lists where multiple independent options can be selected.
* **`RadioButton` [API 1]** – A two-state selection button typically contained inside a RadioGroup to facilitate mutually exclusive choice patterns.
* **`ToggleButton` [API 1]** – A visual text-button switch displaying custom ON/OFF text strings depending on its selection state.
* **`Switch` [API 14]** – A tactile slider-toggle track component. It replaced the original `ToggleButton` as the standard implementation for on/off app configurations.
* **`SeekBar` [API 1]** – An extension of `ProgressBar` adding a draggable slider thumb, allowing users to select a fluid range of values (e.g., volume or screen brightness levels).
* **`AbsSeekBar` [API 1]** – An abstract underlying base engine class that handles the core tracking touch mechanics used by components like `SeekBar`.
* **`Toolbar` [API 21]** – A highly customizable action-bar component that replaces the legacy application window `ActionBar`.
Because it operates as a standard `ViewGroup` child node, it can be embedded anywhere within a layout tree to house navigation icons, titles, and custom menu items.
* **`SearchView` [API 11]** – A specialized entry widget built for executing search operations.
It wraps an input field, clean buttons, and active voice hooks inside a uniform container designed to interface with the system search manager.

------------------------------
## 🗂 5. Heavy Data-Driven Adapter Views (Arrays)
Specialized layout components that extend `AdapterView`. They do not hold children directly in code; instead,
they generate child rows dynamically using an external structural backing data provider called an `Adapter`.

* **`AbsListView` [API 1]** – An abstract base class that manages the core scrolling, virtualization, and item tracking logic for structural multi-item view containers.
* **`ListView` [API 1]** – A vertically scrollable layout container that displays a linear sequence of data items retrieved from an adapter.
* **`GridView` [API 1]** – A scrollable container layout that displays data items in a strict, two-dimensional grid arrangement of rows and columns.
* **`AbsSpinner` [API 1]** – An abstract structural base foundation designed to handle list drop-down and picker selection events.
* **`Spinner` [API 1]** – A drop-down selection widget that displays a single active value and reveals a scrollable list of alternative choices when clicked.
* **`NumberPicker` [API 11]** – A selection dial interface allowing a user to step sequentially through a predefined numeric range or a custom string array via increment indicators or wheel gestures.
* **`AdapterViewAnimator` [API 11]** – An abstract layout foundation that provides base implementation mechanics to animate transitions between different underlying adapter row cells.
* **`AdapterViewFlipper` [API 11]** – A specialized instance of an animated adapter view that automatically alternates between its data list rows at distinct intervals,
operating effectively as an automated slideshow loop.
* **`StackView` [API 11]** – An advanced adapter-backed layout view that displays an array of items as a 3D deck of overlapping cards,
allowing users to swipe individual cards away to expose the next node in the array.
* **`Gallery` [API 1]** **(Deprecated)** – A legacy horizontal scrolling widget that centered the current active choice item within a horizontal carousel format.

------------------------------
## 🌿 Unified Master Android View Hierarchy Tree
This tree visually maps exactly how every component listed above inherits from the core java.lang.Object base, structured by major architectural blocks.
```kotlin
java.lang.Object
↳ android.view.View [API 1]
  │
  ├── [ViewGroup Container Branch — Structural Layouts First]
  └── android.view.ViewGroup [API 1]
      ├── android.widget.LinearLayout [API 1]
      │   ├── android.widget.TableLayout [API 1]
      │   ├── android.widget.RadioGroup [API 1]
      │   ├── android.widget.SearchView [API 11]
      │   └── android.widget.Toolbar [API 21]
      │
      ├── android.widget.FrameLayout [API 1]
      │   ├── android.widget.ScrollView [API 1]
      │   ├── android.widget.HorizontalScrollView [API 1]
      │   ├── android.widget.TabHost [API 1]
      │   ├── android.widget.DatePicker [API 1]
      │   └── android.widget.TimePicker [API 1]
      │
      ├── android.widget.GridLayout [API 14]
      ├── android.widget.RelativeLayout [API 1]
      ├── android.widget.TableRow [API 1]
      ├── android.widget.TabWidget [API 1]
      ├── android.widget.AbsoluteLayout [API 1] (Deprecated)
      ├── android.webkit.WebView [API 1]
      │
      ├── [Data Collection / Adapter View Sub-Branch]
      └── android.widget.AdapterView [API 1]
          ├── android.widget.AbsListView [API 1]
          │   ├── android.widget.ListView [API 1]
          │   └── android.widget.GridView [API 1]
          │
          ├── android.widget.AbsSpinner [API 1]
          │   ├── android.widget.Spinner [API 1]
          │   └── android.widget.NumberPicker [API 11]
          │
          ├── android.widget.Gallery [API 1] (Deprecated)
          │
          └── android.widget.AdapterViewAnimator [API 11]
              ├── android.widget.AdapterViewFlipper [API 11]
              └── android.widget.StackView [API 11]
  │
  ├── [Atomic Content & Interactive Widget Branch]
  ├── android.widget.ImageView [API 1]
  ├── android.widget.Space [API 14]
  ├── android.view.TextureView [API 14]
  ├── android.view.SurfaceView [API 1]
  │   └── android.widget.VideoView [API 1]
  │
  ├── android.widget.ProgressBar [API 1]
  │   └── android.widget.AbsSeekBar [API 1]
  │       └── android.widget.SeekBar [API 1]
  │
  └── android.widget.TextView [API 1]
      ├── android.widget.EditText [API 1]
      ├── android.widget.TextClock [API 17]
      │
      └── android.widget.Button [API 1]
          └── android.widget.CompoundButton [API 1]
              ├── android.widget.CheckBox [API 1]
              ├── android.widget.RadioButton [API 1]
              ├── android.widget.ToggleButton [API 1]
              └── android.widget.Switch [API 14]
```
