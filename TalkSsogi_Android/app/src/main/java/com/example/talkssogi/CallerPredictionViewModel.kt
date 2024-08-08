// CallerPredictionViewModel.kt
package com.example.talkssogi

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class CallerPredictionViewModel(private val apiService: ApiService) : ViewModel() {
    private val _callerPrediction = MutableLiveData<String>()
    val callerPrediction: LiveData<String> get() = _callerPrediction

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun fetchCallerPrediction(crnum: Int, keyword: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getCallerPrediction(crnum, keyword)
                response.enqueue(object : Callback<String> {
                    override fun onResponse(call: Call<String>, response: Response<String>) {
                        if (response.isSuccessful) {
                            _callerPrediction.value = response.body()
                        } else {
                            _error.value = "Error: ${response.message()}"
                        }
                    }

                    override fun onFailure(call: Call<String>, t: Throwable) {
                        _error.value = "Failed to fetch data: ${t.message}"
                    }
                })
            } catch (e: Exception) {
                Log.e("CallerPredictionViewModel", "Error fetching caller prediction", e)
                _error.value = "Unexpected error: ${e.message}"
            }
        }
    }
}
