package com.team12.fruitwatch.data.model

import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Data class that captures user information for logged in users retrieved from LoginRepository
 */
@Parcelize
data class LoggedInUser(
    val userId: String,
    val displayName: String,
    val jwt : String
) : Parcelable