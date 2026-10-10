/* \uFDFD
 *		   			   \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *	 	  \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u0631\u0628\u064A\u0639 \u0671\u0644\u0622\u062E\u0631 \u06F2\u06F7
 */
package localhost.idroid.salamun

import android.os.Parcel
import android.os.Parcelable

enum class Priority { NORMAL, URGENT }

data class ActiveOrder(
	val orderId: Long,
	val name: String,
	var progress: Int,		// 0%, 25%, 50%, 75%, 100%
	val priority: Priority
) : Parcelable {
	constructor(parcel: Parcel) : this(
		parcel.readLong(),
		parcel.readString() ?: "",
		parcel.readInt(),
		Priority.valueOf(parcel.readString() ?: Priority.NORMAL.name)
	)
	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeLong(orderId)
		parcel.writeString(name)
		parcel.writeInt(progress)
		parcel.writeString(priority.name)
	}
	override fun describeContents(): Int = 0

	companion object CREATOR : Parcelable.Creator<ActiveOrder> {
		override fun createFromParcel(parcel: Parcel): ActiveOrder = ActiveOrder(parcel)
		override fun newArray(size: Int): Array<ActiveOrder?> = arrayOfNulls(size)
	}
}
