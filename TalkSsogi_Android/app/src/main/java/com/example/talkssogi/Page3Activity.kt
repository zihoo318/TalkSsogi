package com.example.talkssogi

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
//가을 추가 코두
import android.graphics.drawable.AnimationDrawable


class Page3Activity : AppCompatActivity() {

    companion object {
        const val REQUEST_CODE_SELECT_FILE = 1
    }

    private lateinit var tvPeople: TextView
    private lateinit var etPeopleCount: EditText
    private lateinit var btnPeople: ImageButton
    private lateinit var tvSelectedFile: TextView
    private lateinit var imageView: ImageView
    private lateinit var textView: TextView
    private lateinit var pot: ImageView
    private lateinit var speech_bubble: ImageView
    private lateinit var btnUploadFile: ImageButton
    private lateinit var loadingIndicator: ProgressBar // 분석 중 띄우는 바
    private val viewModel: MyViewModel by lazy { //공유 뷰모델
        (application as MyApplication).viewModel
    }
    private var selectedFileUri: Uri? = null // 파일 경로
    private lateinit var sharedPreferences: SharedPreferences


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page3_activity)

        // UI 요소의 참조를 가져옵니다.
        tvPeople = findViewById(R.id.tvPeople) // .text에 인원 수
        etPeopleCount = findViewById(R.id.etPeopleCount) //인원 수 뷰
        tvSelectedFile = findViewById(R.id.tvSelectedFile)
        imageView = findViewById(R.id.undo_button)
        textView = findViewById(R.id.title_analyze)
        speech_bubble = findViewById(R.id.analyze_speech)
        pot = findViewById(R.id.pot_page3)
        btnUploadFile = findViewById(R.id.btnUploadName)
        loadingIndicator = findViewById(R.id.loadingIndicator) // 분석 중 띄우는 바(띄워져 있는데 안 보이게 해둠)
        // SharedPreferences에서 사용자 아이디를 가져온다
        sharedPreferences = getSharedPreferences("Session_ID", Context.MODE_PRIVATE)

        // AnimationDrawable 설정
        val animationDrawable = getDrawable(R.drawable.animation_loding) as? AnimationDrawable
        loadingIndicator.indeterminateDrawable = animationDrawable


        // null 체크 추가
        checkForNulls()

        // tvSelectedFile 클릭 이벤트 설정
        tvSelectedFile.setOnClickListener {
            openFileChooser()
        }

        // 파일 업로드 버튼 클릭 이벤트 설정
        btnUploadFile.setOnClickListener {
            showLoadingIndicator()
            Log.d("fetchChatRooms", "인디케이터 보이게 바꿈")

            // 파일과 인원 수를 서버에 업로드
            uploadFileAndPeopleCount()
        }


        // TextWatcher 설정(인원수가 수정될 때마다 바로 업데이트)
        etPeopleCount.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                handlePeopleCount()
            }
        })

        // 기타 초기화 작업 수행
        imageView.setImageResource(R.drawable.smile)
    }

    private fun checkForNulls() {
        if (!::tvPeople.isInitialized) Log.e("Page3Activity", "tvPeople is null")
        if (!::etPeopleCount.isInitialized) Log.e("Page3Activity", "etPeopleCount is null")
        if (!::btnPeople.isInitialized) Log.e("Page3Activity", "btnPeople is null")
        if (!::tvSelectedFile.isInitialized) Log.e("Page3Activity", "tvSelectedFile is null")
        if (!::imageView.isInitialized) Log.e("Page3Activity", "imageView is null")
        if (!::textView.isInitialized) Log.e("Page3Activity", "textView is null")
        if (!::pot.isInitialized) Log.e("Page3Activity", "imageView3 is null")
        if (!::speech_bubble.isInitialized) Log.e("Page3Activity", "imageView2 is null")
        if (!::btnUploadFile.isInitialized) Log.e("Page3Activity", "btnUploadFile is null")
    }

    private fun handlePeopleCount() {
        val peopleCount = etPeopleCount.text.toString()
        if (peopleCount.isNotEmpty()) {
            // 인원 수 처리 로직 추가
            tvPeople.text = "입력된 인원 수: $peopleCount"
        } else {
            tvPeople.text = "인원 수를 입력해주세요."
        }
    }

    private fun openFileChooser() { //파일 선택 tvSelectedFile 클릭 이벤트
        val intent = Intent(Intent.ACTION_GET_CONTENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
        }
        startActivityForResult(intent, REQUEST_CODE_SELECT_FILE)
    }

    // "분석 중" 상태를 표시하는 함수
    private fun showLoadingIndicator() {
        // 애니메이션 시작
        (loadingIndicator.indeterminateDrawable as? AnimationDrawable)?.start()

        loadingIndicator.visibility = View.VISIBLE
    }

    // "분석 중" 상태를 숨기는 함수
    public fun hideLoadingIndicator() {
        // 애니메이션 멈춤
        (loadingIndicator.indeterminateDrawable as? AnimationDrawable)?.stop()

        loadingIndicator.visibility = View.GONE
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_SELECT_FILE && resultCode == Activity.RESULT_OK) {
            selectedFileUri = data?.data
            tvSelectedFile.text = selectedFileUri?.path ?: "파일 선택 실패"
        }
    }

    //업로드 버튼 클릭시 파일과 인원수 뷰모델에 저장 후 서버에 저장
    private fun uploadFileAndPeopleCount() {
        val peopleCount = etPeopleCount.text.toString().toIntOrNull() ?: 0
        val fileUri = tvSelectedFile.text.toString()
        val userId = sharedPreferences.getString("Session_ID", "") ?: ""

        viewModel.setHeadCountAndFile(peopleCount, fileUri)

        selectedFileUri?.let { uri ->
            viewModel.uploadFile(uri, userId, peopleCount) { result ->
                if (result >= 0) {
                    analyzeFile(result)
                } else {
                    handleUploadError(result)
                    hideLoadingIndicator()
                }
            }
        } ?: run {
            tvSelectedFile.text = "파일을 선택해주세요."
            hideLoadingIndicator()
        }
    }

    private fun analyzeFile(crNum: Int) {
        viewModel.requestBasicPythonAnalysis(crNum) { result ->
            if (result >= 0) {
                hideLoadingIndicator()
                startActivity(Intent(this, Page2Activity::class.java))
            } else {
                handleUploadError(result)
                hideLoadingIndicator()
            }
        }
    }
    private fun handleUploadError(errorCode: Int) {
        when (errorCode) {
            -1 -> Log.e("fetchChatRooms", "업로드 실패")
            -2 -> Log.e("fetchChatRooms", "네트워크 오류")
            -3 -> Log.e("fetchChatRooms", "경로 오류")
            -4 -> Log.e("fetchChatRooms", "분석 실패")
            else -> Log.e("fetchChatRooms", "알 수 없는 오류")
        }
    }

}