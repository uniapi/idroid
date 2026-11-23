### Challenge 2: Asynchronous Thumbnail Gallery & `LruCache` Engine

#### 🎯 Objective
Build a smooth-scrolling `GridView` image gallery backed by a custom `ArrayAdapter<File>` (pure `android.widget.*`, zero `RecyclerView` or AndroidX) that scans device images,
decodes subsampled thumbnails on a background `ExecutorService`, caches them in an `android.util.LruCache` sized to \(1/8^{\text{th}}\) of available VM heap memory,
and prevents stale-image flickering during rapid scrolling.

---

#### 📋 Functional Requirements

1. **Framework-Native Runtime Storage Permissions:**
   * Request read permissions using native `Activity.requestPermissions()` and `onRequestPermissionsResult()` (no `ActivityResultContracts` or `ContextCompat`):
     * **Android 13+ (API 33+):** `Manifest.permission.READ_MEDIA_IMAGES`
     * **Android 12 and lower (API ≤ 32):** `Manifest.permission.READ_EXTERNAL_STORAGE`
   * Query `MediaStore.Images.Media.EXTERNAL_CONTENT_URI` via `ContentResolver` to collect local image `File` paths
	(and include a fallback generator that creates 40+ synthetic high-resolution test images in `cacheDir` if the emulator/device gallery is empty).

2. **Heap-Budgeted Memory Cache (`android.util.LruCache`):**
   * Dynamically calculate cache capacity from `Runtime.getRuntime().maxMemory() / 1024 / 8` (in kilobytes).
   * Override `sizeOf(key: String, bitmap: Bitmap)` to return `bitmap.allocationByteCount / 1024`.
   * Track and expose real-time **Cache Hits**, **Cache Misses**, and **Current Cache Fill (KB / Max KB)** in a live telemetry header bar.

3. **Asynchronous Decoding Pipeline (`ExecutorService` + `Handler`):**
   * Never decode bitmaps on the main UI thread inside `ArrayAdapter.getView()`.
   * On a cache hit, bind the `Bitmap` synchronously inside `getView()`.
   * On a cache miss, set a dark placeholder background on the `ImageView`, submit a decoding task to a fixed background `ExecutorService` (e.g., 4 worker threads),
	subsample the image to the grid cell's exact pixel size (`inSampleSize`), store the result in `LruCache`, and post the UI update back via `Handler(Looper.getMainLooper())`.

4. **View Recycling & Stale-Image Race Condition Guard (`ViewHolder` + `View.tag`):**
   * Implement the `ViewHolder` pattern inside your custom `ArrayAdapter<File>` to eliminate repeated `findViewById` calls.
   * Assign `holder.imageView.tag = file.absolutePath` before launching the background decode task.
   * When the background thread finishes decoding and returns to the main thread, **verify `holder.imageView.tag == file.absolutePath`** before calling `setImageBitmap()`,
	ensuring fast-scrolled recycled views never flash stale thumbnails.

5. **Clean Resource Shutdown:**
   * Shut down the `ExecutorService` (`shutdownNow()`) and evict the `LruCache` (`evictAll()`) inside `Activity.onDestroy()`.

---

#### 🏗️ Pure-Code UI Blueprint
Build the entire interface programmatically in `MainActivity.kt` using `LinearLayout`, `TextView`, `Button`, and `GridView` (3 columns):

```text
+--------------------------------------------------+

| LruCache: 14.2 MB / 32.0 MB | Hits: 84 | Miss: 21|
| [ Evict Cache ]  [ Reload Gallery ]              |
+--------------------------------------------------+

| +-------------+ +-------------+ +-------------+  |
| |             | |             | |             |  |
| |  Thumb 1    | |  Thumb 2    | |  Thumb 3    |  |
| |             | |             | |             |  |
| +-------------+ +-------------+ +-------------+  |
| +-------------+ +-------------+ +-------------+  |
| |             | |             | |             |  |
| |  Thumb 4    | |  Thumb 5    | |  Thumb 6    |  |
| |             | |             | |             |  |
| +-------------+ +-------------+ +-------------+  |
+--------------------------------------------------+
```

---

#### ✅ Verification Checklist
* [ ] Scrolling rapidly through 100+ high-resolution images never triggers `OutOfMemoryError` or drops UI frames.
* [ ] Recycled grid cells never briefly display an image belonging to a previous scroll position (`View.tag` check works).
* [ ] Scrolling back up to previously viewed thumbnails results in **100% LruCache Hits** with instant rendering and zero disk reads.
* [ ] Tapping **[ Evict Cache ]** drops current cache usage to `0 KB` and forces fresh background decodes on the next scroll.
* [ ] Zero `androidx.*` imports anywhere in the codebase.
