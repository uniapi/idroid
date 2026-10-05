/* \uFDFD
 *                     \u0648\u0671\u0630\u0643\u0631 \u0631\u0628\u0651\u0643
 *        \u0643\u062A\u0628\u0647 \u0639\u0628\u062F \u0671\u0644\u0643\u0631\u064A\u0645
 *  \u06F1\u06F4\u06F4\u06F8 \u0631\u0628\u064A\u0639 \u0671\u0644\u0622\u062E\u0631 \u06F2\u06F4
 */
package localhost.idroid.salamun

import android.os.Parcel
import android.os.Parcelable

data class TaskPayload(
	var taskTitle: String,
	var estimatedHours: Int,
	var allocationDepartment: String,
	var isUrgentPriority: Boolean
) : Parcelable {
	constructor(parcel: Parcel) : this(
		parcel.readString() ?: "",
		parcel.readInt(),
		parcel.readString() ?: "",
		parcel.readByte() != 0.toByte()
	)
	override fun writeToParcel(parcel: Parcel, flags: Int) {
		parcel.writeString(taskTitle)
		parcel.writeInt(estimatedHours)
		parcel.writeString(allocationDepartment)
		parcel.writeByte(if (isUrgentPriority) 1 else 0)
	}
	override fun describeContents(): Int = 0

	companion object CREATOR : Parcelable.Creator<TaskPayload> {
		override fun createFromParcel(parcel: Parcel): TaskPayload = TaskPayload(parcel)
		override fun newArray(size: Int): Array<TaskPayload?> = arrayOfNulls(size)
	}
}
