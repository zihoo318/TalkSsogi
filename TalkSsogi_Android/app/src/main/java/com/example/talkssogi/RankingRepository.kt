package com.example.talkssogi

import android.util.Log
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RankingRepository {
    private val apiService: ApiService

    init {
        val retrofit = Retrofit.Builder()
//            .baseUrl("http://10.0.2.2:8080/") // 실제 서버 주소로 변경 // 에뮬레이터에서 호스트 컴퓨터의 localhost를 가리킴
            .baseUrl(Constants.BASE_URL) // 실제 서버 주소로 변경 // 에뮬레이터에서 호스트 컴퓨터의 localhost를 가리킴
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    suspend fun getBasicRankingResults(crnum: Int): Map<String, List<String>> {
        return try {
            val response = apiService.getBasicRankingResults(crnum)
            response.body() ?: emptyMap()
        } catch (e: Exception) {
            Log.e("RankingRepository", "Error fetching basic ranking results", e)
            emptyMap()
        }
    }

    suspend fun getSearchRankingResults(crnum: Int, keyword: String): Map<String, List<String>> {
        return try {
            val response = apiService.getSearchRankingResults(crnum, keyword)
            response.body() ?: emptyMap()
        } catch (e: Exception) {
            Log.e("RankingRepository", "Error fetching search ranking results", e)
            emptyMap()
        }
    }
}