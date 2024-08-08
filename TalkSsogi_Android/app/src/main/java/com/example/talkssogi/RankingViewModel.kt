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

class RankingViewModel : ViewModel() {
    private val _basicRankingResults = MutableLiveData<Map<String, Map<String, String>>>()
    val basicRankingResults: LiveData<Map<String, Map<String, String>>> get() = _basicRankingResults

    private val _searchRankingResults = MutableLiveData<Map<String, Map<String, String>>?>()
    val searchRankingResults: LiveData<Map<String, Map<String, String>>?> get() = _searchRankingResults

    //8페이지 (가을추가)
    private val _activityAnalysis = MutableLiveData<Map<String, List<String>>>()
    val activityAnalysis: LiveData<Map<String, List<String>>> get() = _activityAnalysis
    //

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> get() = _error

    suspend fun fetchBasicRankingResults(crnum: Int) {
        viewModelScope.launch {
            try {
                val results = RankingRepository.getBasicRankingResults(crnum)
                Log.d("ApiRequest", "출력된 crnum에 해당하는 ranking result 요청: $crnum")
                Log.d("ApiResponse", "basic 랭킹 결과: $results")
                _basicRankingResults.value = results
            } catch (e: Exception) {
                Log.e("RankingViewModel", "Error fetching basic ranking results", e)
                _error.value = "데이터를 가져오는 중 문제가 발생했습니다. 나중에 다시 시도해 주세요."
            }
        }
    }

    suspend fun fetchSearchRankingResults(crnum: Int, keyword: String) {
        RankingRepository.getSearchRankingResults(crnum, keyword) { results ->
            if (results != null) {
                Log.d("ApiRequest", "출력된 crnum에 해당하는 search ranking result 요청: $crnum")
                Log.d("ApiResponse", "search 랭킹 결과: $results")
                _searchRankingResults.postValue(results)
            } else {
                _error.postValue("search 랭킹 에러!!! fetching search ranking results")
            }
        }
    }
}