# 📡 Intents & Activity Lifecycle: The Core Communication Bus

This reference manual provides an exhaustive architectural breakdown of the **Android Activity Lifecycle** and the **Intent Communication Subsystem**
within the native AOSP SDK (`android.app`, `android.content`). 

In a pure-code architecture that bypasses XML and Jetpack dependencies, mastering screen states and explicit boundary mapping is essential for writing memory-safe, stable apps.

---

## 🌿 1. Activity Lifecycle: The Micro-Architecture

An `Activity` does not control its own birth or death. The Android operating system orchestrates its lifecycle by invoking precise callback routines depending on hardware configurations,
system memory constraints, and user navigation states.

```	text
       [ Activity Launched ]
                 │
                 ▼
          ┌─────────────┐
          │  onCreate() │  ◄── (Memory Allocation, Programmatic View Construction)
          └──────┬──────┘
                 ▼
          ┌─────────────┐  ◄── [ User Returns to Screen ] ──┐
          │  onStart()  │                                   │
          └──────┬──────┘                                   │
                 ▼                                          │
          ┌─────────────┐                                   │
          │  onResume() │  ◄── (Foreground Interaction Loop / Active State)
          └──────┬──────┘                                   │
                 │                                          │
           [ Activity is Running ]                          │
                 │                                          │
                 ▼                                          │
          ┌─────────────┐                                   │
          │  onPause()  │  ◄── (Partial Obscurity - e.g., dialog popover overlay)
          └──────┬──────┘                                   │
                 ▼                                          │
          ┌─────────────┐                                   │
          │   onStop()  │  ◄── (Complete Invisibility / Safe to drop heavy assets)
          └──────┬──────┘                                   │
                 ├──────────────────────────────────────────┘
                 ▼
          ┌─────────────┐
          │ onRestart() │  ◄── (Transitional hook right before onStart() re-fires)
          └──────┬──────┘
                 ▼
          ┌─────────────┐
          │ onDestroy() │  ◄── (Final Deallocation / View references cleaned)
          └─────────────┘
```

### Core Callback Responsibilities in Pure Code:
*   **`onCreate(Bundle?)`**: Executed exactly once per execution lifecycle loop. This is where you instantiate programmatic view hierarchies (`LinearLayout`, `ListView`)
and bind them to the screen via `setContentView()`.
*   **`onStart()`**: The view hierarchy enters the window layout tree and becomes visually trackable on screen.
*   **`onResume()`**: The optimal hook to initiate active visual operations, start non-heavy UI handlers, or subscribe to input sensors.
*   **`onPause()`**: Executed when the window loses focal interactive priority but remains partially visible. You must immediately freeze processing loops or UI animations here.
*   **`onStop()`**: The screen is completely invisible. This is the last safe boundary to release heavy hardware hooks (e.g., active GPS positioning, raw background tasks).
*   **`onRestart()`**: Executed exclusively when an Activity transition state moves from being completely obscured/invisible (`onStop()`) back into the active foreground processing pipe.
It acts as a specialized transitional hook right before `onStart()` fires again. Use this method to re-verify transient configurations or system flags
(e.g., checking if user session credentials changed while the app was minimized) without re-running the entire heavy memory setup inside `onCreate()`.
*   **`onDestroy()`**: The operating system is fully reclaiming the class reference framework. In pure code, you must nullify references to long-lived objects or adapters here to prevent heavy memory leaks.

---

## 💾 2. Hard State Retention Management

When a device rotates, the system completely tears down the active Activity instance (`onPause` -> `onStop` -> `onDestroy`)
and instantiates a brand new one from scratch (`onCreate` -> `onStart` -> `onResume`). 

Because we bypass XML, views do not have automatic ID-state persistence engines. We must explicitly hook into state save buffers manually.

```kotlin
override fun onSaveInstanceState(outState: Bundle) {
    super.onSaveInstanceState(outState)
    // Cache primitive states inside the outState system dictionary
    outState.putString("KEY_INPUT_TEXT", currentInputString)
    outState.putInt("KEY_SELECTED_INDEX", activeIndex)
}

override fun onRestoreInstanceState(savedInstanceState: Bundle) {
    super.onRestoreInstanceState(savedInstanceState)
    // Re-apply states to your programmatic view variables
    currentInputString = savedInstanceState.getString("KEY_INPUT_TEXT", "")
    activeIndex = savedInstanceState.getInt("KEY_SELECTED_INDEX", 0)
    
    myProgrammaticTextView.text = currentInputString
}
```

---

## 📡 3. Intents: The System Communication Bus

An `Intent` is an abstract description of an operation to be performed. It acts as the messaging catalyst between components.

### A. Explicit Intents (Internal View Swapping)
Used to target an explicit destination class inside your application module package layout.

```kotlin
// Navigation from the host Activity context directly to the Dashboard screen
val targetIntent = Intent(this, DashboardActivity::class.java).apply {
    putExtra("EXTRA_USER_ROLE", "ADMIN")
    putExtra("EXTRA_SECURE_TOKEN", 884021L)
}
startActivity(targetIntent)
```

### B. Explicit Intents for Result (Two-Way Communication Loop)
When you need to launch a child Activity and receive data back from it upon its closure, use `startActivityForResult()`
in combination with the `onActivityResult()` interceptor callback.

#### 1. Launching the Child Screen for a Result from the Parent Activity:
```kotlin
private companion object {
    const val REQUEST_CODE_VERIFY = 101 // Unique integer key to identify this specific request
}

val targetIntent = Intent(this, VerifyActivity::class.java)
startActivityForResult(targetIntent, REQUEST_CODE_VERIFY)
```

#### 2. Returning the Data Payload from the Child Activity before finishing:
```kotlin
val returnIntent = Intent().apply {
    putExtra("EXTRA_RESULT_TOKEN", "AUTH_VALID_2026")
}
setResult(Activity.RESULT_OK, returnIntent) // Set the structural result code status
finish() // Destroys the child activity and pops the user back to the parent screen
```

#### 3. Intercepting the Returned Payload inside the Parent Activity:
```kotlin
override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    
    // Verify that the callback matches our unique request identifier and completed successfully
    if (requestCode == REQUEST_CODE_VERIFY && resultCode == Activity.RESULT_OK) {
        val verificationToken = data?.getStringExtra("EXTRA_RESULT_TOKEN")
        Log.d(TAG, "Intercepted verified callback token: \$verificationToken")
    }
}
```

### C. Implicit Intents (System-Wide Service Requests)
Does not name a specific destination class. Instead, it declares a general system action to perform,
leaving it up to the Android operating system to resolve an external application handler.

```kotlin
// Requesting the platform layer to open a specific destination URL link inside a browser
val webIntent = Intent(Intent.ACTION_VIEW).apply {
    data = android.net.Uri.parse("https://github.com")
}

// Security Guardrail: Always verify that the device has an app capable of handling this action
if (webIntent.resolveActivity(packageManager) != null) {
    startActivity(webIntent)
} else {
    Log.e(TAG, "Implicit Intent Failure: No browser application found on this device")
}
```


---

## 📝 5. Key Takeaways
* **Explicit Destructive Leaks**: Never pass static variables or hard-cached Activity `Context` references into deep data models.
If an Activity is destroyed during a device rotation, but a data class retains its old `Context` pointer, the entire view hierarchy leaks out of memory tracking.
* **The Intent Payload Bound**: Explicit intents communicate parameters via a dictionary wrapper called `Extras`.
While it accepts arbitrary primitive arrays, it has a total system constraint allocation limit of **1MB**.
Attempting to pass raw bitmap images or massive database array sequences through a `putExtra()` call will instantly crash your application loop with a `TransactionTooLargeException`.
* **Transient Syncing via `onRestart()`**: Unlike `onCreate()`, which runs initialization from empty buffers, `onRestart()` lets you capture when the application window is re-exposed.
This makes it ideal for updating security session properties or refreshing layout content elements that might have changed while the user minimized the application.
