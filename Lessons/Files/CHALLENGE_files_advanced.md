# Challenge 2: Multi-Track Binary Log Stream Combiner (Advanced)

## 🎯 Goal
Build a low-level telemetry consolidation matrix Activity entirely in code (no XML layouts, no AndroidX).
The system will dynamically generate multiple sandboxed binary data chunk fragments off the Main Thread,
read those isolated tracks concurrently via pure byte streams (`FileInputStream`),
and combine them into a single unified binary master log file (`FileOutputStream`) through a manual chunk-buffered array loop.

---

## 🏗️ Technical Specification & Architecture

### 1. View & Structural Dashboard Hierarchy
Create a functional raw telemetry console UI entirely inside the Activity code within `onCreate`:
*   Use a manual vertical root layout container (`android.widget.LinearLayout` prefixed as `llRoot`).
*   Instantiate an interactive control panel container (`android.widget.LinearLayout` prefixed as `llControlPanel`) containing two actions:
    *   `Button` labeled "GENERATE FRAGMENTS" prefixed as `btnGenerateFrags`.
    *   `Button` labeled "COMBINE SEPARATE TRACKS" prefixed as `btnCombineTracks`.
*   Instantiate a visual tracking progress item (`android.widget.ProgressBar` prefixed as `pbMergeProgress`).
*   Instantiate a multi-line scrolling output monitor (`android.widget.ScrollView` prefixed as `svConsoleScroll`)
wrapping around a `android.widget.TextView` prefixed as `tvLogConsole` to print system execution streams.

### 2. Concrete Binary IO Processing Rules
*   **The Fragmentation Payload:** Clicking `btnGenerateFrags` must asynchronously write 3 separate raw binary files directly inside `context.filesDir`:
    *   `part_1.bin` (Containing a static byte-array header sequence).
    *   `part_2.bin` (Containing simulated application state data frames).
    *   `part_3.bin` (Containing a closing hexadecimal footer verification sequence).
*   **Manual Stream Merging Loop:** Clicking `btnCombineTracks` must open a target master stream descriptor (`master_output.bin`).
It must sequentially open each fragment channel, execute a chunked byte data transfer loop, and append the bytes into the master document.
*   **The Array Chunk Constraint:** You must *not* read the entire file into memory at once.
You must use a small, low-level binary transfer buffer allocation array (`ByteArray(1024)`) to stream the file contents in safe, non-blocking steps:
    ```kotlin
    val byteBuffer = ByteArray(1024)
    var bytesRead = fileInputStream.read(byteBuffer)
    while (bytesRead != -1) {
        fileOutputStream.write(byteBuffer, 0, bytesRead)
        bytesRead = fileInputStream.read(byteBuffer)
    }
    ```

### 3. Thread Orchestration & Prefix Typing
*   All file stream executions must be fully offloaded onto isolated background threads to prevent frame rate dropping.
*   All view states, handler bridges, and local components must strictly implement Hungarian notation shorthand constraints under the `localhost.idroid.salamun` package namespace layer.

---

## 🔍 Step-by-Step Architecture Guide

To build this multi-channel binary data engine without getting stuck, map your development into logical phases:

### Step 1: Initialize Prefix-Compliant Telemetry Views
```kotlin
package localhost.idroid.salamun

val context = this
val llRoot = LinearLayout(context).apply {
    orientation = LinearLayout.VERTICAL
    setPadding(40, 50, 40, 50)
}

val btnGenerateFrags = Button(context).apply { text = "GENERATE FRAGMENTS" }
val btnCombineTracks = Button(context).apply { text = "COMBINE SEPARATE TRACKS" }
val pbMergeProgress = ProgressBar(context, null, android.R.attr.progressBarStyleHorizontal)
val tvLogConsole = TextView(context).apply { text = "CONSOLE: Binary pipeline operational.\n" }
```

### Step 2: Implement Fragment Binary Assembly Generation
Inside your `btnGenerateFrags.setOnClickListener`, safely populate your sandbox workspace off the Main Thread:
```kotlin
Thread {
    try {
        val file1 = File(filesDir, "part_1.bin")
        file1.outputStream().use { it.write(byteArrayOf(0x49, 0x44, 0x52, 0x4F, 0x49, 0x44)) } // "IDROID" Magic Bytes
        
        // Populate part_2.bin and part_3.bin with arbitrary mock binary structures here...
        
        hMain.post { tvLogConsole.append("LOG: 3 independent binary payload fragments written to disk.\n") }
    } catch (e: Exception) {
        hMain.post { tvLogConsole.append("ERROR: Fragment generation aborted.\n") }
    }
}.start()
```

### Step 3: Implement Sequenced Byte Channel Appendation Matrix
Inside your `btnCombineTracks.setOnClickListener`, build a sequential array tracking the target files, open the unified master destination handle,
and manually dump the bytes through your memory array slice buffer.

---

## 🧪 Verification & Acceptance Criteria
1.  **Pure Binary Transmission:** Code operates exclusively via primitive byte matrices (`FileOutputStream`/`FileInputStream`). No character wrappers (`BufferedReader`/`FileWriter`) are used.
2.  **Shorthand Variable Compliance:** View references must match Hungarian shorthand notation styles (`hMain`, `llRoot`, `llControlPanel`, `btnGenerateFrags`, `btnCombineTracks`, `pbMergeProgress`, `svConsoleScroll`, `tvLogConsole`).
3.  **Strict Allocation Boundaries:** Files are read using the strict `ByteArray(1024)` slice loop constraint, ensuring memory remains completely flat even when handling massive disk payloads.
4.  **No Interface Lag:** Large structural byte stream operations never collide with the system UI main loop sequence.
