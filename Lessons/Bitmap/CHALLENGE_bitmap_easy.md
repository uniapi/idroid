### Challenge 1: Memory-Safe Image Downsampler & Metadata Inspector

#### 🎯 Objective
Build a pure-code Android diagnostic tool that inspects a high-resolution image file **without loading its full pixel buffer into RAM**,
calculates the optimal `inSampleSize` for a target display viewport, corrects EXIF rotation metadata, and demonstrates the real-time RAM impact of `ARGB_8888` versus `RGB_565`.

---

#### 📋 Functional Requirements

1. **Zero-Allocation Metadata Inspection (`inJustDecodeBounds`):**
   * Load a sample high-resolution image (either bundled in `assets/` / `res/raw/` or picked via `Intent.ACTION_OPEN_DOCUMENT` into a local cache file
	so `ExifInterface` and `BitmapFactory.decodeFile` have a direct file path).
   * Perform a `BitmapFactory.Options().apply { inJustDecodeBounds = true }` pass to extract:
     * Raw image width & height (`outWidth` × `outHeight`)
     * MIME type (`outMimeType`)
     * Theoretical **unscaled RAM cost** in `ARGB_8888` (`width * height * 4` bytes)

2. **Dynamic Target Dimensions & Subsampling (`inSampleSize`):**
   * Provide interactive controls (e.g., `SeekBar` or preset `Button`s for `128×128`, `256×256`, `512×512`, and `1024×1024` viewports).
   * Compute the largest power-of-2 `inSampleSize` (`1, 2, 4, 8, 16...`) that keeps decoded dimensions ≥ the target viewport.

3. **Color Format Toggle (`ARGB_8888` vs. `RGB_565`):**
   * Add a `Switch` or `ToggleButton` to switch `options.inPreferredConfig` between `Bitmap.Config.ARGB_8888` (4 bytes/pixel) and `Bitmap.Config.RGB_565` (2 bytes/pixel).
   * Display the resulting decoded dimensions, `inSampleSize` used, actual `bitmap.allocationByteCount`, and exact **percentage of RAM saved** compared to the unscaled `ARGB_8888` footprint.

4. **Automatic EXIF Orientation Correction:**
   * Inspect `android.media.ExifInterface.TAG_ORIENTATION` on the source file.
   * If rotated (`90°`, `180°`, or `270°`), apply a `android.graphics.Matrix` via `Bitmap.createBitmap(...)` and immediately `.recycle()` the unrotated intermediate bitmap.

5. **Safe Lifecycle Cleanup:**
   * Whenever a new decoded bitmap replaces the previous one in the `ImageView`, verify `oldBitmap != null && !oldBitmap.isRecycled` and call `oldBitmap.recycle()` after swapping the `ImageView` source.

---

#### 🏗️ Pure-Code UI Blueprint
Build the entire interface programmatically in `MainActivity.kt` using `LinearLayout`, `ScrollView`, `ImageView`, `TextView`, `Switch`, and `Button` (zero XML layouts, zero AndroidX):

```text
+--------------------------------------------------+

| [ Load Sample / Pick Image File ]                |
+--------------------------------------------------+

|                                                  |
|              ImageView (Preview)                 |
|                                                  |
+--------------------------------------------------+

| Target Box: [ 128px ] [ 256px ] [ 512px ] [1024] |
| Color Config: [x] Use RGB_565 (16-bit, No Alpha) |
+--------------------------------------------------+

| DIAGNOSTICS TELEMETRY:                           |
| • Raw Dimensions: 4032 x 3024 (image/jpeg)       |
| • Unscaled ARGB_8888 RAM: 46.51 MB               |
| • EXIF Orientation: ROTATE_90 (Corrected)        |
| • Computed inSampleSize: 8                       |
| • Decoded Dimensions: 378 x 504                  |
| • Actual Allocation: 372.09 KB (99.2% Saved!)    |
+--------------------------------------------------+
```

---

#### ✅ Verification Checklist
* [ ] App does **not** allocate full image memory during the initial bounds check (`BitmapFactory.decodeFile` returns `null` when `inJustDecodeBounds = true`).
* [ ] Switching from `ARGB_8888` to `RGB_565` at the same `inSampleSize` cuts `bitmap.allocationByteCount` exactly in half.
* [ ] Tapping `128px` vs `1024px` dynamically recalculates `inSampleSize` as a power of 2 and updates `ImageView` without leaking previous `Bitmap` instances.
* [ ] Zero `androidx.*` imports anywhere in the project.
