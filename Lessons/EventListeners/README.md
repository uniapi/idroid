## 🎧Android Event Listeners: The Complete Master Reference
Here is the complete, exhaustive master directory of all native **Android Event Listeners and Interface Handlers** from the standard AOSP SDK (`android.view`, `android.widget`, `android.text`).

Every component is explicitly mapped with its structural introduction **[API Level]**, categorized by logical execution zones,
and designed for 100% syntactic compliance with a pure-code, zero-XML, no-AndroidX Kotlin architecture.

------------------------------
In a pure programmatic environment, user interaction is managed by binding instance implementations of functional platform interfaces directly to view nodes.
This comprehensive document catalogs every native listener engine available to handle tactile spatial inputs, data mutations, text variations, layout state shifts, and system hardware lifecycle responses.

------------------------------
## 📐 1. Structural Layout & Window Focus Event Handlers
These listeners monitor spatial adjustments, view dimension measurements, drawing phases, and visibility focal transitions across the application's graphical tree matrix.
* **`View.OnLayoutChangeListener` [API 11]** – Intercepts exactly when a view's bounding layout layout limits change due to a measurement calculation pass.
Delivers specific parameters outlining the precise layout coordinates: `(v: View?, left: Int, top: Int, right: Int, bottom: Int, oldLeft: Int, oldTop: Int, oldRight: Int, oldBottom: Int)`.
* **`View.OnFocusChangeListener` [API 1]** – Triggers immediately whenever an interactive node gains or loses focus state attention.
Delivers a binary boolean flag tracking focus tracking loops: `(v: View?, hasFocus: Boolean)`.
* **`ViewTreeObserver.OnGlobalLayoutListener` [API 1]** – Intercepts layout phase changes globally across the entire viewport hierarchy.
Essential in pure code to extract actual hardware rendering widths/heights before drawing logic initiates.
* **`ViewTreeObserver.OnPreDrawListener` [API 1]** – Fires immediately before a graphics layout frame is written to the display subsystem.
Returns a boolean; returning `false` completely aborts the frame pass to reschedule animations.
* **`View.OnAttachStateChangeListener` [API 12]** – Intercepts exactly when a structural component is safely linked to or unbound from its physical active window context framework.
Dispatches two explicit methods: `onViewAttachedToWindow(v: View?)` and `onViewDetachedFromWindow(v: View?)`.

------------------------------
## 📜 2. Scrollable Viewport & Window State Transits
These platform hooks allow background layout pipelines to listen to sliding offsets, swipe gesture movements, and scroll velocity distributions across viewport containers.

* **`View.OnScrollChangeListener` [API 23]** – Provides streaming telemetry data charting precise horizontal and vertical offset variations across
scrolling frameworks like `ScrollView` or `HorizontalScrollView`. Dispatches layout updates via: `(v: View?, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int)`.
* **`AbsListView.OnScrollListener` [API 1]** – A multi-method monitor interface bound to scrolling adapter containers (`ListView`).
Tracks operational states (`SCROLL_STATE_FLING`, `SCROLL_STATE_TOUCH_SCROLL`) and viewport item visibility offsets.

------------------------------
## ⌨️ 3. Text Streams & Native Keyboard Entry Handlers
Specialized monitoring frameworks designed to parse real-time character mutations, formatting operations, soft keyboard entries, and physical hardware keystrokes.

* **`android.text.TextWatcher` [API 1]** – An abstract contract tracking text modifications on `EditText`.
Since it contains three specific method branches, it cannot utilize single lambda SAM conversion loops:
	* `beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int)`
	* `onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int)`
	* `afterTextChanged(s: Editable?)`
* **`TextView.OnEditorActionListener` [API 3]** – Intercepts action triggers dispatched from software IME input boards
(e.g., catching when a user strikes the soft keyboard "Done", "Search", or "Next" actions). Dispatches input via: `(v: TextView?, actionId: Int, event: KeyEvent?): Boolean`.
* **`View.OnKeyListener` [API 1]** – Intercepts raw physical key codes processed from external keyboards or dedicated physical devices buttons
before they are consumed by individual layout elements. Dispatches inputs via: `(v: View?, keyCode: Int, event: KeyEvent?): Boolean`.

------------------------------
## 🔘 4. Touch Gestures & Direct Interactive Controls
Atomic user interaction listeners used to catch standard single taps, ongoing system activations, long layout state presses, or raw pixel touch matrices.

* **`View.OnClickListener` [API 1]** – The foundational interaction trigger. Captures simple, swift tap sequences on interactive nodes: `(v: View?)`.
* **`View.OnLongClickListener` [API 1]** – Monitored continuous element hold states. Enforces a strict boolean completion return statement: `(v: View?): Boolean`.
* **`View.OnTouchListener` [API 1]** – High-velocity raw canvas tracking tool. Passes deep structural control parameters directly out from hardware touch monitors.
Must map nullable signatures explicitly to block application crashes: `(v: View?, event: MotionEvent?): Boolean`.
* **`View.OnContextClickListener` [API 23]** – Listens specifically for targeted right-clicks or peripheral stylus input operations executing on active user nodes: `(v: View?): Boolean`.
* **`View.OnHoverListener` [API 14]** – Intercepts movement streams tracking precise pointer trajectories when pointer inputs hover directly over a view segment boundaries:
`(v: View?, event: MotionEvent?): Boolean`.

------------------------------
## 🗂 5. Compound Switches & Dynamic Adaptive Handlers
Listeners dedicated to structural components that modify active selections, cycle binary check marks, click data tables, or manipulate progress lines.

* **`CompoundButton.OnCheckedChangeListener` [API 1]** – Listens to structural status updates generated when elements
such as `Switch`, `CheckBox`, or `ToggleButton` shift their internal state loops: `(buttonView: CompoundButton?, isChecked: Boolean)`.
* **`RadioGroup.OnCheckedChangeListener` [API 1]** – Tracks selection events within a native `RadioGroup`
to output the exact dynamic unique ID of the currently toggled button element: `(group: RadioGroup?, checkedId: Int)`.
* **`SeekBar.OnSeekBarChangeListener` [API 1]** – Captures drag changes executed over continuous slider dimensions. Maps three critical method overrides:
	* `onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean)`
	* `onStartTrackingTouch(seekBar: SeekBar?)`
	* `onStopTrackingTouch(seekBar: SeekBar?)`
* **`AdapterView.OnItemClickListener` [API 1]** – Hooks directly to dynamic adapter lists (`ListView`, `Spinner`)
to pinpoint item click indexing coordinates across external data sources: `(parent: AdapterView<*>?, view: View?, position: Int, id: Long)`.
* **`AdapterView.OnItemLongClickListener` [API 1]** – Tracks long-press selections applied over structural item list matrices:
`(parent: AdapterView<*>?, view: View?, position: Int, id: Long): Boolean`.
* **`AdapterView.OnItemSelectedListener` [API 1]** – Listens for positional selections inside adapter dropdown views,
capturing when an option is selected or when the row matrix defaults to empty. Requires overriding `onItemSelected(...)` and `onNothingSelected(...)`.

------------------------------
## 🌿 Complete Inheritance & Signature Reference Hierarchy
The architecture diagram below displays exactly how these structural event-listener definitions exist natively under the primary platform framework,
complete with their explicit Kotlin trailing lambda parameter signatures.
```kotlin
android.view.View
│
├── Interfaces bound directly via setOn...Listener
│   ├── View.OnClickListener                   ➔ (View?) -> Unit
│   ├── View.OnLongClickListener               ➔ (View?) -> Boolean
│   ├── View.OnTouchListener                   ➔ (View?, MotionEvent?) -> Boolean
│   ├── View.OnKeyListener                     ➔ (View?, Int, KeyEvent?) -> Boolean
│   ├── View.OnFocusChangeListener             ➔ (View?, Boolean) -> Unit
│   ├── View.OnLayoutChangeListener            ➔ (View?, Int, Int, Int, Int, Int, Int, Int, Int) -> Unit
│   ├── View.OnHoverListener                   ➔ (View?, MotionEvent?) -> Boolean
│   └── View.OnContextClickListener            ➔ (View?) -> Boolean
│
└── Subclassed Component Interfaces
    ├── android.widget.TextView
    │   └── TextView.OnEditorActionListener    ➔ (TextView?, Int, KeyEvent?) -> Boolean
    │
    ├── android.widget.CompoundButton
    │   └── CompoundButton.OnCheckedChangeListener ➔ (CompoundButton?, Boolean) -> Unit
    │
    ├── android.widget.RadioGroup
    │   └── RadioGroup.OnCheckedChangeListener ➔ (RadioGroup?, Int) -> Unit
    │
    └── android.widget.AdapterView
        ├── AdapterView.OnItemClickListener    ➔ (AdapterView<*>?, View?, Int, Long) -> Unit
        ├── AdapterView.OnItemLongClickListener➔ (AdapterView<*>?, View?, Int, Long) -> Boolean
        └── AdapterView.OnItemSelectedListener ➔ Requires: onItemSelected() & onNothingSelected()
```
## Event Handlers
In the Android platform, **Event Handling** is the mechanism through which an application intercept and responds to hardware-level user interactions,
such as screen taps (clicks), long presses, text input, or physical drags.

Because we are working **strictly in pure Kotlin code without XML configurations or external library dependencies** **(`androidx`)**,
event binding happens by directly assigning standard platform interface listeners to the view objects.
Kotlin simplifies this workflow via **SAM (Single Abstract Method) Conversion**
which automatically compiles clean trailing lambda expressions into the required underlying anonymous Java listener classes.

------------------------------
## ⚡ TextWatcher Implementation
Because TextWatcher has three distinct methods, it cannot use a simple Kotlin lambda (SAM conversion). Instead, you must instantiate it using an anonymous object declaration `(object : TextWatcher)`.
```kotlin
val inputField = EditText(this).apply {
	layoutParams = LinearLayout.LayoutParams(
		LinearLayout.LayoutParams.MATCH_PARENT,
		LinearLayout.LayoutParams.WRAP_CONTENT
	)
	hint = "Type a password (min 6 chars)"
}

val errorText = TextView(this).apply {
	layoutParams = LinearLayout.LayoutParams(
		LinearLayout.LayoutParams.WRAP_CONTENT,
		LinearLayout.LayoutParams.WRAP_CONTENT
	).apply { topMargin = 16 }
	text = "Too short"
	setTextColor(Color.RED)
}

// Connecting TextWatcher via an anonymous object implementation
inputField.addTextChangedListener(object : TextWatcher {
	override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
		// Invoked immediately BEFORE the text changes in memory
	}

	override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
		// Invoked AS the characters are mutating. 
		// We use safe calls 's?.length' due to platform nullability rules
		val currentLength = s?.length ?: 0
		if (currentLength >= 6) {
			errorText.text = "Password secure"
			errorText.setTextColor(Color.parseColor("#2E7D32")) // Green
		} else {
			errorText.text = "Too short (${6 - currentLength} characters left)"
			errorText.setTextColor(Color.RED)
		}
	}

	override fun afterTextChanged(s: Editable?) {
		// Invoked immediately AFTER modifications are applied to the editable buffer
	}
})

```
## Summary Rule of Thumb
If an interface has **only 1 method** (like `OnClickListener`), you can omit `object` and use a clean lambda: `button.setOnClickListener { ... }`.
```kotlin
// 1. Declare the listener variable using a clean lambda expression
val buttonListener = View.OnClickListener { view ->
    // Your click handling code here
    Toast.makeText(this, "Button Clicked!", Toast.LENGTH_SHORT).show()
}

// 2. Pass the variable to the target button element
button.setOnClickListener(buttonListener)
```
If an interface has **2 or more methods** (like `SeekBar.OnSeekBarChangeListener` or `TextWatcher`), you must use the `object :` syntax.
```kotlin
val volumeChangeListener = object : SeekBar.OnSeekBarChangeListener {
    // You are forced to implement the entire multi-method contract here
    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {}
    override fun onStartTrackingTouch(seekBar: SeekBar?) {}
    override fun onStopTrackingTouch(seekBar: SeekBar?) {}
}
```
------------------------------
## 🛠️ Kotlin Syntactic Features for Native Event Handlers
When writing interfaces entirely in raw programmatic code lines, capitalize on these distinct Kotlin compilation behaviors to keep your files lean:

1. Implicit Single Parameter Named `it`: If your callback logic does not need to parse specific properties of the executing sender object,
you can discard the named argument syntax completely. Kotlin binds single-argument parameters to an implicit reference keyword called it:
```kotlin
   button.setOnClickListener {
       it.isEnabled = false // 'it' acts as a direct reference to the target 'button' object
   }
```   
2. The Underscore `_` Placeholder Argument: When consuming interface overrides that deliver multiple parameters (such as focus listener states or coordinate changes)
but you only require a specific single value, use an underscore block to omit unneeded items. This informs the compiler optimization paths to drop structural allocations:
```kotlin   
   // The view variable instance is dropped; code execution isolates strictly onto the boolean hasFocus state
   inputField.setOnFocusChangeListener { _, hasFocus -> 
       if (hasFocus) { /* Execute focus updates */ }
   }
```
### 📝 Key Takeaways
* **`setOnKeyListener`** vs **`onKeyDown`**: `setOnKeyListener` intercepts events targeted at a specific focused element,
while `onKeyDown` acts globally on the `Activity` layer.
* **Returning Boolean (Event Consumption)**: Returning `true` tells the Android system that your code completely managed the action,
preventing default system behavior.
* **Kotlin Syntax Optimization**: Utilizing the underscore `_` syntax drops the implicit `View` reference variable instantiation, reducing local overhead.
