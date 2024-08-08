// CallerPredictionViewModelFactory.kt
package com.example.talkssogi

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class CallerPredictionViewModelFactory(private val apiService: ApiService) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(CallerPredictionViewModel::class.java)) {
            @Suppress("UNCHECKED_CAST")
            return CallerPredictionViewModel(apiService) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}
