package com.team12.fruitwatch.database.entities

import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
class PastSearch (
    var id: Long? = null,
    var itemName: String? = null,
    var itemSearchDate: LocalDateTime? = null,
    var itemImage: ByteArray? = null
    ) : Parcelable {
    override fun toString(): String {
        return "Id: $id\nItem Name: $itemName"
    }
}