### Challenge 3: Procedural Pixel Matrix & Filter Studio

#### 🎯 Objective
Build a pure-code, high-speed pixel manipulation studio that generates procedural test bitmaps from scratch,
applies real-time bitwise color transformations and 3×3 spatial convolution kernels using bulk `IntArray` buffers (strictly forbidding slow per-pixel `getPixel()`/`setPixel()` loops),
benchmarks CPU execution speed in milliseconds, and exports processed outputs to compare compression formats.

---

#### 📋 Functional Requirements

1. **Procedural Pattern Generator (`Bitmap.createBitmap`):**
   * Generate a **512 × 512** mutable `Bitmap` (`Bitmap.Config.ARGB_8888`) purely via math and bitwise operations in a flat `IntArray(512 * 512)`:
     * **Mode A — RGB XOR Fractal:** `R = (x xor y) and 0xFF`, `G = ((x * 2) xor y) and 0xFF`, `B = (x xor (y * 2)) and 0xFF`
     * **Mode B — Radial Color Wheel & Checkerboard:** Combines trigonometric distance/angle gradients with crisp high-contrast squares to test edge detection and blur kernels clearly.

2. **Bulk `IntArray` Pixel Filter Engine (`getPixels` / `setPixels`):**
   * Strictly **forbid** calling `bitmap.getPixel(x, y)` or `bitmap.setPixel(x, y, c)` inside loops.
   * Implement the following filters using bit shifts (`shr`, `shl`) and bit masks (`and 0xFF`, `or`):
     * **Original (Reset):** Restores the base procedural bitmap.
     * **Red / Green / Blue Channel Isolation:** Masks out the other two color channels while preserving `Alpha`.
     * **Luminance Grayscale:** Uses perceptual weights `luma = (0.299 * r + 0.587 * g + 0.114 * b).toInt()`.
     * **Color Inversion (Negative):** Inverts `R`, `G`, `B` via `255 - channel` (or `channel xor 0xFF`) while keeping `Alpha` intact.
     * **Interactive Brightness & Contrast:** Driven by two `SeekBar` sliders (`Brightness: -100..+100`, `Contrast: 0.5x..2.0x`) with strict `.coerceIn(0, 255)` clamping.
     * **3×3 Spatial Convolution Kernels:** Read from an immutable `sourcePixels` snapshot array and write into a separate `outputPixels` array (so neighbour reads aren't corrupted mid-pass) for:
       * **3×3 Box Blur** (averages all 9 neighbors)
       * **3×3 Sobel/Laplacian Edge Detection** (highlights sharp pixel transitions)

3. **Execution Time Profiler (`System.nanoTime()`):**
   * Measure the exact duration (in milliseconds, formatted to 2 decimal places) of every filter pass
	and display it in a live telemetry bar so students can observe the speed difference between point operations (grayscale/invert) and 9-neighbour convolution kernels (box blur/edge detection).

4. **Compression & Format Exporter (`bitmap.compress`):**
   * Provide an **[ Export & Compare Formats ]** button that saves the currently displayed `Bitmap` into `cacheDir` using three formats:
     * `PNG` (Lossless, quality = `100`)
     * `JPEG` (Lossy, quality = `85`)
     * `JPEG` (Low quality = `25`)
   * Read back the resulting file sizes (`file.length()`) and display an exact byte/KB comparison in the telemetry output.

---

#### 🏗️ Pure-Code UI Blueprint
Build the entire interface programmatically in `MainActivity.kt` using `LinearLayout`, `ScrollView`, `ImageView`, `TextView`, `SeekBar`, `HorizontalScrollView`, and `Button` (zero XML layouts, zero AndroidX):

```text
+--------------------------------------------------+

| Pattern: [ XOR Fractal ] [ Radial Grid ]         |
+--------------------------------------------------+

|                                                  |
|           ImageView (512 x 512 Canvas)           |
|                                                  |
+--------------------------------------------------+

| Filters: [Reset] [Gray] [Invert] [Red] [Blur] ...|
| Brightness: [---------o---------] (+0)           |
| Contrast  : [---------o---------] (1.00x)        |
+--------------------------------------------------+

| [ Export & Compare PNG vs JPEG Sizes ]           |
+--------------------------------------------------+

| BENCHMARK & EXPORT TELEMETRY:                    |
| • Active Filter : 3x3 Edge Detection             |
| • Pixel Count   : 262,144 pixels (1.00 MB RAM)   |
| • Execution Time: 4.82 ms (54.3 MPixels/sec)     |
| • Export Sizes  : PNG: 184.2 KB | JPG(85): 42.1KB|
+--------------------------------------------------+
```

---

#### ✅ Verification Checklist
* [ ] Zero calls to `getPixel()` or `setPixel()` anywhere in the project; all operations run via bulk `getPixels()` / `setPixels()`.
* [ ] 3×3 Convolution filters (`Box Blur` and `Edge Detection`) read from a separate source buffer so adjacent pixels do not corrupt neighboring calculations during the loop.
* [ ] Adjusting Brightness/Contrast sliders updates the preview smoothly without leaking intermediate `Bitmap` objects.
* [ ] Exporting writes valid, inspectable `.png` and `.jpg` files to `cacheDir` and accurately reports their compressed disk sizes.
* [ ] Zero `androidx.*` imports anywhere in the project.
