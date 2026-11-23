/* \uFDFD
 *                           \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *              \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u062C\u0645\u0627\u062F\u0649 \u0627\u0644\u0622\u062E\u0631\u0629 \u06F1\u06F2
 */
package localhost.idroid.salamun

import java.io.File

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.AbsListView
import android.widget.ArrayAdapter
import android.widget.FrameLayout
import android.widget.ImageView
import android.graphics.Color

class ImageGridAdapter(
	context: Context,
	files: Array<File>,
	private val cellSizePx: Int,
	private val imageLoader: AsyncImageLoader
) : ArrayAdapter<File>(context, 0, files) {
	private class ViewHolder(val imageView: ImageView)
	override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
		val holder: ViewHolder
		val container: FrameLayout
		if (convertView == null) {
			container = FrameLayout(context).apply {
				layoutParams = AbsListView.LayoutParams(cellSizePx, cellSizePx)
			}
			val imageView = ImageView(context).apply {
				layoutParams = FrameLayout.LayoutParams(
					ViewGroup.LayoutParams.MATCH_PARENT,
					ViewGroup.LayoutParams.MATCH_PARENT
				)
				scaleType = ImageView.ScaleType.CENTER_CROP
				setBackgroundColor(Color.parseColor("#2A2A2A"))
			}
			container.addView(imageView)
			holder = ViewHolder(imageView)
			container.tag = holder
		} else {
			container = convertView as FrameLayout
			holder = container.tag as ViewHolder
		}
		val file = getItem(position)
		if (file != null) {
			imageLoader.loadThumbnail(file, holder.imageView, cellSizePx)
		}
		return container
	}
}
