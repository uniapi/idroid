# Challenge 2: Custom Thread Pool Progress Matrix (Advanced)

## 🎯 Goal
Build a multi-lane dashboard Activity entirely in code (no XML, no AndroidX) that manually orchestrates a pool of parallel background worker threads.
The UI must display multiple distinct progress tracking bars running simultaneously.
You must handle complex thread safety, shared counter states, and multi-threaded interface updates manually using raw `Handler` matrices.

---

## 🏗️ Technical Specification & Architecture

### 1. View & Structural Dashboard Hierarchy
Create a dynamic multi-item UI entirely inside the Activity code within `onCreate`:
*   Use a vertical root layout container (`android.widget.LinearLayout` prefixed as `llRoot`).
*   Instantiate a top-level **Control Panel** container (`android.widget.LinearLayout` prefixed as `llControlPanel`) containing two interactive elements:
    *   `Button` labeled "Spawn Worker Lane" prefixed as `btnSpawnWorker`
    *   `TextView` acting as a global master metric label prefixed as `tvSummaryMetrics` (e.g., "ACTIVE THREADS: 0 | ACCUMULATED STEPS: 0").
*   Beneath the control panel, embed a secondary vertical container layout prefixed as `llDynamicLaneContainer` that acts as a target list for row layout items.

### 2. Custom Thread Pool Mechanics
When the user clicks `btnSpawnWorker`:
1.  Enforce a hard maximum safety limit of **4 concurrent active background threads** running at the same time. If the limit is reached, change `btnSpawnWorker` styles to warn the user.
2.  Dynamically instantiate a new horizontal row container (`android.widget.LinearLayout` prefixed as `llRowLayout`) representing that specific background worker lane. The row must contain:
    *   A text description label prefixed as `tvLaneLabel` (e.g., "Lane #3 [000%]").
    *   A linear progress bar indicator prefixed as `pbLaneProgress` (`android.widget.ProgressBar`).
3.  Instantiate and launch an isolated `java.lang.Thread` bound strictly to that row instance.
4.  Inside the worker thread, calculate a variable speed payload (delays generated randomly between 40ms and 180ms per step) to ensure threads run independently.

### 3. Multi-Threaded Main Interception & Thread Safety
*   **The Shared State Problem:** Multiple independent worker threads will concurrently attempt to increment global analytics values.
You *cannot* safely increment a standard primitive `Int` simultaneously from separate threads without causing race conditions.
*   **The Low-Level Solution:** You must use explicit atomic variables (`java.util.concurrent.atomic.AtomicInteger`) to handle multi-threaded state modifications safely.
*   The worker lane must continuously forward its localized percentage progress to `pbLaneProgress` and `tvLaneLabel`, and alter the global `tvSummaryMetrics` dynamically.

---

## 🔍 Step-by-Step Architecture Guide

To construct this system without getting stuck, map your development into modular phases:

### Step 1: Initialize Atomic and Synchronization Controls
In your activity scope under the `localhost.idroid.salamun` namespace, maintain state integrity:
```kotlin
package localhost.idroid.salamun

private val hMain = Handler(Looper.getMainLooper())

// Thread-safe integer storage mechanics
private val activeWorkersCount = AtomicInteger(0)
private val totalProcessedUnits = AtomicInteger(0)

private const val MAX_THREADS = 4
```

### Step 2: Implement Dynamic UI Row Generation
Build a dedicated helper function within your pure-code activity capable of injecting visual rows dynamically using proper notation constraints:
```kotlin
private fun createWorkerRowUI(workerId: Int): Pair<LinearLayout, Pair<TextView, ProgressBar>> {
    val context = this
    val llRowLayout = LinearLayout(context).apply {
        orientation = LinearLayout.HORIZONTAL
        setPadding(0, 20, 0, 20)
    }
    
    val tvLaneLabel = TextView(context).apply {
        text = "Lane #$workerId [000%]"
        textSize = 14f
    }
    
    val pbLaneProgress = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
    
    llRowLayout.addView(tvLaneLabel)
    llRowLayout.addView(pbLaneProgress)
    return Pair(llRowLayout, Pair(tvLaneLabel, pbLaneProgress))
}
```

### Step 3: Orchestrate Parallel Thread Pumping
Inside your spawn button listener, validate space availability, adjust your atomic states, and instantiate your explicit execution thread container:
```kotlin
if (activeWorkersCount.get() >= MAX_THREADS) return@setOnClickListener

val currentId = activeWorkersCount.incrementAndGet()
val (llRow, views) = createWorkerRowUI(currentId)
val tvLabel = views.first
val pbProgress = views.second
llDynamicLaneContainer.addView(llRow)

Thread {
    val computationalDelay = (40..180).random().toLong()
    
    for (progressStep in 1..100) {
        try {
            Thread.sleep(computationalDelay)
        } catch (e: InterruptedException) {
            break
        }

        totalProcessedUnits.incrementAndGet()

        // Safeguard thread traversal bounds
        hMain.post {
            pbProgress.progress = progressStep
            tvLabel.text = String.format("Lane #%d [%03d%%]", currentId, progressStep)
            updateGlobalTelemetryDisplay() // Modifies tvSummaryMetrics
        }
    }

    // Task Finalization Sequence
    activeWorkersCount.decrementAndGet()
    hMain.post {
        tvLabel.text = "Lane #$currentId [DONE]"
        updateSpawnButtonState() // Alters btnSpawnWorker properties
    }
}.start()
```

---

## 🧪 Verification & Acceptance Criteria
1.  **No High-Level Utility Libraries:** Code uses raw `java.lang.Thread` structures exclusively. Do not import `ExecutorService` or AndroidX tools.
2.  **Shorthand Prefix Enforcement:** All component items utilize explicit semantic labels (`hMain`, `tvSummaryMetrics`, `btnSpawnWorker`, `llDynamicLaneContainer`, `llRowLayout`, `tvLaneLabel`, `pbLaneProgress`).
3.  **Concurrency Isolation:** Multiple visual worker rows scale their `pbLaneProgress` trackers completely independently of one another at the same time.
4.  **Atomic State Integrity:** The `tvSummaryMetrics` display shows absolute arithmetic precision when computing multi-threaded counter updates.
