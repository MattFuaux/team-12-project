package com.team12.fruitwatch.database.entities

import android.graphics.Bitmap
import java.time.LocalDateTime

class PastSearch (
    var id: Long? = null,
    var itemName: String? = null,
    var itemSearchDate: LocalDateTime? = null,
    var itemImage: Bitmap? = null
    ){
    override fun toString(): String {
        return "Id: $id\nItem Name: $itemName"
    }
}