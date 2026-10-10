/* \uFDFD
 *					 \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *		\u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u0631\u0628\u064A\u0639 \u0671\u0644\u0622\u062E\u0631 \u06F2\u06F6
 */
package localhost.idroid.salamun

import android.util.Log
import android.app.Activity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import android.widget.ListView
import android.widget.AdapterView
import android.graphics.Color

class PlaylistActivity : Activity() {
	private companion object {
		const val TAG = "localhost.idroid.salamun"
	}
	override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		val rootLayout = LinearLayout(this).apply {
			layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
			orientation = LinearLayout.VERTICAL
			setBackgroundColor(Color.WHITE)
		}
		val listView = ListView(this).apply {
			layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT)
		}
		rootLayout.addView(listView)
		setContentView(rootLayout)

		val playlist = listOf(
			Track(1, "Hall", 355),
			Track(2, "Bathroom", 482),
			Track(3, "Bedroom", 390),
			Track(4, "Kitchen", 301),
			Track(5, "Lobby", 356),
			Track(6, "On the street", 183),
			Track(7, "In the car", 294),
			Track(8, "Bus top", 382),
			Track(9, "In the bus", 491)
		)
		val adapter = PlaylistAdapter(this, playlist)
		listView.adapter = adapter
		listView.setOnItemClickListener { parent: AdapterView<*>?, view: View?, position: Int, id: Long ->
			parent?.let {
				val track = it.getItemAtPosition(position) as Track
				Log.d(TAG, "PlaylistActivity: Clicked row position $position, playing track: ${track.title}")
			}
		}
	}
}
