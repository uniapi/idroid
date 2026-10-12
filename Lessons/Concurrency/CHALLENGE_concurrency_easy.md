# Challenge 1: Pure-Code Progress Counter Engine (Easy)

## 🎯 Goal
Build a single-screen Activity entirely in code (no XML layouts, no AndroidX) that spawns a manual background thread to simulate a heavy processing engine.
The background worker must communicate safely back to the Main Thread via an `android.os.Handler` to update an active textual indicator without blocking user layout interactions.

---

## 🏗️ Technical Specification & Architecture

### 1. View & Structural Hierarchy
Create your UI entirely inside the Activity code within `onCreate`:
*   Use a manual root layout container (`android.widget.LinearLayout` prefixed as `llRoot`).
*   Instantiate an interactive `android.widget.Button` prefixed as `btnStart` to trigger the operation.
*   Instantiate an `android.widget.TextView` prefixed as `tvStatus` to display the real-time operational processing metrics.
*   Bind the configuration together dynamically using `.addView()` and apply it via `setContentView(llRoot)`.

### 2. Concrete Thread Mechanics
When the user clicks the button:
1.  Verify if a background operation is already actively executing. If it is active, safely ignore the second press or reset the count to enforce execution safety.
2.  Instantiate a raw `java.lang.Thread`.
3.  Inside the thread, implement an active loop counting from `1` to `100`.
4.  Imitate a system load inside each step of the loop by holding execution for 100 milliseconds via `Thread.sleep(100)`.

### 3. Main-Thread Interception
*   Instantiate an `android.os.Handler` targeting `Looper.getMainLooper()` prefixed as `hMain`.
*   Inside the background thread loop, use `hMain` to post a text modification runnable back to the layout elements. 
*   **Do not directly call view methods from inside the thread loop block!**

---

## 🔍 Step-by-Step Implementation Map

To prevent getting stuck, structure your code step-by-step:

### Step 1: Establish Layout Elements
```kotlin
val context = this
val llRoot = LinearLayout(context).apply {
    orientation = LinearLayout.VERTICAL
    gravity = Gravity.CENTER
}

val tvStatus = TextView(context).apply {
    text = "Engine Idle. Awaiting Start."
    textSize = 18f
}

val btnStart = Button(context).apply {
    text = "Execute Background Task"
}

llRoot.addView(tvStatus)
llRoot.addView(btnStart)
setContentView(llRoot)
```

### Step 2: Set up the Communication Bridge
Outside your button listeners, declare your handler tracking the main platform thread loop:
```kotlin
private val hMain = Handler(Looper.getMainLooper())
private var isTaskRunning = false
```

### Step 3: Implement the Worker Loop and Double-Trigger Shield
Inside your `btnStart.setOnClickListener`:
```kotlin
if (isTaskRunning) return@setOnClickListener
isTaskRunning = true

Thread {
    for (progress in 1..100) {
        try {
            Thread.sleep(100) // Mock calculation payload
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        // Jump safely across the thread barrier
        hMain.post {
            tvStatus.text = "Processing Telemetry Data: $progress%"
            
            if (progress == 100) {
                tvStatus.text = "Task Complete! Core Processed Cleanly. ✅"
                isTaskRunning = false
            }
        }
    }
}.start()
```

### Step 4: Handle Framework Component Lifecycle Leaks
Ensure that if the user closes or exits the activity before the loop wraps up, pending updates are cleared:
```kotlin
override fun onDestroy() {
    super.onDestroy()
    // Wipe out any un-executed updates remaining in the loop buffer
    hMain.removeCallbacksAndMessages(null)
}
```

---

## 🧪 Verification & Acceptance Criteria
1.  **No XML or AndroidX:** Code compiles cleanly utilizing raw framework imports under the `localhost.idroid.salamun` package layer.
2.  **Explicit Naming Compliance:** Variables utilize proper type prefixes (`hMain`, `tvStatus`, `btnStart`, `llRoot`).
3.  **Thread Stability:** The app does not crash with a `CalledFromWrongThreadException`.
4.  **UI Responsiveness:** The user interface layout stays fully active and fluid while processing; the button is clickable, and text handles runtime progression smoothly.
5.  **No Double-Trigger Overhead:** Pressing the execution action repeatedly during active progress cycles does not spawn duplicate overlapping counter calculations.
