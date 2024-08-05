package com.example.talkssogi

import okhttp3.MultipartBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("/api/userIds") //페이지1에서 쓸 유저 아이디 목록
    fun getAllUserIds(): Call<UserIdResponse> // 업로드 성공 여부를 확인하기 위한 응답

    @POST("/api/userId") //페이지1에서 쓸 유저 생성(아이디 입력 후 확인 버튼 누르면)
    fun sendUserId(
        @Body user: User
    ): Call<ResponseBody>

    @GET("/api/chatrooms") // 채팅방 목록
    fun getChatRooms(
        @Query("ID") userID: String?
    ): Call<Map<Int, String>> // Map<Integer, String> 형태의 응답

    @Multipart // 파일 전송
    @POST("/api/uploadfile")
    fun uploadFile(
        @Part file: MultipartBody.Part, // 업로드할 파일을 MultipartBody.Part 형식으로 전달
        @Query("userId") userId: String?,
        @Query("headcount") headcount: Int?
    ): Call<Map<String, Any>>

    @Multipart
    @POST("/api/updatefile/{crnum}")
    fun updateFile(@Path("crnum") crnum: Int,
                   @Part file: MultipartBody.Part
    ): Call<Map<String, Any>>

    @GET("/api/analysis/basic-python") // 기본 분석 요청(uploadFile을 실행하고 같은 메서드에서 같이 요청 실행)
    fun runBasicPythonAnalysis(
        @Query("crnum") crnum: Int
    ): Call<String>


    @GET("/api/rankings/basicRankingResults")
    fun getBasicRankingResults(
        @Query("crnum") crnum: Int
    ): Call<Map<String, Map<String, String>>>

    @GET("/api/rankings/searchRankingResults")  //페이지7에서 사용할 랭킹 배열(검색 시)
    fun getSearchRankingResults(
        @Query("crnum") crnum: Int,
        @Query("keyword") keyword: String      //keyword와 userId를 넘겨준다.
    ): Call<Map<String, Map<String, String>>>

    //가을 api 수정사항(페이지8)
    @GET("/api/analysis/basicActivityAnalysis")
    fun getActivityAnalysis(
        @Query("crnum") crnum: Int
    ): Call<List<String>>

    @GET("/api/members/{crnum}") // 채팅방 멤버 목록 가져오기
    suspend  fun getChattingRoomMembers(
        @Path("crnum") crnum: Int
    ): Response<List<String>> // Response 객체

    @GET("/api/analysis/wordCloudImageUrl/{crnum}/{userId}") // 특정 사용자의 워드 클라우드 이미지 URL 가져오기
    fun getWordCloudImageUrl(
        @Path("crnum") crnum: Int,
        @Path("userId") userId: Int
    ): Call<List<ImageURL>>

    @POST("/api/analysis/personalActivityAnalysisImage")
    fun getActivityAnalysisImage(
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String,
        @Query("searchWho") searchWho: String,
        @Query("resultsItem") resultsItem: String,
        @Query("crnum") crnum: Int
    ): Call<String>


    @GET("/api/participants/{chatRoomId}") // 페이지 9에서 사용한 검색 대상 선택을 위해 대화 참가자 이름 목록 가져오기
    fun getParticipants(
        @Path("chatRoomId") chatRoomId: Int
    ): Call<List<String>>

    @DELETE("/api/chatrooms/{crnum}")
    fun deleteChatRoom(@Path("crnum") crnum: Int): Call<Void>

    // 로그인 API 호출
    @POST("/api/login")
    fun login(
        @Body loginRequest: LoginRequest
    ): Call<ResponseBody>

    // 회원가입 API 호출
    @POST("/api/register")
    fun register(
        @Body registerRequest: RegisterRequest
    ): Call<ResponseBody>

    // 사용자 ID 유효성 확인 API 호출
    @GET("/api/checkUserId")
    fun checkUserId(
        @Query("userId") userId: String
    ): Call<ResponseBody>

}
