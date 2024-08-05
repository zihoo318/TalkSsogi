package com.example.talkssogi

import com.example.talkssogi.model.ChatRoom
import android.app.Application
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

class MyApplication : Application() {
    //page2Activity에서 사용할 apiService
    lateinit var apiService: ApiService
    val viewModel: MyViewModel by lazy {
        ViewModelProvider.AndroidViewModelFactory.getInstance(this)
            .create(MyViewModel::class.java)
    }
}

data class UserIdResponse(val userIds: List<String>) // 전체 유저 아이디들 페이지1
data class User(val userId: String) //유저 아이디 db저장을 위한 클래스 페이지1
data class ImageURL(val imageUrl: String) // 서버에서 반환하는 이미지 URL 담아 옴 페이지9
data class LoginRequest(val userId: String)
data class RegisterRequest(val userId: String)

class MyViewModel(application: Application) : AndroidViewModel(application) {
    // private val BASE_URL = "http://10.0.2.2:8080/" // 실제 API 호스트 URL로 대체해야 됨 //에뮬레이터에서 호스트 컴퓨터의 localhost를 가리킴
    private val BASE_URL = "http://192.168.45.165:8080/"    // 실제 안드로이드 기기에서 실행 할 때

    // 테스트 중 원인 분석을 위한 로그 보기 설정 (OkHttpClient 설정)
    val logging = HttpLoggingInterceptor().apply {
        setLevel(HttpLoggingInterceptor.Level.BODY)
    }

    // Accept 헤더를 추가하는 인터셉터
    private val acceptHeaderInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val newRequest = originalRequest.newBuilder()
            .addHeader("Accept", "application/json") // Accept 헤더 추가
            .build()
        chain.proceed(newRequest)
    }

    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(HttpLoggingInterceptor().apply { setLevel(HttpLoggingInterceptor.Level.BODY) })
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    private val apiService = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(ApiService::class.java)

    /*val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(acceptHeaderInterceptor) // Accept 헤더 인터셉터
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .build()

    var gson= GsonBuilder().setLenient().create()
    private val apiService = Retrofit.Builder() //api 사용을 위한 객체
        .baseUrl(Constants.BASE_URL)
        .client(client) // OkHttpClient를 Retrofit에 설정 (원인 분석을 위한 로그를 보기위한 설정)
        .addConverterFactory(GsonConverterFactory.create(gson)) // JSON 변환
        .build()
        .create(ApiService::class.java)*/

    private val _userIds = MutableLiveData<List<String>>() // 전체 유저 아이디 목록
    val userIds: LiveData<List<String>>
        get() = _userIds

    private val _chatRoomList = MutableLiveData<List<ChatRoom>>() // 한 유저의 채팅방 목록
    val chatRoomList: LiveData<List<ChatRoom>>
        get() = _chatRoomList

    //가을 추가 코드
    private val _headcount = MutableLiveData<Int>() // 파일 업로드 버튼 클릭시 인원수
    val headcount: LiveData<Int>
        get() = _headcount

    private val _fileUri = MutableLiveData<String>() // 파일 업로드 버튼 클릭시 파일 URI
    val fileUri: LiveData<String>
        get() = _fileUri
//가을 추가 코드

    // 페이지3에서 파일 저장 후 페이지2의 목록 갱신을 실행하기 위한 파일 업로드 결과 데이터이자 분석을 위한 채팅방 번호(crNum)
    private val _uploadResult =
        MutableLiveData<Int?>() // 0이상: 채팅방 번호, -1: 업로드 실패, -2: 네트워크 오류, -3: 경로 오류, -4: 분석 실패
    val uploadResult: LiveData<Int?> = _uploadResult

    // 페이지2에서 재분석 요청 시 분석이 끝나고 다음 동작을 하기 위한 라이브데이터
    private val _REanalysisResult =
        MutableLiveData<Int?>()// 0이상: 채팅방 번호, -1: 업로드 실패, -2: 네트워크 오류, -3: 경로 오류, -4: 분석 실패
    val REanalysisResult: MutableLiveData<Int> = MutableLiveData()

    // 페이지3에서 분석 요청 시 분석이 끝나고 다음 동작을 하기 위한 라이브데이터
    private val _analysisResult =
        MutableLiveData<Int?>()// 0이상: 채팅방 번호, -1: 업로드 실패, -2: 네트워크 오류, -3: 경로 오류, -4: 분석 실패
    val analysisResult: MutableLiveData<Int> = MutableLiveData()

    // 업로드 결과를 업데이트하는 함수
    fun updateUploadResult(result: Int?) {
        _uploadResult.value = result
    }
    fun updateanalysisResult(result: Int?) {
        _analysisResult.value = result
    }

    init {
        fetchUserIds() // 전체 유저 아이디 목록
        fetchChatRooms() // 한 유저의 채팅방 목록
    }

    fun fetchUserIds() { // 서버에 요청하고 전체 유저 목록 받기 페이지1
        apiService.getAllUserIds().enqueue(object : Callback<UserIdResponse> {
            override fun onResponse(
                call: Call<UserIdResponse>,
                response: Response<UserIdResponse>
            ) {
                if (response.isSuccessful) {
                    val userIdResponse = response.body()
                    userIdResponse?.let { _userIds.value = it.userIds }
                } else {
                    // 오류 처리
                    Log.e("fetchUserIds", "Error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<UserIdResponse>, t: Throwable) {
                // 네트워크 오류 처리
                Log.e("fetchUserIds", "Network error: ${t.message}")
            }
        })
    }

    fun getUserIdsLiveData(): LiveData<List<String>> { // 전체 유저 아이디 목록 getter
        return _userIds
    }

    fun addUserId(newID: String) {
        // 예시로 로컬 LiveData에 추가하는 코드
        // 실제로는 서버나 데이터베이스에 추가해야 함
        val currentList = userIds.value?.toMutableList() ?: mutableListOf()
        currentList.add(newID)
        _userIds.value = currentList
    }

    fun fetchChatRooms() { // 서버에 요청하고 한 유저의 채팅방 목록 받기
        val userToken = getUserToken()
        // API 요청하여 채팅방 목록 가져오기
        apiService.getChatRooms(userID = userToken).enqueue(object : Callback<Map<Int, String>> {
            override fun onResponse(
                call: Call<Map<Int, String>>,
                response: Response<Map<Int, String>>
            ) {
                if (response.isSuccessful) {
                    // 성공적인 응답(onResponse)일 경우, 받은 채팅방 목록을 처리하여 ViewModel에 값을 설정
                    val chatRoomMap = response.body()

                    // Map을 List<ChatRoom>으로 변환
                    val chatRoomList = chatRoomMap?.map { (roomNumber, roomName) ->
                        ChatRoom(
                            crnum = roomNumber,
                            name = roomName,
                            profileImageResId = R.drawable.profile_placeholder // 기본 이미지 리소스 설정
                        )
                    } ?: emptyList() // Map이 null인 경우 빈 리스트로 대체

                    Log.d("fetchChatRooms", "실제 api실행 Number of chat rooms: ${chatRoomList.size}")


                    // ViewModel에 값 설정
                    _chatRoomList.value = chatRoomList
                } else {
                    // 오류 처리
                    Log.e("fetchChatRooms", "Error: ${response.code()} - ${response.message()}")
                }
            }

            override fun onFailure(call: Call<Map<Int, String>>, t: Throwable) {
                // 네트워크 오류 처리
                Log.e("fetchChatRooms", "Network error: ${t.message}")
            }
        })
    }

    // 채팅방 삭제 메서드
    fun deleteChatRoom(crnum: Int) {
        apiService.deleteChatRoom(crnum).enqueue(object : Callback<Void> {
            override fun onResponse(call: Call<Void>, response: Response<Void>) {
                if (response.isSuccessful) {
                    // 삭제 성공 시 채팅방 목록 갱신
                    fetchChatRooms()
                } else {
                    Log.e("DeleteChatRoom", "채팅방 삭제를 실패하였습니다: ${response.message()}")
                }
            }
            override fun onFailure(call: Call<Void>, t: Throwable) {
                Log.e("DeleteChatRoom", "Network error: ${t.message}")
            }
        })
    }

    fun sendUserId(userId: String) { //페이지1에서 쓸 유저 생성(아이디 입력 후 확인 버튼 누르면)
        apiService.sendUserId(User(userId)).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
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

    private fun getUserToken(): String {
        val sharedPreferences: SharedPreferences =
            getApplication<Application>().getSharedPreferences("Session_ID", Context.MODE_PRIVATE)
        return sharedPreferences.getString("Session_ID", "") ?: ""
    }

    //파일 업로드 버튼 클릭시 파일과 인원수 부모델에 저장
    fun setHeadCountAndFile(headcount: Int, fileUri: String) {
        _headcount.value = headcount
        _fileUri.value = fileUri
    }

    // 모바일의 파일 경로 알아내기
    fun getPathFromUri(uri: Uri): String? {
        val projection = arrayOf(MediaStore.Images.Media.DATA)
        val cursor =
            getApplication<Application>().contentResolver.query(uri, projection, null, null, null)
        cursor?.use {
            val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
            if (it.moveToFirst()) {
                return it.getString(columnIndex)
            }
        }
        return null
    }

    // 파일 업로드 api 실행 메서드 페이지
    fun uploadFile(fileUri: Uri, userId: String, headcount: Int, callback: (Int) -> Unit) {
        // Uri에서 Path 만들기
        val filePath = getPathFromUri(fileUri)
        val file = filePath?.let { File(it) }

        // null인지 확인
        if (file != null && file.exists()) {
            // RequestBody로 바꾸기
            val requestFile = file.asRequestBody("multipart/form-data".toMediaType())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            Log.i("fetchChatRooms", "api보내기 직전 파일 업로드 전, crnum: ${_uploadResult.value}")

            // API 호출
            apiService.uploadFile(body, userId, headcount)
                .enqueue(object : Callback<Map<String, Any>> {
                    override fun onResponse(
                        call: Call<Map<String, Any>>,
                        response: Response<Map<String, Any>>
                    ) {
                        Log.i("fetchChatRooms", "api호출")
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            Log.i("fetchChatRooms", "API 응답 내용: $responseBody")
                            // 응답에서 'crNum' 값을 추출
                            val crNumRaw = responseBody?.get("crNum")
                            // 'crnum' 값이 Double인지 확인하고 정수로 변환
                            val crnum = (crNumRaw as? Number)?.toInt()
                            Log.i("fetchChatRooms", "파일 업로드 직후 변수에 넣기전, crnum: ${crnum}")
                            if (crnum != null) {
                                Log.i("fetchChatRooms", "파일 업로드 성공하고 아직 분석 전, crnum: $crnum")
                                callback(crnum) // 업로드 성공 결과를 콜백으로 전달
                            } else {
                                Log.e("FileUpload", "crnum을 찾을 수 없음")
                                callback(-1) // 업로드 실패 코드 전달
                            }
                        } else {
                            Log.e("FileUpload", "파일 업로드 실패: ${response.errorBody()?.string()}")
                            callback(-1) // 업로드 실패 코드 전달
                        }
                    }

                    override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                        Log.e("FileUpload", "네트워크 오류: ${t.message}")
                        callback(-2) // 네트워크 오류 코드 전달
                    }
                })
        } else {
            Log.e("FileUpload", "파일 경로가 잘못되었거나 파일이 존재하지 않습니다.")
            callback(-3) // 파일 경로 오류 코드 전달
        }
    }


    // 파일 업로드 api 실행 메서드 페이지
    fun updateFile(crnum: Int, fileUri: Uri, onSuccess: (Int) -> Unit, onFailure: () -> Unit) {
        // URI에서 File 객체를 가져옵니다.
        val filePath = getPathFromUri(fileUri)
        val file = filePath?.let { File(it) }

        // 파일이 null이거나 존재하지 않을 경우 처리
        if (file != null && file.exists()) {
            // RequestBody와 MultipartBody.Part를 생성합니다.
            val requestFile = file.asRequestBody("application/octet-stream".toMediaType())
            val body = MultipartBody.Part.createFormData("file", file.name, requestFile)

            Log.i("SelectedChatRoom", "업데이트 API 호출 전, crnum: $crnum")

            // API 호출
            apiService.updateFile(crnum, body)
                .enqueue(object : Callback<Map<String, Any>> {
                    override fun onResponse(
                        call: Call<Map<String, Any>>,
                        response: Response<Map<String, Any>>
                    ) {
                        Log.i("SelectedChatRoom", "API 호출")
                        if (response.isSuccessful) {
                            val responseBody = response.body()
                            Log.i("SelectedChatRoom", "API 응답 내용: $responseBody")
                            // 응답에서 'filePath'와 'crNum' 값을 추출
                            val filePath = responseBody?.get("filePath") as? String
                            val crNumRaw = responseBody?.get("crNum")
                            // 'crNum' 값을 Double인지 확인하고 정수로 변환
                            val crNum = (crNumRaw as? Number)?.toInt()
                            Log.i("SelectedChatRoom", "파일 업데이트 성공 후 응답에 있는 파일 경로: $filePath + crNum: $crNum")

                            if (crNum != null) {
                                onSuccess(crNum) // 파일 업로드 성공 후 crNum 전달
                            } else {
                                onFailure() // crNum이 null일 경우 실패 처리
                            }
                        } else {
                            Log.e("SelectedChatRoom", "파일 업데이트 실패: ${response.errorBody()?.string()}")
                            onFailure() // 파일 업데이트 실패
                        }
                    }

                    override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                        Log.e("SelectedChatRoom", "네트워크 오류: ${t.message}")
                        onFailure() // 네트워크 오류 처리
                    }
                })
        } else {
            Log.e("SelectedChatRoom", "파일 경로가 잘못되었거나 파일이 존재하지 않습니다.")
            onFailure() // 파일 경로 오류 처리
        }
    }


    // Uri를 MultipartBody.Part로 변환하는 메서드
    fun convertUriToFilePart(uri: Uri, context: Context): MultipartBody.Part? {
        val file = getFileFromUri(uri, context) ?: return null

        val requestFile = file.asRequestBody("multipart/form-data".toMediaType())
        return MultipartBody.Part.createFormData("file", file.name, requestFile)
    }

    // 파일을 Uri에서 가져오는 메서드
    private fun getFileFromUri(uri: Uri, context: Context): File? {
        val fileName = getFileName(uri, context) ?: return null
        val file = File(context.cacheDir, fileName)
        context.contentResolver.openInputStream(uri)?.use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return file
    }

    // Uri의 파일 이름을 가져오는 메서드
    private fun getFileName(uri: Uri, context: Context): String? {
        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                val fileName = cursor.getString(nameIndex)
                Log.d("FileName", "업데이트 할 파일 이름: $fileName")  // 로그 출력
                return fileName
            }
        }
        return null
    }

    // 기본 분석 요청 API 호출 함수
    fun RErequestBasicPythonAnalysis(crnum: Int, callback: (Int) -> Unit) {
        apiService.runBasicPythonAnalysis(crnum).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    Log.i("SelectedChatRoom", "분석 성공: ${response.body()}")
                    callback(crnum) // 분석 성공 시 crnum 콜백으로 전달
                } else {
                    Log.e("SelectedChatRoom", "분석 실패: ${response.errorBody()?.string()}")
                    callback(-4) // 분석 실패 시 오류 코드 전달
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("SelectedChatRoom", "네트워크 오류: ${t.message}")
                callback(-2) // 네트워크 오류 코드 전달
            }
        })
    }


    // 기본 분석 요청 API 호출 함수
    fun requestBasicPythonAnalysis(crnum: Int, callback: (Int) -> Unit) {
        apiService.runBasicPythonAnalysis(crnum).enqueue(object : Callback<String> {
            override fun onResponse(call: Call<String>, response: Response<String>) {
                if (response.isSuccessful) {
                    Log.i("fetchChatRooms", "분석 성공: ${response.body()}")
                    callback(crnum) // 분석 성공 결과를 콜백으로 전달
                } else {
                    Log.e("fetchChatRooms", "분석 실패: ${response.errorBody()?.string()}")
                    callback(-4) // 분석 실패 코드 전달
                }
            }

            override fun onFailure(call: Call<String>, t: Throwable) {
                Log.e("fetchChatRooms", "네트워크 오류: ${t.message}")
                callback(-2) // 네트워크 오류 코드 전달
            }
        })
    }

    // SharedPreferences에 사용자 ID를 저장
    fun saveUserIdToSharedPreferences(userId: String) {
        val sharedPreferences = getApplication<Application>().getSharedPreferences("Session_ID", Context.MODE_PRIVATE)
        with(sharedPreferences.edit()) {
            putString("Session_ID", userId)
            apply()
        }
    }

    // 서버에 사용자 ID 존재 여부를 확인하는 요청 API 호출
    fun checkUserIdExists(userId: String): LiveData<Boolean> {
        val result = MutableLiveData<Boolean>()
        apiService.checkUserId(userId).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    val responseBody = response.body()?.string()
                    result.value = responseBody == "Username is already in use"
                } else {
                    Log.e("checkUserIdExists", "Error: ${response.code()} - ${response.message()}")
                    result.value = false
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("checkUserIdExists", "Network error: ${t.message}")
                result.value = false
            }
        })
        return result
    }

    // 서버에 회원가입 요청 (사용자 ID 등록) API 호출
    fun registerUserId(userId: String, callback: (Boolean) -> Unit) {
        apiService.register(RegisterRequest(userId)).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    callback(true)
                } else {
                    Log.e("registerUserId", "Error: ${response.code()} - ${response.message()}")
                    callback(false)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("registerUserId", "Network error: ${t.message}")
                callback(false)
            }
        })
    }

    // 서버에 로그인 요청 API 호출
    fun loginUserId(userId: String, callback: (Boolean) -> Unit) {
        apiService.login(LoginRequest(userId)).enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful) {
                    callback(true)
                } else {
                    Log.e("loginUserId", "Error: ${response.code()} - ${response.message()}")
                    callback(false)
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Log.e("loginUserId", "Network error: ${t.message}")
                callback(false)
            }
        })
    }

    // 지정된 페이지로 이동
    fun navigateToNextPage(activity: AppCompatActivity, nextPage: Class<*>) {
        val intent = Intent(activity, nextPage)
        activity.startActivity(intent)
    }
}