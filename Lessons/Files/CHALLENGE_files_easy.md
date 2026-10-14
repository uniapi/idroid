# Challenge 1: Programmatic Form Persister Engine (Easy)

## 🎯 Goal
Build an administrative terminal Activity entirely in code (no XML layouts, no AndroidX) that captures raw text user input from an `android.widget.EditText` component,
encodes it, and serializes it straight to disk using a native `FileOutputStream`. The app must load the saved string configuration back into memory on startup without freezing the user interface canvas.

---

## 🏗️ Technical Specification & Architecture

### 1. View & Structural Hierarchy
Create your UI entirely inside the Activity code within `onCreate`:
* Use a manual vertical root layout container (`android.widget.LinearLayout` prefixed as `llRoot`).
* Instantiate an input form field (`android.widget.EditText` prefixed as `etInputPayload`).
* Instantiate a control row containing two interactive elements:
  * `Button` labeled "COMMIT TO STORAGE" prefixed as `btnCommitDisk`.
  * `Button` labeled "FLUSH DISK STORAGE" prefixed as `btnFlushDisk`.
* Instantiate an `android.widget.TextView` prefixed as `tvFileLogs` to show tracking metrics.

### 2. Concrete File IO Storage Rules
* **Target Coordinates:** Save your file explicitly to internal storage using `File(context.filesDir, "sandboxed_payload.txt")`.
* **Threading Rule:** You must offload your input/output stream execution tasks onto an isolated background thread.
Use `Handler(Looper.getMainLooper())` to update UI components when operations complete.

---

## 🔍 Step-by-Step Architecture Guide

To construct this storage layer without getting stuck, map your pipeline into clear steps:

### Step 1: Initialize Prefix-Compliant Code Views
```kotlin
package localhost.idroid.salamun

val context = this
val llRoot = LinearLayout(context).apply {
    orientation = LinearLayout.VERTICAL
    setPadding(35, 45, 35, 45)
}

val etInputPayload = EditText(context).apply {
    hint = "Enter raw terminal log command stream..."
}

val btnCommitDisk = Button(context).apply { text = "COMMIT TO STORAGE" }
val btnFlushDisk = Button(context).apply { text = "FLUSH DISK STORAGE" }
val tvFileLogs = TextView(context).apply { text = "Status: Operational Awaiting Input." }
```

### Step 2: Offload Output File Serialization Streams
Inside your `btnCommitDisk.setOnClickListener`, safely execute the text serialization pipeline on a background thread:
```kotlin
val textToSave = etInputPayload.text.toString()

Thread {
    try {
        val fileTarget = File(context.filesDir, "sandboxed_payload.txt")
        FileOutputStream(fileTarget).use { fos ->
            fos.write(textToSave.toByteArray(Charsets.UTF_8))
        }
        
        hMain.post {
            tvFileLogs.text = "SUCCESS: Wrote payload data block to internal storage."
            etInputPayload.setText("")
        }
    } catch (e: Exception) {
        hMain.post { tvFileLogs.text = "ERROR: Exception during serialization." }
    }
}.start()
```

### Step 3: Implement Initial Configuration Loading Routine
Inside your Activity's `onResume()` hook, execute a non-blocking input stream read operation to check for existing data and automatically populate `tvFileLogs`.

---

## 🧪 Verification & Acceptance Criteria
1. **No External Abstractions:** Code does not reference shared preferences, databases, or AndroidX storage tools. It runs entirely on native Java `File` streams.
2. **Shorthand Variable Compliance:** View references must use exact prefix identifiers (`hMain`, `llRoot`, `etInputPayload`, `btnCommitDisk`, `btnFlushDisk`, `tvFileLogs`).
3. **No Thread Crashes:** Disk reading/writing routines execute cleanly off the main thread to protect frame rendering cycles.
