# 📂 Files, Streams & Sandboxed I/O Storage Primitives

Welcome to the low-level Android Storage module.
In this lesson, we strip away all database engines, Jetpack Preferences DataStore, and AndroidX components to master pure Java/Kotlin Input/Output streams within the isolated Android sandbox application layer.

---

## 🔬 Core Theory: The Under-the-Hood Sandbox Architecture

### 1. The Linux Sandbox Application Context
Android allocates a unique Linux User ID (UID) to every installed application. This isolates your app inside its own secure runtime container. 
* **Internal Storage Bounds:** Your app gets exclusive access to a dedicated directory inside the file system (`/data/data/your.package.name/`).
No other app on the system can read or write to this directory.
* **Context Accessors:** You query these system folders programmatically using raw context methods:
  * `context.filesDir`: Direct access to the internal storage root directory.
  * `context.cacheDir`: Points to an internal transient file area that the system can auto-clear if disk memory pressure drops dangerously low.

### 2. Stream Primitives: Byte Channels vs. Character Readers
Low-level operating system I/O communicates purely via sequential streams. We classify them into two structural primitives:
* **Byte Streams (`InputStream` / `OutputStream`):** Handle raw binary components (8-bit bytes). Used for loading binary blobs, encryption matrices, or image streams.
* **Character Streams (`Reader` / `Writer`):** Handle text characters (16-bit unicode transformations).
Optimized with wrappers like `BufferedReader` and `BufferedWriter` to combine individual characters into chunks, avoiding heavy, repetitive kernel disk hits.

### 3. Combining Vectors: Asynchronous I/O Operations
* **The StrictMode Trap:** Android blocks disk writing operations on the main thread loop. 
* **The Integration Rule:** Every challenge in this file persistence module requires you to leverage your knowledge from the **Concurrency & Handlers** module.
You must offload stream pipelines onto a background worker thread, then jump back to the UI thread via `Handler(Looper.getMainLooper())` to reflect updates.

---

## 🛠️ The Pure-Code Tooling Set

Since we operate **strictly without XML layouts, Jetpack, or AndroidX components**, you will build your storage layout configurations using the following native primitives:

### A. Raw Output Stream Injection
Writing encrypted or plain text files into internal application boundaries:
```kotlin
val fileTarget = File(context.filesDir, "telemetry_dump.dat")
FileOutputStream(fileTarget).use { outputStream ->
    outputStream.write("PAYLOAD_DATA".toByteArray(Charsets.UTF_8))
}
```

### B. Buffered Buffer Traversal Channels
Streaming structural text information from a secure storage file:
```kotlin
val fileTarget = File(context.filesDir, "telemetry_dump.dat")
val strBuilder = java.lang.StringBuilder()
FileInputStream(fileTarget).bufferedReader().use { bufferedReader ->
    var currentLine = bufferedReader.readLine()
    while (currentLine != null) {
        strBuilder.append(currentLine).append("\n")
        currentLine = bufferedReader.readLine()
    }
}
```

---

## 🏆 Module Progress Map
- **Challenge 1: Programmatic Form Persister Engine (Easy)**
- **Challenge 2: Multi-Track Binary Log Stream Combiner (Advanced)**
- **Challenge 3: High-Performance Multithreaded Image/File Matrix Cache (Master)**
