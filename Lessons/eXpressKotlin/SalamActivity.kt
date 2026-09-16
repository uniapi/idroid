/* \uFDFD
 *                   \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *      \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0633\u0651\u0644\u0627\u0645
 *                         \u06F1\u06F4\u06F4\u06F7 \u0631\u062C\u0628 \u06F1\u06F8
 */
package localhost.idroid.salamun

import android.util.Log
import android.app.Activity

val TAG = "localhost.idroid.salamun"

class SalamActivity : Activity() {
	init {
		Log.d(TAG, "\uFDFD")

		val systems = arrayOf("Linux", "Android", "Solaris", "Windows", "macOS")
		systems.forEachIndexed { i, sys ->
			Log.d(TAG, "$i -> $sys")
		}

		val keypad = arrayOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0")
		var row = ""
		for (n in 1..keypad.size) {
			row += keypad[n - 1] + " "
			when {
				n % 3 == 0 -> { Log.d(TAG, "$row"); row = "" }
				n == keypad.size -> Log.d(TAG, "  0")
			}
		}
	}
}
