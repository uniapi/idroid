# 🏆 Easy Challenge: App Theme Customizer (Two-Way Intents)

## 🎯 The Objective
Your goal is to build a simple **App Theme Customizer** completely in pure Kotlin code (zero XML, zero AndroidX). 

This challenge focuses on the fundamentals of two-way communication between screens.
You will pass a default configuration option from a **Main Screen (`MainActivity`)** to a **Selection Screen (`ThemeActivity`)**,
let the user choose a non-sensitive style configuration (a background color), return that choice as an activity result,
and safely preserve the applied state through device screen rotations.

---

## 🏗️ Step-by-Step Architectural Blueprint

To guarantee implementation success without getting stuck, follow this precise sequence of engineering steps:

### Step 1: Design the Main Screen Layout (`MainActivity`)
1.  **Layout Construction**: Instantiate a vertical `LinearLayout` named `rootLayout`. Inside it, add:
    *   `tvCurrentTheme`: A `TextView` (TextSize `18sp`, default text: `"Active Theme: Default (White)"`, text color `Color.BLACK`).
    *   `btnChangeTheme`: A `Button` (Text: `"Select Custom Background"`).
2.  **State Management**: Create a mutable integer variable `currentBackgroundColor = Color.WHITE`.
3.  **The Lifecycle Logger**: Implement standard `Log.d` lines inside `onCreate`, `onStart`, `onResume`, `onPause`, `onStop`, `onRestart`, and `onDestroy`
to trace exactly how the system manages this screen when a secondary window appears.

### Step 2: Wire the Selection Launch Loop
1.  Define a unique request identifier inside a companion object:
    ```kotlin
    private companion object {
        const val REQUEST_CODE_THEME = 201
    }
    ```
2.  Bind a click listener to `btnChangeTheme` that targets `ThemeActivity` via `startActivityForResult(..., REQUEST_CODE_THEME)`. 

### Step 3: Architect the Customizer Screen (`ThemeActivity`)
This screen does not process secure data; it simply displays non-sensitive styling options:
1.  **Layout Construction**: Create a vertical container with a light gray background (`Color.parseColor("#F5F5F5")`). Add two buttons:
    *   `btnLightGreen`: A `Button` (Text: `"Mint Green"`).
    *   `btnLightBlue`: A `Button` (Text: `"Sky Blue"`).
2.  **Returning the Result**:
    *   Clicking `btnLightGreen` must create an empty `Intent`, attach a primitive integer payload `putExtra("EXTRA_COLOR", Color.parseColor("#E8F5E9"))`,
		set the result to `Activity.RESULT_OK`, and call `finish()`.
    *   Clicking `btnLightBlue` must do the exact same thing but attach `Color.parseColor("#E3F2FD")` as the payload value instead.

### Step 4: Intercept and Apply the Result
1.  Override `onActivityResult()` inside `MainActivity`. 
2.  Verify that `requestCode == REQUEST_CODE_THEME` and `resultCode == Activity.RESULT_OK`.
3.  Extract the integer value using `data?.getIntExtra("EXTRA_COLOR", Color.WHITE)`.
4.  Assign this value to `currentBackgroundColor`, immediately update the root layout's background color via `rootLayout.setBackgroundColor(currentBackgroundColor)`,
and change `tvCurrentTheme.text` to read `"Active Theme: Custom Colored"`.

### Step 5: Protect Against Device Rotations
If a user selects a beautiful Mint Green background and then rotates their phone, the system will recreate the Activity and default it back to plain White. Prevent this bug:
1.  Override `onSaveInstanceState(outState: Bundle)` inside `MainActivity` to save the active `currentBackgroundColor` integer.
2.  Inside `onCreate()`, extract the value if `savedInstanceState != null` and instantly apply it to `rootLayout` during the layout compilation phase.

---

## 🧪 Verification Matrix & Edge-Case Evaluation

To pass the code challenge successfully, verify that your runtime architecture satisfies these structural constraints:

| Operational Test Case | Expected Runtime Behavior | Architecture Verification |
| :--- | :--- | :--- |
| **Color Callback Return** | Selecting "Mint Green" finishes the screen, returns to the main page, and instantly tints the main canvas background. | Data feedback loop via `startActivityForResult` works perfectly. |
| **Rotation State Lock** | Toggling a background color, rotating the phone simulator, and verifying the chosen color persists on screen. | `onSaveInstanceState` state cache loop handles configuration shifts. |
| **Lifecycle Validation** | Leaving the main screen writes `onPause() -> onStop()` to logcat. Returning from the color picker writes `onRestart() -> onStart() -> onResume()`. | Basic lifecycle state boundaries are respected. |

---
