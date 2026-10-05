/* \uFDFD
 *                     \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *        \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u0631\u0628\u064A\u0639 \u0671\u0644\u0622\u062E\u0631 \u06F2\u06F4
 */
package localhost.idroid.salamun

import android.os.Parcel
import android.os.Parcelable

data class FilterState(
	var minPrice: Int,
	var maxPrice: Int,
	var categoryName: String
) : Parcelable {
	// Constructor for reading data from Parcel (the order should be the same as writing)
	constructor(parcel: Parcel) : this(
		parcel.readInt(),
		parcel.readInt(),
		parcel.readString() ?: "All"
	)
	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeInt(minPrice)
		parcel.writeInt(maxPrice)
		parcel.writeString(categoryName)
	}
	override fun describeContents(): Int = 0

	companion object CREATOR : Parcelable.Creator<FilterState> {
		override fun createFromParcel(parcel: Parcel): FilterState = FilterState(parcel)
		override fun newArray(size: Int): Array<FilterState?> = arrayOfNulls(size)
	}
}
