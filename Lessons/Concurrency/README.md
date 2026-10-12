# 🧵 Concurrency, Loopers & Handlers

Welcome to the low-level Android Concurrency module. In this lesson, we strip away all AndroidX abstractions (such as Coroutines, LiveData, and WorkManager)
to explore how the Android Operating System handles asynchronous work and thread communication entirely through raw system primitives.

---

## 🔬 Core Theory: The Under-the-Hood Architecture

### 1. The Main Thread (UI Thread)
When an Android application launches, the system spawns a single Linux process running an execution sequence called the **Main Thread**.
This thread is solely responsible for rendering the layout, dispatching window events, and mapping visual components onto the screen canvas. 

*   **The Golden Rule:** Never block the Main Thread. Doing heavy work (disk operations, deep counting, networking) here freezes the entire system UI loop.
If the UI thread is frozen for more than 5 seconds, the OS forcefully terminates your application with an **ANR (Application Not Responding)** dialog.
*   **The UI Constraint:** Android components are not thread-safe. You cannot modify, mutate, or alter any layout `View` from outside the Main Thread.
Doing so immediately throws a runtime `CalledFromWrongThreadException`.

### 2. Message Primitives: Message, Looper & MessageQueue
To shift heavy computational steps away from the UI thread while preserving the ability to update layout elements, Android uses an event-loop pattern driven by three components:
*   **Message:** A data transport object containing a variable payload (`what`, `arg1`, `arg2`, `obj`) or a runnable execution block.
*   **MessageQueue:** A sequential, FIFO (First-In, First-Out) data buffer that holds messages ready for dispatch.
*   **Looper:** A persistent background pump. It runs an infinite execution loop (`while(true)`) on its assigned thread, monitoring the `MessageQueue`
and extracting incoming requests to send to their matching handlers.

By default, worker threads do not have a `Looper`. The Main Thread, however, has a specialized `Looper.getMainLooper()` prepared for it automatically at launch.

### 3. The Thread Bridge: Handlers
A `Handler` is the dedicated API wrapper used to interact with a thread's message pipeline. It serves two distinct purposes:
1.  **Enqueueing:** Allowing background worker threads to safely push operations into a target thread's `MessageQueue` using commands like `.post(Runnable)` or `.sendMessage(Message)`.
2.  **Execution:** Processing those same blocks once the target thread's `Looper` pumps them out of the queue.

**Yes**, `Handler` **is the only component in the Android framework that can pass** `Message` and `Runnable` **tasks into a** `Looper`'s **`MessageQueue`**.

Under the hood, even when you use other native high-level Android concurrency utilities, they all secretly use a `Handler` as their underlying transport layer to interact with the loop.

The only other object that can execute custom logic inside a `Looper`'s execution cycle without being directly posted by a `Handler` is a native interface called **`MessageQueue.IdleHandler`**.
```kotlin
// Retrieve the queue from the current Looper
val queue = Looper.myQueue()

// Register an IdleHandler
queue.addIdleHandler {
    // This code runs automatically when the thread has nothing else to do!
    myTextView.text = "Thread is currently idle. Performing low-priority cleanup..."
    
    false // Return false to remove it, or true to keep it active for the next idle cycle
}
```
---

## 🛠️ The Pure-Code Tooling Set

Since we operate **strictly without XML layouts or AndroidX components**, you will build your asynchronous pipelines using the following native SDK components:

### A. Raw Worker Threads
Spawning a manual worker execution loop:
```kotlin
Thread {
    // Heavy execution runs safely isolated here
}.start()
```


### B. UI-Bound Handlers
Instantiating a bridge explicitly coupled to the UI loop:
```kotlin
// Deprecated: Handler() without arguments (omits explicit context)
// Correct Pure-Code Syntax:
val uiHandler = Handler(Looper.getMainLooper())

uiHandler.post {
    // This block is safely forwarded and executed directly on the Main UI Thread!
    myTextView.text = "File successfully saved!"
}
```

### C. Overriding `handleMessage`
```kotlin
// The traditional way to process messages
val workerHandler = object : Handler(Looper.myLooper()!!) {
    override fun handleMessage(msg: Message) {
        when (msg.what) {
            1 -> {
                // Do background task A (e.g., File Write)
            }
            2 -> {
                // Do background task B
            }
        }
    }
}
```

### D. Using `HandlerThread`
```kotlin
import android.os.Handler
import android.os.HandlerThread

// 1. Create and start the background thread
val handlerThread = HandlerThread("MyBackgroundThread").apply { start() }

// 2. Safely assign the handler variable. 
// handlerThread.looper blocks the main thread for a microsecond if the 
// background thread isn't ready yet, preventing race conditions.
val backgroundHandler = Handler(handlerThread.looper)

backgroundHandler.post {
    myTextView.text = "File written on background thread: ${Thread.currentThread().name}"
}
```
### E. Context Disconnection (Memory Management)
To prevent threads or queued handlers from keeping "dead" activity instances alive in memory during screen orientation changes or exits, you must clean up explicit callbacks:
```kotlin
override fun onDestroy() {
    super.onDestroy()
    // Clears all pending posts and messages out of the queue
    uiHandler.removeCallbacksAndMessages(null)
}
```

### F. ❌ DANGEROUS CODE - DO NOT USE THESE
```kotlin
var myCustomLooper: Looper? = null

val thread = Thread {
    Looper.prepare()
    myCustomLooper = Looper.myLooper()
    Looper.loop()
}
thread.start()

// Inside the Main Thread: We want to create a Handler bound to that thread
// ❌ CRASH/BUG RISK: If the CPU takes a split second to spin up the thread, 
// 'myCustomLooper' will still be NULL right here!
val handler = Handler(myCustomLooper!!) 
```

```kotlin
var backgroundHandler: Handler? = null

val thread = Thread {
    Looper.prepare()
    
    // Assigning the variable inside the background thread
    backgroundHandler = Handler(Looper.myLooper()!!) 
    
    Looper.loop()
}
thread.start()

// Inside the Main Thread (Immediately after starting the thread):
// 🛑 CRASH RISK: The main thread keeps executing instantly. If the CPU takes 
// even a fraction of a millisecond to spawn the background thread, 
// 'backgroundHandler' will still be NULL right here, causing an app crash!
backgroundHandler?.post { 
    // Code may never run or will crash
}
```

```kotlin
// ❌
try {
    Thread.sleep(5000)
} catch (e: InterruptedException) {
    // Left empty or just a println(). 
    // The interruption flag is now WIPED OUT, and the loop keeps running!
}
```
---

## 🏆 Module Progress Map
- [ ] **Challenge 1: Pure-Code Progress Counter Engine (Easy)**
- [ ] **Challenge 2: Custom Thread Pool Progress Matrix (Advanced)**
- [ ] **Challenge 3: Multi-Directional State Orchestrator & Event Bus (Master)**
