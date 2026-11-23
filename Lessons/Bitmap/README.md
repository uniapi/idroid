# 🖼️  Android Bitmaps

## 1. Architectural Overview: What is a Bitmap?

In Android, an `android.graphics.Bitmap` is an uncompressed, 2D matrix of pixels stored in memory. 

While image files on disk (JPEG, PNG, WebP) use compression algorithms to minimize storage space,
`BitmapFactory` must decompress every single pixel into raw color values in RAM before Android's rendering pipeline can display or manipulate the image.

### Calculating Raw Memory Footprint
The amount of RAM a `Bitmap` occupies is strictly determined by its pixel dimensions and its color encoding configuration (`Bitmap.Config`):

$$\text{Memory (Bytes)} = \text{Width} \times \text{Height} \times \text{Bytes per Pixel}$$

For example, a standard 12-megapixel camera photo ($4000 \times 3000$ pixels) takes roughly **2.5 MB** on disk as a compressed JPEG.
When decoded into RAM using the default 32-bit format (`ARGB_8888`, 4 bytes per pixel):

$$4000 \times 3000 \times 4\text{ bytes} = 48,000,000\text{ bytes} \approx 45.78\text{ MB}$$

Loading just a handful of full-resolution images simultaneously will exhaust your application's heap limit and terminate the process with a `java.lang.OutOfMemoryError`.

---

## 2. Pixel Formats (`Bitmap.Config`)

`Bitmap.Config` defines how color channels are packed into each pixel and how many bytes of memory each pixel consumes.

| `Bitmap.Config` | Bytes / Pixel | Channel Layout | Characteristics & Use Cases |
| :--- | :--- | :--- | :--- |
| **`ARGB_8888`** | **4 bytes** (32-bit) | Alpha (8), Red (8), Green (8), Blue (8) | **Default format.** Supports 16.7 million colors and 256 levels of transparency. Best for UI assets and high-fidelity photos. |
| **`RGB_565`** | **2 bytes** (16-bit) | Red (5), Green (6), Blue (5) — *No Alpha* | Cuts memory usage by **50%** compared to `ARGB_8888`. Ideal for opaque photos and backgrounds, though subtle gradients may show slight color banding. |
| **`ARGB_4444`** | **2 bytes** (16-bit) | Alpha (4), Red (4), Green (4), Blue (4) | *Deprecated since API 13.* Produces poor visual quality; Android automatically upgrades requests for this format to `ARGB_8888`. |
| **`ALPHA_8`** | **1 byte** (8-bit) | Alpha (8) only — *No RGB channels* | Stores only transparency values. Useful for alpha masks, drop shadows, and font/stencil rendering. |
| **`RGBA_F16`** | **8 bytes** (64-bit) | 16-bit floating-point per channel (API 26+) | High dynamic range (HDR) and wide-color-gamut photography. |
| **`HARDWARE`** | **Native GPU** | Stored exclusively in GPU texture memory (API 26+) | Zero-copy rendering to screen. **Strictly immutable**; CPU pixel operations (`getPixels()`, `setPixel()`, software `Canvas`) are unsupported. |

### Measuring Actual Memory Allocation
Android provides three properties to inspect a `Bitmap`'s memory usage:
* `bitmap.rowBytes`: The number of bytes between consecutive rows of pixels.
* `bitmap.byteCount`: The minimum number of bytes required to store the bitmap's pixels (`rowBytes * height`).
* `bitmap.allocationByteCount`: The actual size of the underlying allocated memory buffer (API 19+). **Always prefer `allocationByteCount`** when tracking memory in caches,
as a reused bitmap buffer can be larger than the image currently stored inside it.

---

## 3. Decoding & Subsampling (`BitmapFactory`)

`android.graphics.BitmapFactory` is the primary factory class for decoding compressed image data into `Bitmap` objects via:
* `BitmapFactory.decodeFile(pathName, options)`
* `BitmapFactory.decodeResource(resources, resId, options)`
* `BitmapFactory.decodeStream(inputStream, outPadding, options)`
* `BitmapFactory.decodeByteArray(data, offset, length, options)`

### The Two-Pass Subsampling Pattern
Never decode an unknown image directly into full memory if it will be displayed inside a smaller container (such as a $150 \times 150$ thumbnail). Instead, use `BitmapFactory.Options` in two passes:

1. **Pass 1 — Metadata Inspection (`inJustDecodeBounds = true`):**
   `BitmapFactory` parses only the image file header, populates `options.outWidth`, `options.outHeight`, and `options.outMimeType`, and returns `null` without allocating any pixel memory.
2. **Calculate `inSampleSize`:**
   `inSampleSize` tells the decoder to subsample the image by reading every $N^{\text{th}}$ pixel horizontally and vertically:
   * `inSampleSize = 1`: Full resolution ($1/1$ pixels).
   * `inSampleSize = 2`: Half width and half height ($1/4$ total pixels = **75% RAM savings**).
   * `inSampleSize = 4`: Quarter width and quarter height ($1/16$ total pixels = **93.75% RAM savings**).
   * *Note:* The native decoder rounds `inSampleSize` down to the nearest **power of 2** ($1, 2, 4, 8, 16, \dots$).
3. **Pass 2 — Actual Pixel Decoding (`inJustDecodeBounds = false`):**
   Set `inSampleSize` and decode the downsampled `Bitmap`.

```kotlin
import android.graphics.Bitmap
import android.graphics.BitmapFactory

fun calculateInSampleSize(
    options: BitmapFactory.Options,
    reqWidth: Int,
    reqHeight: Int
): Int {
    val height = options.outHeight
    val width = options.outWidth
    var inSampleSize = 1

    if (height > reqHeight || width > reqWidth) {
        val halfHeight = height / 2
        val halfWidth = width / 2

        // Calculate the largest inSampleSize value that is a power of 2 and keeps both
        // height and width larger than or equal to the requested height and width.
        while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
            inSampleSize *= 2
        }
    }
    return inSampleSize
}

fun decodeSampledBitmapFromFile(
    filePath: String,
    reqWidth: Int,
    reqHeight: Int,
    preferRgb565: Boolean = false
): Bitmap? {
    // Pass 1: Read dimensions only
    val options = BitmapFactory.Options().apply {
        inJustDecodeBounds = true
    }
    BitmapFactory.decodeFile(filePath, options)

    // Pass 2: Calculate subsampling factor and decode pixels
    options.apply {
        inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
        inJustDecodeBounds = false
        if (preferRgb565) {
            inPreferredConfig = Bitmap.Config.RGB_565
        }
    }

    return BitmapFactory.decodeFile(filePath, options)
}
```

### Resource Density Scaling (`decodeResource`)
When decoding from `res/drawable-*` directories, `BitmapFactory` automatically scales the image based on the ratio between `options.inDensity` (the resource bucket's DPI, e.g., `xhdpi` = 320)
and `options.inTargetDensity` (the device screen's DPI). 
* If you need the exact raw pixel dimensions of a drawable asset without automatic DPI scaling, set `options.inScaled = false`.

---

## 4. Mutability, Pixel Arrays & Bitwise ARGB Math

### Mutable vs. Immutable Bitmaps
Bitmaps returned by `BitmapFactory` are **immutable** (`bitmap.isMutable == false`) so the rendering engine can safely share and optimize them.
Attempting to modify an immutable bitmap throws an `IllegalStateException`.

To work with writable pixels, you must either create a blank bitmap or copy an existing one:

```kotlin
// 1. Create a blank mutable bitmap in memory
val blankBitmap = Bitmap.createBitmap(512, 512, Bitmap.Config.ARGB_8888)

// 2. Create a mutable copy of an existing immutable bitmap
val mutableCopy = sourceBitmap.copy(Bitmap.Config.ARGB_8888, true)
```

### Bulk Pixel Access: `getPixels()` vs. `getPixel()`
* **`getPixel(x, y)` / `setPixel(x, y, color)`:** Crosses the JNI (Java Native Interface) boundary between Kotlin/ART and native C++ memory on **every single call**.
Processing a $1000 \times 1000$ image requires $1,000,000$ JNI transitions, stalling the thread for hundreds of milliseconds.
* **`getPixels(...)` / `setPixels(...)`:** Copies the entire 2D pixel grid into a flat 1D Kotlin `IntArray` in **one single JNI call**,
lets you run arithmetic at full CPU register speed, and writes the modified array back in one call.

### Packing & Unpacking 32-Bit ARGB Colors
In `ARGB_8888`, every pixel in the `IntArray` is a 32-bit signed `Int` formatted as `0xAARRGGBB`, where each channel occupies 8 bits (`0` to `255`, or `0x00` to `0xFF`):

```text
Bit index:  31      24 23      16 15       8 7        0
           +----------+----------+----------+----------+

           |  Alpha   |   Red    |  Green   |   Blue   |
           +----------+----------+----------+----------+
```

You extract and recombine channels using bitwise shift (`shr`, `shl`) and mask (`and`, `or`) operators:

```kotlin
// Unpack channels from a 32-bit ARGB Int
val alpha = (pixel shr 24) and 0xFF
val red   = (pixel shr 16) and 0xFF
val green = (pixel shr 8) and 0xFF
val blue  = pixel and 0xFF

// Pack channels back into a 32-bit ARGB Int
val packedPixel = (alpha shl 24) or (red shl 16) or (green shl 8) or blue
```

#### Example: High-Speed Luminance Grayscale Filter
```kotlin
fun toGrayscale(source: Bitmap): Bitmap {
    val width = source.width
    val height = source.height
    val result = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)

    val pixels = IntArray(width * height)
    source.getPixels(pixels, 0, width, 0, 0, width, height)

    for (i in pixels.indices) {
        val color = pixels[i]
        val a = (color shr 24) and 0xFF
        val r = (color shr 16) and 0xFF
        val g = (color shr 8) and 0xFF
        val b = color and 0xFF

        // ITU-R BT.601 perceptual luminance weights
        val luma = (0.299f * r + 0.587f * g + 0.114f * b).toInt().coerceIn(0, 255)
        pixels[i] = (a shl 24) or (luma shl 16) or (luma shl 8) or luma
    }

    result.setPixels(pixels, 0, width, 0, 0, width, height)
    return result
}
```

---

## 5. Transformations, EXIF Orientation & Compression

### Cropping, Scaling & Rotating (`Bitmap.createBitmap` + `Matrix`)
You can generate transformed copies of a `Bitmap` directly using `android.graphics.Matrix`:

```kotlin
import android.graphics.Bitmap
import android.graphics.Matrix

fun rotateAndScale(source: Bitmap, degrees: Float, scaleFactor: Float): Bitmap {
    val matrix = Matrix().apply {
        postRotate(degrees)
        postScale(scaleFactor, scaleFactor)
    }
    return Bitmap.createBitmap(
        source,
        0,
        0,
        source.width,
        source.height,
        matrix,
        true // Enables bilinear filtering for smooth scaling
    )
}
```
* **Important:** `Bitmap.createBitmap(...)` may return the exact same `source` instance if no transformation was actually applied (`transformed === source`).
Always check `if (transformed !== source) source.recycle()` before recycling the original.

### Handling Camera EXIF Orientation
Hardware camera sensors typically capture images in a fixed landscape orientation and write a rotation flag into the file's EXIF metadata header rather than rotating the raw pixels.
`BitmapFactory` ignores EXIF tags, causing camera photos to appear sideways or upside down.

Use `android.media.ExifInterface` to inspect `TAG_ORIENTATION` and apply the corrective rotation:

```kotlin
import android.graphics.Bitmap
import android.graphics.Matrix
import android.media.ExifInterface

fun applyExifOrientation(filePath: String, bitmap: Bitmap): Bitmap {
    val exif = ExifInterface(filePath)
    val orientation = exif.getAttributeInt(
        ExifInterface.TAG_ORIENTATION,
        ExifInterface.ORIENTATION_NORMAL
    )

    val degrees = when (orientation) {
        ExifInterface.ORIENTATION_ROTATE_90 -> 90f
        ExifInterface.ORIENTATION_ROTATE_180 -> 180f
        ExifInterface.ORIENTATION_ROTATE_270 -> 270f
        else -> return bitmap
    }

    val matrix = Matrix().apply { postRotate(degrees) }
    val rotated = Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    if (rotated !== bitmap) {
        bitmap.recycle()
    }
    return rotated
}
```

### Compressing & Saving to Disk (`compress`)
To write an in-memory `Bitmap` back to a file or network stream, use `bitmap.compress(format, quality, outputStream)`:
* **`Bitmap.CompressFormat.JPEG`:** Lossy compression; strips alpha transparency (transparent pixels become black).
`quality` ranges from `0` (smallest file) to `100` (highest quality); `85` is a standard balance.
* **`Bitmap.CompressFormat.PNG`:** Lossless compression; preserves the full alpha channel. Ignores the `quality` parameter.
* **`Bitmap.CompressFormat.WEBP_LOSSY` / `WEBP_LOSSLESS`** (API 30+; `WEBP` on older APIs): Offers smaller file sizes than JPEG/PNG at equivalent visual quality while supporting transparency.

```kotlin
import java.io.File
import java.io.FileOutputStream

fun saveBitmapAsPng(bitmap: Bitmap, destination: File) {
    FileOutputStream(destination).use { out ->
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
    }
}
```

---

## 6. Memory Management, Caching & Bitmap Pooling

### Lifecycle & Explicit Disposal (`recycle()`)
Although modern Android versions manage bitmap pixel memory on the ART heap (allowing standard Garbage Collection to reclaim unreferenced bitmaps),
large bitmaps can still cause heap fragmentation and GC pauses.
* Calling `bitmap.recycle()` immediately frees the underlying native pixel buffer and marks the bitmap as dead (`bitmap.isRecycled == true`).
* **Warning:** Never call `recycle()` while an `ImageView` is still displaying that `Bitmap`; doing so causes a `RuntimeException: Canvas: trying to use a recycled bitmap` on the next UI draw pass.

### Memory Caching with `android.util.LruCache`
Decoding bitmaps from disk repeatedly during list scrolling wastes CPU cycles and battery.
`android.util.LruCache` (Least Recently Used cache) keeps a bounded pool of decoded bitmaps in RAM and automatically evicts the oldest entries when the memory budget is reached.

Always compute the cache budget dynamically as a fraction (typically $1/8^{\text{th}}$) of `Runtime.getRuntime().maxMemory()`:

```kotlin
import android.graphics.Bitmap
import android.util.LruCache

class BitmapMemoryCache {
    // Max available VM memory in kilobytes
    private val maxMemoryKb = (Runtime.getRuntime().maxMemory() / 1024).toInt()
    private val cacheSizeKb = maxMemoryKb / 8

    private val cache = object : LruCache<String, Bitmap>(cacheSizeKb) {
        override fun sizeOf(key: String, bitmap: Bitmap): Int {
            // Measure size in kilobytes rather than item count
            return bitmap.allocationByteCount / 1024
        }
    }

    fun get(key: String): Bitmap? = cache.get(key)

    fun put(key: String, bitmap: Bitmap) {
        if (get(key) == null) {
            cache.put(key, bitmap)
        }
    }

    fun clear() = cache.evictAll()
}
```

### Bitmap Reuse (`options.inBitmap`)
Allocating and garbage-collecting thousands of bitmaps in rapid succession triggers stop-the-world GC pauses that drop UI frames.
You can instruct `BitmapFactory` to decode new image data **directly into the memory buffer of an existing, unused mutable `Bitmap`** via `options.inBitmap`:

```kotlin
val options = BitmapFactory.Options().apply {
    inMutable = true
    inBitmap = reusableBitmap // Must be mutable and allocationByteCount >= decoded byteCount
}
val reused = BitmapFactory.decodeFile(nextFilePath, options)
```
When `inBitmap` succeeds, zero new heap memory is allocated.
(On API 19+, the target `inBitmap` simply needs to be mutable and have an `allocationByteCount` greater than or equal to the `byteCount` of the incoming decoded image.)

---

## 7. Decoding Ultra-Large Images (`BitmapRegionDecoder`)

Sometimes an image is far larger than the screen—such as a high-resolution map, architectural blueprint,
or panoramic scan ($12,000 \times 12,000$ pixels)—and the user needs to inspect a small portion at **full, crisp 1:1 resolution**.

Using `inSampleSize` in that scenario blurs fine details,
while decoding the entire image at `inSampleSize = 1` triggers an `OutOfMemoryError` ($12,000 \times 12,000 \times 4\text{ bytes} \approx 549\text{ MB}$).

### Region-Based Decoding
`android.graphics.BitmapRegionDecoder` solves this by decoding **only a specific rectangular region (`android.graphics.Rect`)** directly from a file path, `FileDescriptor`,
or `InputStream` without allocating memory for the rest of the image.

```kotlin
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BitmapRegionDecoder
import android.graphics.Rect
import android.os.Build

fun decodeImageTile(filePath: String, tileRect: Rect, sampleSize: Int = 1): Bitmap? {
    // 1. Initialize the region decoder (API 31+ uses newInstance, older APIs use newInstance(path, isShareable))
    val decoder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        BitmapRegionDecoder.newInstance(filePath)
    } else {
        @Suppress("DEPRECATION")
        BitmapRegionDecoder.newInstance(filePath, false)
    }

    val options = BitmapFactory.Options().apply {
        inSampleSize = sampleSize
        inPreferredConfig = Bitmap.Config.RGB_565 // Optional: save 50% more RAM for opaque maps
    }

    // 2. Decode only the requested Rect region into RAM
    val tileBitmap = decoder?.decodeRegion(tileRect, options)

    // 3. Always release native decoder resources when finished
    decoder?.recycle()

    return tileBitmap
}
```

### Key Rules for `BitmapRegionDecoder`
* **Supported Formats:** Works only with **JPEG** and **PNG** streams/files (and WebP on modern APIs).
* **Combine with `inSampleSize`:** You can pass `inSampleSize` inside `BitmapFactory.Options`
when calling `decodeRegion()` to implement smooth **zoom levels** (e.g., `inSampleSize = 4` when zoomed out, `inSampleSize = 1` when zoomed in).
* **Explicit Cleanup:** `BitmapRegionDecoder` holds native file handles and C++ memory buffers—always call `decoder.recycle()` once you are done extracting tiles.


## 🏆 Module Progress Map
- **Challenge 1: Memory-Safe Image Downsampler & Metadata Inspector**
- **Challenge 2: Asynchronous Thumbnail Gallery & `LruCache` Engine**
- **Challenge 3: Procedural Pixel Matrix & Filter Studio**

---
