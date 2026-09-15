## 🛠️ Coding Challenge: Pure-Code Profile Dashboard
## 🎯 Objective
Create a responsive user profile dashboard using a combination of **Structural Layouts**, **Interactive Widgets**, and **Relative Alignments** completely inside a native `android.app.Activity` class.
## 📋 Constraints & Rules

   1. **No XML**: Every element must be declared and built directly in Kotlin code.
   2. **No androidx / Jetpack**: Use exclusively native standard platform classes (e.g., `android.app.Activity`, `android.widget.\*`, `android.view.\*`).
   3. **No Hardcoded Resource IDs**: Utilize `View.generateViewId()` dynamically to create reference keys for relative arrangements.

------------------------------
## 📐 Layout Architecture Requirements
Your activity viewport should be split into **three distinct structural sections** using a main vertical container:

   1. **Header Section** **(`RelativeLayout`)**
      * A placeholder square profile image block (`ImageView`) centered horizontally.
      * A status text indicator (`TextView`) reading `"System Mode: Idle"`. It must be aligned **directly to the right** of the profile image block and vertically centered relative to it.
   2. **Structural Information Section** **(`TableLayout`)**
	  * This section should be separated from the header by an invisible `Space` component of **48 pixels**.
      * It must contain exactly **3 tabular rows** **(`TableRow`)**, mapping user details:
      * *Row 1*: `Username` ➔ `idroid_developer`
      * *Row 2*: `API Level` ➔ `34`
      * *Row 3*: `Runtime` ➔ `Native ART`
   3. **Interactive Control Section** **(Footer Component)**
   * A platform binary slider (`Switch`).
      * **(Bonus)** **The Logic Rule**: When the user toggles the `Switch` to **ON**, the status text inside the Header Section must immediately change its text to `"System Mode: ACTIVE"` and its text color to dark green. When toggled **OFF**, it must revert back to `"System Mode: Idle"` in a dark gray color.
   
------------------------------
## 📝 Code Template to Begin
Use this bare-metal platform skeleton template to implement your solution:
```kotlin
package localhost.idroid.salamun
import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
class ProfileDashboardActivity : Activity() { // Standard SDK native Activity class

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // 1. Initialize a root layout container (e.g., LinearLayout)
        // 2. Build the RelativeLayout header structure using dynamic IDs
        // 3. Build the TableLayout information data grid
        // 4. Implement the interactive Switch control logic
        // 5. Hierarchy assembly: rootLayout.addView(...)
        
        // TODO: Complete the challenge implementation here
        
        // setContentView(rootLayout)
    }
}
```
