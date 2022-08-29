package com.team12.fruitwatch.ui.main.fragments.search

import android.os.Parcelable
import androidx.lifecycle.ViewModel
import kotlinx.parcelize.Parcelize
import java.time.LocalDateTime

@Parcelize
class PastSearchItemModel( var id: Long? = null,
                           var itemName: String,
                           var itemSearchDate: LocalDateTime,
                           var itemImage: ByteArray)
 : ViewModel(), Parcelable
