# 🏆 Easy Challenge: Dynamic Media Playlist Manager (Custom BaseAdapter)

## 🎯 The Objective
Your goal is to build a high-performance **Media Playlist Manager** entirely in pure Kotlin code, bypassing XML and AndroidX.
The application must display a list of audio tracks, handle user selection states without glitches, and support dynamic calculation utilities.

---

## 🏗️ Step-by-Step Architectural Blueprint

### Step 1: Define the Immutable Data Model
Create a data class named `Track` to encapsulate the entity state:
*   `id: Long` — Unique identifier.
*   `title: String` — The name of the track.
*   `durationSeconds: Int` — Track duration in seconds.
*   `isFavorite: Boolean` — Stateful binary flag tracking user preference.

### Step 2: Construct the ViewHolder Custom View (`TrackItemView`)
Extend `LinearLayout` programmatically to act as your layout engine and view recycler binder:
1.  **Container Properties**: Set orientation to `HORIZONTAL`, layout width to `MATCH_PARENT`, height to `WRAP_CONTENT`, and vertical gravity to `CENTER_VERTICAL`.
2.  **Child Hierarchy**:
    *   `ivPlayPause: ImageView` (Leftmost, fixed dimensions `48dp` x `48dp`). Use system resource `android.R.drawable.ic_media_play`.
    *   `innerVerticalLayout: LinearLayout` (Center, width `0`, weight `1f`, orientation `VERTICAL`).
        *   `tvTitle: TextView` (Top text, text size `18sp`, bold, black text color).
        *   `tvDuration: TextView` (Bottom text, text size `14sp`, gray text color).
    *   `tbFavorite: ToggleButton` (Rightmost, width `WRAP_CONTENT`, height `WRAP_CONTENT`).
3.  **Utility Dimensions**: Implement a private metric conversion helper:
    ```kotlin
    private fun dpToPx(dp: Int): Int = (dp * resources.displayMetrics.density).toInt()
    ```

### Step 3: Engineer the Custom `BaseAdapter` (`PlaylistAdapter`)
1.  **Count & Index Binding**: Map `getCount()`, `getItem(position)`, and `getItemId(position)` strictly to your backing collection data array.
2.  **Recycling Logic Loop (`getView`)**:
    *   Assert nullability check on `convertView`. Cast or construct `TrackItemView` cleanly.
    *   **CRITICAL FIX**: Before mutating the view properties with row data, nullify any pre-existing listeners on the reused component to prevent state bleed glitches:
        ```kotlin
        itemView.tbFavorite.setOnCheckedChangeListener(null)
        ```
    *   Map data properties to text/image nodes. Format the raw `durationSeconds` integer into a readable `MM:SS` duration string wrapper dynamically using `String.format("%02d:%02d", minutes, seconds)`.
    *   Bind a fresh `setOnCheckedChangeListener` listener to update the underlying data model.

### Step 4: Execute the UI Container Architecture (`PlaylistActivity`)
1.  Instantiate a vertical root layout container holding a native `ListView`.
2.  Generate a collection array consisting of at least 5 realistic mock media audio tracks.
3.  Instantiate and attach your `PlaylistAdapter` to the `ListView`.
4.  Bind an `setOnItemClickListener` to dispatch a system log or `Log.d` when a user taps a list item.
