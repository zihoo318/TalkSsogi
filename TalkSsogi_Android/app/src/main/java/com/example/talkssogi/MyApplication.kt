package com.example.talkssogi

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MyApplication : Application() {
    val viewModel: MyViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this)
            .create(MyViewModel::class.java)
    }
}

data class UserIdResponse(val userIds: List<String>) // 전체 유저 아이디들 페이지1
data class User(val userId: String) //유저 아이디 db저장을 위한 클래스 페이지1
data class Page9SearchData(val selectedDate1: String?, val selectedDate2: String?,
                           val searchWho: String, val resultsItem: String) //페이지9에서 검색할 정보 담기
data class ImageURL(val imageUrl: String) // 서버에서 반환하는 이미지 URL 담아 옴 페이지9

class MyViewModel(application: Application) : AndroidViewModel(application) {
    private val BASE_URL = "http://10.0.2.2:8080/" // 실제 API 호스트 URL로 대체해야 됨 //에뮬레이터에서 호스트 컴퓨터의 localhost를 가리킴

    private val apiService = Retrofit.Builder() //api 사용을 위한 객체
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    private val _userIds = MutableLiveData<List<String>>() // 전체 유저 아이디 목록
    val userIds: LiveData<List<String>>
        get() = _userIds

    private val _chatRoomList = MutableLiveData<List<ChatRoom>>() // 한 유저의 채팅방 목록
    val chatRoomList: LiveData<List<ChatRoom>>
        get() = _chatRoomList

    private val _imageUrls = MutableLiveData<List<ImageURL>>()
    val imageUrls: LiveData<List<ImageURL>> get() = _imageUrls

    private val _participants = MutableLiveData<List<String>>() // 대화 참가자 이름 리스트
    val participants: LiveData<List<String>> get() = _participants

    init {
        fetchUserIds() // 전체 유저 아이디 목록
        fetchChatRooms() // 한 유저의 채팅방 목록
    }

    fun fetchUserIds() { // 서버에 요청하고 전체 유저 목록 받기
        apiService.getAllUserIds().enqueue(object : Callback<UserIdResponse> {
            override fun onResponse(call: Call<UserIdResponse>, response: Response<UserIdResponse>) {
                if (response.isSuccessful) {
                    val userIdResponse = response.body()
                    userIdResponse?.let { _userIds.value = it.userIds }
                } else {
                    // 오류 처리
                }
            }

            override fun onFailure(call: Call<UserIdResponse>, t: Throwable) {
                // 네트워크 오류 처리
            }
        })
    }

    fun getUserIdsLiveData(): LiveData<List<String>> { // 전체 유저 아이디 목록 getter
        return _userIds
    }

    fun addUserId(newID: String) {
        val currentList = _userIds.value?.toMutableList() ?: mutableListOf()
        currentList.add(newID)
        _userIds.value = currentList
    }

    fun fetchChatRooms() { // 서버에 요청하고 한 유저의 채팅방 목록 받기
        val userToken = getUserToken()
        // API 요청하여 채팅방 목록 가져오기
        apiService.getChatRooms(userID = userToken).enqueue(object : Callback<List<ChatRoom>> {
            override fun onResponse(call: Call<List<ChatRoom>>, response: Response<List<ChatRoom>>) {
                if (response.isSuccessful) {
                    // 성공적인 응답(onResponse)일 경우, 받은 채팅방 목록을 처리하여 ViewModel에 값을 설정
                    val chatRooms = response.body()
                    chatRooms?.let { _chatRoomList.value = it }
                } else {
                    // 오류 처리
                    // 예를 들어, response.code() 등을 이용하여 상세 오류 처리
                }
            }

            override fun onFailure(call: Call<List<ChatRoom>>, t: Throwable) {
                // 네트워크 오류 처리
                // 예를 들어, 네트워크 연결 상태 확인 및 사용자에게 오류 메시지 표시
            }
        })
    }

    fun sendUserId(userId: String) { //페이지1에서 쓸 유저 생성(아이디 입력 후 확인 버튼 누르면)
        apiService.sendUserId(User(userId)).enqueue(object : retrofit2.Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: retrofit2.Response<ResponseBody>) {
                if (response.isSuccessful) {
                    // 성공 처리
                    println("User created successfully")
                } else {
                    // 실패 처리
                    println("Failed to create user: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                // 네트워크 실패 처리
                println("Network error: ${t.message}")
            }
        })
    }

    fun getActivityAnalysisImage(searchData: Page9SearchData) { //페이지9에서 쓸 검색 정보 보내고 이미지 주소 받기
        apiService.getActivityAnalysisImage(searchData).enqueue(object : Callback<List<ImageURL>> {
            override fun onResponse(call: Call<List<ImageURL>>, response: Response<List<ImageURL>>) {
                if (response.isSuccessful) {
                    val ImageURL = response.body()
                    ImageURL?.let { _imageUrls.value = it }
                } else {
                    println("Failed to fetch images: ${response.errorBody()?.string()}")
                }
            }

            override fun onFailure(call: Call<List<ImageURL>>, t: Throwable) {
                println("Network error: ${t.message}")
            }
        })
    }

    fun loadParticipants(chatRoomId: Int) { // 채팅방 대화 참여자 이름 가져와서 리스트로 변환 (페이지9 + 윤지 파트? api 같은거 가져다 씀)
        viewModelScope.launch {
            try {
                val response = apiService.getChattingRoomMembers(chatRoomId).execute()
                if (response.isSuccessful) {
                    val participantsList = response.body() ?: emptyList()
                    _participants.postValue(participantsList)
                } else {
                    // 에러 처리
                    _participants.postValue(emptyList())
                }
            } catch (e: Exception) {
                // 예외 처리
                _participants.postValue(emptyList())
            }
        }
    }

    private fun getUserToken(): String {
        val sharedPreferences: SharedPreferences = getApplication<Application>().getSharedPreferences("Session_ID", Context.MODE_PRIVATE)
        return sharedPreferences.getString("userToken", "") ?: ""
    }
}