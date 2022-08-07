package com.team12.fruitwatch.ui.main.fragments.results

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class ResultsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is search results Fragment"
    }
    val text: LiveData<String> = _text
}