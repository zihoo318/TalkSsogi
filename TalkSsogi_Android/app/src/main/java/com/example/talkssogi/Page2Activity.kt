package com.example.talkssogi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import retrofit2.*
import retrofit2.converter.gson.GsonConverterFactory
import java.io.File
import com.example.talkssogi.model.ChatRoom
import kotlinx.coroutines.launch


class Page2Activity : AppCompatActivity() {

    private lateinit var user_name: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatRoomAdapter: ChatRoomAdapter
    private lateinit var bottomNavigationView: BottomNavigationView
    private val viewModel: MyViewModel by lazy {
        (application as MyApplication).viewModel
    }
    private val activityviewModel: ActivityAnalysisViewModel by viewModels()

    private lateinit var sharedPreferences: SharedPreferences
    private val PICK_FILE_REQUEST_CODE = 1
    private var selectedFileUri: Uri? = null
    private lateinit var uploadButton: ImageButton
    private lateinit var progressBar: ProgressBar
    private lateinit var SelectedFileText: TextView

    private var selectedCrnum: Int = -1 // 선택된 채팅방 ID 저장

    // Retrofit 설정 및 ApiService 인터페이스 생성
    private val retrofit = Retrofit.Builder()
        .baseUrl(Constants.BASE_URL) // 서버의 기본 URL을 Constants에서 가져옵니다.
        .addConverterFactory(GsonConverterFactory.create()) // JSON 변환을 위해 Gson을 사용합니다.
        .build()

    private val apiService: ApiService = retrofit.create(ApiService::class.java)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page2)

        // SharedPreferences에서 사용자 아이디를 가져온다
        sharedPreferences = getSharedPreferences("Session_ID", Context.MODE_PRIVATE)

        // user_name TextView 초기화
        user_name = findViewById(R.id.user_name)

        // SharedPreferences에서 사용자 아이디를 가져온다
        val userId = sharedPreferences.getString("Session_ID", "Unknown User")

        // 사용자 아이디를 TextView에 설정
        user_name.text = userId

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // Adapter 생성 시 클릭 리스너 전달
        chatRoomAdapter = ChatRoomAdapter(emptyList(), { chatRoom ->
            // 클릭 시 처리할 로직
            val intent = Intent(this, FragmentActivity::class.java)
            intent.putExtra("crnum", chatRoom.crnum) // 채팅방 ID를 전달
            startActivity(intent)
            Log.d("page2activity", "선택된 채팅방의 crnum: page2activity: ${chatRoom.crnum}") // crnum 값 로그 출력
        }, { chatRoom ->
            // 길게 눌렀을 때 삭제 다이얼로그 표시
            showDeleteConfirmationDialog(chatRoom)
        })
        recyclerView.adapter = chatRoomAdapter

        // 실시간으로 변화 확인하면서 화면 출력(=api요청 시 실행 됨)
        viewModel.chatRoomList.observe(this) { chatRooms ->
            Log.d("fetchChatRooms", "옵저버 감지 Received chat rooms: $chatRooms") // 로그 추가
            chatRooms?.let {
                chatRoomAdapter.submitList(it)
            }
        }

        // BottomNavigationView의 아이템 선택 리스너 설정
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_update -> {
                    // 업데이트 선택 시 처리
                    showSelectDialog()
                    true
                }

                R.id.navigation_add -> {
                    // 추가 선택 시 처리: Page3Activity로 이동
                    val userToken = sharedPreferences.getString("Session_ID", null)
                    val intent = Intent(this, Page3Activity::class.java)
                    intent.putExtra("Session_ID", userToken)
                    startActivity(intent)
                    true
                }
                R.id.navigation_logout -> {
                    // 로그아웃 선택 시 처리(로그아웃할건지 한번 물어보고, 액티비티1로 이동)
                    showLogoutConfirmationDialog()
                    true
                }

                else -> false
            }
        }

    }

    override fun onResume() {
        super.onResume()
        // 데이터 갱신
        viewModel.fetchChatRooms()
        Log.d("fetchChatRooms", "2에서 resume으로 갱신 Number of chat rooms")
    }

    //업데이트 누르면 뜨는 다이얼로그 코드
    private fun showSelectDialog() {
        // 다이얼로그 레이아웃을 Inflate 합니다.
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_chatroom_selection, null)
        val recyclerViewDialog = dialogView.findViewById<RecyclerView>(R.id.recyclerViewChatRooms)

        // 다이얼로그에 사용할 Adapter 생성
        val dialogChatRoomAdapter = DialogChatRoomAdapter(emptyList()) { chatRoom ->
            // 채팅방 클릭 시 처리할 로직을 여기에 추가 가능
            selectedCrnum = chatRoom.crnum // 선택된 채팅방 ID 저장
            Log.d("SelectedChatRoom", "selectedCrnum 선택된 채팅방의 값으로 바꾸기: $selectedCrnum")
        }

        recyclerViewDialog.layoutManager = LinearLayoutManager(this)
        recyclerViewDialog.adapter = dialogChatRoomAdapter

        // ViewModel을 통해 채팅방 목록을 가져와서 Adapter에 설정
        viewModel.chatRoomList.observe(this) { chatRooms ->
            chatRooms?.let {
                dialogChatRoomAdapter.submitList(it)
            }
        }

        // 다이얼로그 생성
        val dialog = AlertDialog.Builder(this)
            .setTitle("채팅방 선택")
            .setView(dialogView)
            .setPositiveButton("확인") { _, _ ->
                selectedCrnum = dialogChatRoomAdapter.selectedCrnum
                if (selectedCrnum != null) {
                    // 선택된 채팅방의 crnum을 로그로 출력
                    Log.d("SelectedChatRoom", "선택된 채팅방의 crnum: $selectedCrnum")
                    // 서버로 선택한 crnum 전송
                    showUpdateDialog()
                } else {
                    Log.d("SelectedChatRoom", "선택된 채팅방이 없습니다.") // 선택된 채팅방이 없는 경우 로그 출력
                }
            }
            .setNegativeButton("취소", null)
            .create()

        dialog.show()
    }

    // 새로운 파일로 업데이트하는 다이얼로그 띄우기
    private fun showUpdateDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_file_upload, null)

        SelectedFileText = dialogView.findViewById(R.id.SelectedFileText)
        uploadButton = dialogView.findViewById(R.id.uploadButton)
        progressBar = dialogView.findViewById(R.id.progressBar)
        val analysisStatus: TextView = dialogView.findViewById(R.id.analysis_status) // 추가된 부분

        SelectedFileText.setOnClickListener {
            openFileChooser()
        }

        // 다이얼로그 생성
        val dialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setTitle("파일 업로드")
            .setNegativeButton("취소") { dialog, _ ->
                dialog.dismiss()
            }

        val dialog = dialogBuilder.create()

        // 업로드 버튼 클릭 리스너 설정
        uploadButton.setOnClickListener {
            uploadButton.isEnabled = false
            progressBar.visibility = ProgressBar.VISIBLE // ProgressBar 표시
            analysisStatus.visibility = TextView.VISIBLE // 추가된 부분, "분석 중..." 텍스트 표시
            Log.d("SelectedChatRoom", "업로드 버튼 클릭 리스너에서 파일 전송 직전")
            selectedFileUri?.let { uri ->
                viewModel.updateFile(selectedCrnum, uri,
                    onSuccess = { crnum ->
                        // 파일 업로드 성공 후 분석 요청
                        viewModel.requestBasicPythonAnalysis(crnum) { result ->
                            when (result) {
                                in 0..Int.MAX_VALUE -> {
                                    // 분석 성공
                                    Log.d("SelectedChatRoom", "찐분석 성공")
                                    uploadButton.isEnabled = true  // 업로드 버튼 다시 활성화
                                    progressBar.visibility = View.GONE // ProgressBar 숨김
                                    analysisStatus.visibility = View.GONE // "분석 중..." 텍스트 숨김
                                    dialog.dismiss() // 다이얼로그 닫기
                                    viewModel.fetchChatRooms() // 다이얼로그 꺼지면 2페이지 목록 새로고침
                                    lifecycleScope.launch {
                                        activityviewModel.startBasicActivityAnalysis(crnum)
                                        Log.d("Page9", "업데이트 후 기본제공 분석 시작")
                                    }
                                }
                                -4 -> {
                                    // 분석 실패
                                    Log.d("SelectedChatRoom", "분석 실패")
                                    uploadButton.isEnabled = true
                                    progressBar.visibility = View.GONE
                                    analysisStatus.visibility = View.GONE
                                }
                                -2 -> {
                                    // 네트워크 오류
                                    Log.d("SelectedChatRoom", "네트워크 오류")
                                    uploadButton.isEnabled = true
                                    progressBar.visibility = View.GONE
                                    analysisStatus.visibility = View.GONE
                                }
                                else -> {
                                    Log.d("SelectedChatRoom", "알 수 없는 오류")
                                    uploadButton.isEnabled = true
                                    progressBar.visibility = View.GONE
                                    analysisStatus.visibility = View.GONE
                                }
                            }
                        }
                    },
                    onFailure = {
                        // 파일 업로드 실패
                        Log.d("SelectedChatRoom", "파일 업로드 실패")
                        uploadButton.isEnabled = true
                        progressBar.visibility = View.GONE
                        analysisStatus.visibility = View.GONE
                    }
                )
            } ?: run {
                Log.d("SelectedChatRoom", "selectedFileUri가 null임")
                uploadButton.isEnabled = true
                progressBar.visibility = View.GONE
                analysisStatus.visibility = View.GONE
            }
        }
        dialog.show()
    }

    private fun openFileChooser() {
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, PICK_FILE_REQUEST_CODE)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == PICK_FILE_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            selectedFileUri = data?.data
            Log.d("FilePicker", "Selected file URI파일이르르름: $selectedFileUri")
            SelectedFileText.text = selectedFileUri?.path ?: "파일 선택 실패"
        }
    }

    private fun getFileName(uri: Uri): String? {
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (cursor.moveToFirst()) {
                return cursor.getString(nameIndex)
            }
        }
        return null
    }

    data class UploadResponse(
        val message: String? // 혹은 메시지와 관련된 다른 프로퍼티
    )

    private fun getFileFromUri(uri: Uri): File? {
        val contentResolver = contentResolver
        val file = File(cacheDir, getFileName(uri) ?: "temp_file")
        contentResolver.openInputStream(uri)?.use { inputStream ->
            file.outputStream().use { outputStream ->
                inputStream.copyTo(outputStream)
            }
        }
        return file
    }

    private fun showDeleteConfirmationDialog(chatRoom: ChatRoom) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("채팅방 삭제")
        builder.setMessage("${chatRoom.name} 채팅방을 삭제하시겠습니까?")
        builder.setPositiveButton("삭제") { dialog, _ ->
            viewModel.deleteChatRoom(chatRoom.crnum)
            dialog.dismiss()
        }
        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("로그아웃")
        builder.setMessage("정말 로그아웃 하시겠습니까?")
        builder.setPositiveButton("로그아웃") { dialog, _ ->
            logout()
            dialog.dismiss()
        }
        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun logout() {
        // SharedPreferences의 세션 아이디 제거
        sharedPreferences.edit().remove("Session_ID").apply()
        // Page1Activity로 이동
        val intent = Intent(this, Page1Activity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // 백 스택을 모두 지움
        startActivity(intent) // 새로운 태스크로 Page1Activity를 시작
        finish() // 현재 Activity 종료
    }
}
