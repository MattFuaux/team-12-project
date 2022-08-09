package com.team12.fruitwatch.ui.main.fragments.search

import android.graphics.Bitmap
import android.os.Parcel
import android.os.Parcelable
import androidx.lifecycle.ViewModel
import kotlinx.parcelize.Parcelize
//import kotlinx.android.parcel.Parcelize
//import kotlinx.parcelize.Parceler

import java.time.LocalDateTime


@Parcelize
class PastSearchItemModel( var id: Long? = null,
                           var itemName: String,
                           var itemSearchDate: LocalDateTime,
                           var itemImage: ByteArray)
 : ViewModel(), Parcelable {

//    private companion object : Parceler<PastSearchItemModel> {
//        override fun PastSearchItemModel.write(parcel: Parcel, flags: Int) {
//            // Custom write implementation
//        }
//
//        override fun create(parcel: Parcel): User {
//            // Custom read implementation
//        }
//    }



}
//constructor( var id: Long? = null,
//             var itemName: String,
//             var itemSearchDate: LocalDateTime,
//             var itemImage: Bitmap) : this()
