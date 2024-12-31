package com.example.utslecture.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * ViewModel to retain blog summaries across configuration changes and fragment recreations.
 */
class HomeViewModel : ViewModel() {
    // LiveData to hold the list of summaries
    private val _summaries = MutableLiveData<List<String>>()
    val summaries: LiveData<List<String>> get() = _summaries

    // LiveData to track if summaries have been generated
    private val _summariesGenerated = MutableLiveData<Boolean>(false)
    val summariesGenerated: LiveData<Boolean> get() = _summariesGenerated

    /**
     * Sets the summaries and marks them as generated.
     */
    fun setSummaries(summaries: List<String>) {
        _summaries.value = summaries
        _summariesGenerated.value = true
    }

    /**
     * Resets the summaries and marks them as not generated.
     */
    fun resetSummaries() {
        _summaries.value = emptyList()
        _summariesGenerated.value = false
    }
}