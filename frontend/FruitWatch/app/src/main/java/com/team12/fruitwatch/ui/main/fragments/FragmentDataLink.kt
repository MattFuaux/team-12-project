package com.team12.fruitwatch.ui.main.fragments

import com.team12.fruitwatch.database.entities.PastSearch
import com.team12.fruitwatch.ui.main.fragments.search.PastSearchItemModel
import java.io.File

interface FragmentDataLink {

    fun startTextSearch(pastSearch: PastSearch)
    fun startImageSearch(imageFile: File)
    fun openSearchFrag()
    fun openCamera()
}