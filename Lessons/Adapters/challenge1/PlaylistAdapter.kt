/* \uFDFD
 *					 \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *		\u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u0631\u0628\u064A\u0639 \u0671\u0644\u0622\u062E\u0631 \u06F2\u06F6
 */
package localhost.idroid.salamun

import android.util.Log
import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.CompoundButton

class PlaylistAdapter(
	private val context: Context,
	private val tracks: List<Track>
) : BaseAdapter() {
	override fun getCount() = tracks.size
	override fun getItem(position: Int) = tracks[position]
	override fun getItemId(position: Int) = tracks[position].id
	override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
		val itemView = if (convertView != null) convertView as TrackItemView else TrackItemView(context)
		val currentTrack = getItem(position)
		with(itemView) {
			tvTitle.text = currentTrack.title
			tvDuration.text = formatDuration(currentTrack.durationSeconds)
			tbFavorite.setOnCheckedChangeListener(null)
			tbFavorite.isChecked = currentTrack.isFavorite
			tbFavorite.setOnCheckedChangeListener { _: CompoundButton, isChecked: Boolean ->
				currentTrack.isFavorite = isChecked
			}
		}
		return itemView
	}
	private fun formatDuration(totalSeconds: Int): String {
		val minutes = totalSeconds / 60
		val seconds = totalSeconds % 60
		return String.format("%02d:%02d", minutes, seconds)
	}
}
