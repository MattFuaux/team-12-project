package com.team12.fruitwatch.ui.main.fragments

import com.team12.fruitwatch.ui.main.fragments.search.PastSearchItemModel
import java.io.File

interface FragmentDataLink {

    fun startTextSearch(pastSearchItemModel: PastSearchItemModel)
    fun startImageSearch(imageFile: File)
}