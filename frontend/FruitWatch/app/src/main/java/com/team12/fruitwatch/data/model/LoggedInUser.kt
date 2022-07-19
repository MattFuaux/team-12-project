package com.team12.fruitwatch.data.model

import android.os.Parcel
import android.os.Parcelable

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
data class LoggedInUser(
    val userId: String,
    val displayName: String,
    val jwt : String
) : Parcelable {
    constructor(parcel: Parcel) : this(parcel.readString()!!, parcel.readString()!!, parcel.readString()!!)

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(userId)
        parcel.writeString(displayName)
        parcel.writeString(jwt)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<LoggedInUser> {
        override fun createFromParcel(parcel: Parcel): LoggedInUser {
            return LoggedInUser(parcel)
        }

        override fun newArray(size: Int): Array<LoggedInUser?> {
            return arrayOfNulls(size)
        }
    }
}