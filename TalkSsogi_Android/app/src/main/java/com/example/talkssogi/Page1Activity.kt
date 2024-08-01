package com.example.talkssogi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider

class Page1Activity : AppCompatActivity() {

    private val viewModel: MyViewModel by lazy {
        ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory.getInstance(application)).get(MyViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page1_activity) // activity_page1.xml 레이아웃을 설정

        // XML 레이아웃에서 View 객체를 찾는다
        val imageView: ImageView = findViewById(R.id.imageView)
        val etID: EditText = findViewById(R.id.etID)
        val idConfirm: TextView = findViewById(R.id.IDConfirm)
        val idConfirm2: TextView = findViewById(R.id.IDConfirm2)
        val btnUploadName: ImageButton = findViewById(R.id.btnUploadName)

        // EditText에 입력된 텍스트의 변화를 감지하는 TextWatcher 추가
        etID.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // 입력된 ID의 중복 여부를 확인하는 로직은 뷰모델의 LiveData를 통해 처리하도록 변경
                val inputID = s.toString()
                val userIdsLiveData = viewModel.getUserIdsLiveData()
                if (inputID.isNotEmpty()) {
                    if (userIdsLiveData.value?.contains(inputID) == true) {
                        // 입력된 ID가 사용 중인 ID 목록에 포함되어 있는 경우
                        idConfirm2.visibility = TextView.VISIBLE // ID가 사용 중임을 나타내는 메시지 표시
                        idConfirm.visibility = TextView.GONE // ID가 사용 가능한 경우의 메시지 숨김
                    } else {
                        // 입력된 ID가 사용 중인 ID 목록에 포함되어 있지 않은 경우
                        idConfirm.visibility = TextView.VISIBLE // ID가 사용 가능한 경우의 메시지 표시
                        idConfirm2.visibility = TextView.GONE // ID가 사용 중임을 나타내는 메시지 숨김
                    }
                } else {
                    // 입력 필드가 비어있는 경우
                    idConfirm.visibility = TextView.GONE // ID가 사용 가능한 경우의 메시지 숨김
                    idConfirm2.visibility = TextView.GONE // ID가 사용 중임을 나타내는 메시지 숨김
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 확인 버튼 클릭 시 실행되는 리스너 설정
        btnUploadName.setOnClickListener {
            val newID = etID.text.toString()
            if (newID.isNotEmpty() && !(viewModel.userIds.value?.contains(newID) == true)) {
                // 입력된 ID가 빈 값이 아니고, usedIDs 리스트에 포함되어 있지 않은 경우
                viewModel.addUserId(newID) // ID를 usedIDs 리스트에 추가
                etID.text.clear() // EditText의 텍스트를 비움
                idConfirm.visibility = TextView.GONE // ID가 사용 가능한 경우의 메시지를 숨김
                idConfirm2.visibility = TextView.GONE // ID가 사용 중임을 나타내는 메시지를 숨김

                // Shared Preferences에 사용자 아이디 저장
                // (메인액티비티에서 만든 변수 안에 값 넣기 -> 다음 앱 접속 땐 값이 있어서 바로 페이지2로 이동)
                val sharedPreferences = getSharedPreferences("Session_ID", Context.MODE_PRIVATE)
                sharedPreferences.edit().putString("Session_ID", newID)
                    .apply() // "Session_ID" 키에 newID 저장

                // 서버에 사용자 아이디 전송
                viewModel.sendUserId(newID)

                // 다음 화면으로 이동
                goToNextActivity(newID)
            }
        }

        // 기타 초기화 작업
        imageView.setImageResource(R.drawable.happy) // 이미지 뷰에 smile2 이미지 설정
    }

    private fun goToNextActivity(userId: String) {
        // 다음 화면으로 이동하는 Intent 생성
        val intent = Intent(this, Page2Activity::class.java)  //페이지2로 가기
        startActivity(intent)
        // 현재 액티비티 종료
        finish()
    }
}
