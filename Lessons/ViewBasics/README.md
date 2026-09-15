# Android View Basics

Here is the complete, absolute master list of all core View and ViewGroup components natively available in Android API Level 10 (Android 2.3.3 Gingerbread).
Every single component listed below belongs to the native android.view or android.widget packages, requires zero AndroidX or Support Library dependencies, and is 100% compatible with Kotlin 1.3.
------------------------------
## 📐 1. Structural Layout Containers (ViewGroups)
These are the foundational parent containers used to arrange child views on the screen.

* FrameLayout – Stacks child elements on top of each other like layers. Items align to the top-left by default unless a child's layout_gravity is specified.
* LinearLayout – Aligns child views sequentially in a single row—either vertically or horizontally. Supports proportional space distribution via layout_weight.
* RelativeLayout – Positions child views relative to their parent boundaries (e.g., align to bottom) or relative to structural sibling views (e.g., position below an input field).
* TableLayout – Manages child views into structural rows and columns, functioning exactly like a grid matrix.
* TableRow – A specialized layout container used exclusively inside a TableLayout to define a single row of data cells.
* AbsoluteLayout – Allows positioning of child views using exact X and Y pixel coordinates. (Note: Already marked deprecated in API 10 because it breaks across different screen densities, but fully present in the SDK).

------------------------------
## 📜 2. Scrollable Layout Containers
Since views do not scroll automatically when they exceed the screen boundaries, these containers provide scrolling bounds.

* ScrollView – A layout container that allows a single direct child view (typically a vertical LinearLayout) to be scrolled vertically.
* HorizontalScrollView – Operates identically to a ScrollView, but provides horizontal axis scroll capabilities for its single child layout.

------------------------------
## 📦 3. Basic Content Views (Displaying Data)
These elements are basic UI nodes designed to render static content, graphics, or state data.

* TextView – The text-rendering engine. Manages font styling, sizes, colors, alignment types (gravity), and handles international text glyph strings.
* ImageView – Renders graphic assets, drawables, and bitmap files. Features scaling algorithms like CENTER_CROP, FIT_CENTER, and MATRIX.
* ProgressBar – Displays a visual indicator of a background task's timeline. Supports circular spinning states (indeterminate) or explicit horizontal fill values.

------------------------------
## 🔘 4. Interactive Input Controls (User Actions)
These widgets capture direct user input, selection parameters, and terminal input typing.

* Button – A standard text-clickable element that fires layout click listeners. Features a native metallic-grey gradient background in API 10.
* EditText – An editable extension of TextView that opens the system soft-keyboard. Configurable for explicit fields like passwords, phone numbers, or emails via inputType.
* CheckBox – A dual-state toggle box allowing independent binary selections. Multiple checkboxes can be active at the same time.
* RadioButton – A selection circle button used for mutual exclusivity. It forces a single choice selection when nested inside a RadioGroup.
* RadioGroup – The mandatory structural container used to group RadioButton views together to enforce their exclusive choice logic.
* ToggleButton – A structural state button with an integrated light-up indicator bar displaying clean textual states like "ON" or "OFF".
* SeekBar – An interactive extension of a horizontal ProgressBar adding a draggable physical thumb slider for adjusting numerical scale values.

------------------------------
## 📑 5. Tabbed Multi-Screen Systems
The native structural system used to build multi-tabbed user environments before modern viewpagers or fragments were introduced.

* TabHost – The highest tier parent window component that coordinates tab selection, switching logic, and active layout views.
* TabWidget – The specific horizontal row layout displaying the physical clickable tab targets. Must live structurally inside a TabHost.

------------------------------
## 🗂️ 6. Heavy Legacy Adapter Views (Data Arrays)
These components manage large, complex data arrays and rely heavily on an backing Adapter to bind dataset items into scrollable recycling rows.

* ListView – Displays a vertically scrollable, single-column collection list of recycling row elements.
* GridView – Displays rows and columns of data elements in a rigid two-dimensional scrollable grid matrix.
* Spinner – The classic native Android dropdown asset. Triggers a modal popup window list allowing single-choice array selections.
* Gallery – A horizontally scrolling carousel element that locks the active item directly to the center axis of the viewport.

------------------------------
## 📅 7. Native System Pickers
Embedded calendar and clock layouts utilized to ensure standard date and time selections.

* DatePicker – An internal grid interface allowing users to systematically pick a calendar Month, Day, and Year.
* TimePicker – An interactive clock widget allowing users to configure explicit Hours, Minutes, and AM/PM state variables.

------------------------------
## ⚡ 8. Advanced Embedded Multimedia Elements
Specialized heavyweight view subsystems running complex rendering architectures under the hood.

* WebView – An embedded native WebKit rendering engine layout used to compile raw HTML text or load live external URLs.
* VideoView – A specialized video surface viewport configured to decode, stream, and play media files from web paths or local application resources.

------------------------------
## 💡 API 10 Environment Tip
When initializing any of these elements programmatically in Kotlin 1.3, remember that layout parameters must always match the parent container:

// Example: Adding an API 10 EditText to a LinearLayout
    val input = EditText(this)val params = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    )
    input.layoutParams = params

------------------------------
## 🌿 Inheritance Tree

java.lang.Object
 ↳ android.view.View
    ↳ android.widget.ImageView
    ↳ android.widget.ProgressBar
       ↳ android.widget.AbsSeekBar
          ↳ android.widget.SeekBar
    ↳ android.view.SurfaceView
       ↳ android.widget.VideoView
    ↳ android.widget.TextView
       ↳ android.widget.EditText
       ↳ android.widget.Button
          ↳ android.widget.CompoundButton
             ↳ android.widget.CheckBox
             ↳ android.widget.RadioButton
             ↳ android.widget.ToggleButton
    ↳ android.view.ViewGroup
       ↳ android.widget.AbsoluteLayout
          ↳ android.webkit.WebView
       ↳ android.widget.RelativeLayout
       ↳ android.widget.LinearLayout
          ↳ android.widget.TableLayout
          ↳ android.widget.TableRow
          ↳ android.widget.RadioGroup
          ↳ android.widget.TabWidget
       ↳ android.widget.FrameLayout
          ↳ android.widget.ScrollView
          ↳ android.widget.HorizontalScrollView
          ↳ android.widget.TabHost
          ↳ android.widget.DatePicker
          ↳ android.widget.TimePicker
       ↳ android.widget.AdapterView
          ↳ android.widget.AbsListView
             ↳ android.widget.ListView
             ↳ android.widget.GridView
          ↳ android.widget.AbsSpinner
             ↳ android.widget.Spinner
             ↳ android.widget.Gallery

