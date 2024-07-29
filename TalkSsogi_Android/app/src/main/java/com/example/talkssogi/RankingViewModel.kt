package com.example.talkssogi

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch

class RankingViewModel : ViewModel() {
    private val _basicRankingResults = MutableLiveData<Map<String, List<String>>>()
    val basicRankingResults: LiveData<Map<String, List<String>>> get() = _basicRankingResults

    private val _searchRankingResults = MutableLiveData<Map<String, List<String>>>()
    val searchRankingResults: LiveData<Map<String, List<String>>> get() = _searchRankingResults

    //8페이지 (가을추가)
    private val _activityAnalysis = MutableLiveData<Map<String, List<String>>>()
    val activityAnalysis: LiveData<Map<String, List<String>>> get() = _activityAnalysis
    //

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    fun fetchBasicRankingResults(userId: String) {
        viewModelScope.launch {
            try {
                val basicResults = RankingRepository.getBasicRankingResults(userId)

                Log.d("RankingViewModel", "Basic Results: $basicResults")
                _basicRankingResults.value = basicResults

            } catch (e: Exception) {
                Log.e("RankingViewModel", "Error fetching basic ranking results", e)
                _error.value = "데이터를 가져오는 중 문제가 발생했습니다. 나중에 다시 시도해 주세요."
            }
        }
    }

    fun fetchSearchRankingResults(userId: String, keyword: String) {
        viewModelScope.launch {
            try {
                val searchResults = RankingRepository.getSearchRankingResults(userId,keyword)
                Log.d("RankingViewModel", "Search Results: $searchResults")
                _searchRankingResults.value = searchResults
            } catch (e: Exception) {
                Log.e("RankingViewModel", "Error fetching search ranking results", e)
                _error.value = "데이터를 가져오는 중 문제가 발생했습니다. 나중에 다시 시도해 주세요."
            }
        }
    }
}