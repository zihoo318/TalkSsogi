package com.example.talkssogi

import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.awaitResponse
import retrofit2.converter.gson.GsonConverterFactory


object RankingRepository {
    private val apiService: ApiService

    var gson: Gson = GsonBuilder()
        .setLenient()
        .create()

    init {
        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL) // 실제 서버 주소로 변경
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        apiService = retrofit.create(ApiService::class.java)
    }

    suspend fun getBasicRankingResults(crnum: Int): Map<String, Map<String, String>> {
        return withContext(Dispatchers.IO) {
            try {
                val response = apiService.getBasicRankingResults(crnum).awaitResponse()
                if (response.isSuccessful) {
                    response.body() ?: emptyMap()
                } else {
                    Log.e("RankingRepository", "Error: ${response.errorBody()?.string()}")
                    emptyMap()
                }
            } catch (e: Exception) {
                Log.e("RankingRepository", "Error fetching basic ranking results", e)
                emptyMap()
            }
        }
    }

    suspend fun getSearchRankingResults(crnum: Int, keyword: String, callback: (Map<String, Map<String, String>>?) -> Unit) {
        val call = apiService.getSearchRankingResults(crnum, keyword)
        call.enqueue(object : Callback<Map<String, Map<String, String>>> {
            override fun onResponse(
                call: Call<Map<String, Map<String, String>>>,
                response: Response<Map<String, Map<String, String>>>
            ) {
                if (response.isSuccessful) {
                    callback(response.body())
                } else {
                    Log.e("RankingRepository", "Error: ${response.errorBody()?.string()}")
                    callback(null)
                }
            }

            override fun onFailure(call: Call<Map<String, Map<String, String>>>, t: Throwable) {
                Log.e("RankingRepository", "Error fetching search ranking results", t)
                callback(null)
            }
        })
    }
}