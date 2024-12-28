package com.example.utslecture.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class HomeViewModel : ViewModel() {
    private val _summariesGenerated = MutableLiveData<Boolean>(false)
    val summariesGenerated: LiveData<Boolean> get() = _summariesGenerated

    private val _summaries = MutableLiveData<List<String>>()
    val summaries: LiveData<List<String>> get() = _summaries

    fun setSummaries(summariesList: List<String>) {
        _summaries.value = summariesList
        _summariesGenerated.value = true
    }

    fun resetSummaries() {
        _summariesGenerated.value = false
        _summaries.value = emptyList()
    }
}