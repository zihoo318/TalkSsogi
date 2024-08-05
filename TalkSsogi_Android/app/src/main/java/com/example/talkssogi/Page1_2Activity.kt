package com.example.talkssogi

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

class Page1_2Activity : AppCompatActivity() {

    private val viewModel: MyViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(MyViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page1_2_activity) // activity_page1_2.xml 레이아웃을 설정

        // XML 레이아웃에서 View 객체를 찾는다
        val etID: EditText = findViewById(R.id.etID)
        val idConfirm: TextView = findViewById(R.id.IDConfirm)
        val idConfirm2: TextView = findViewById(R.id.IDConfirm2)
        val btnCheckRedundancy: ImageButton = findViewById(R.id.btnCheckRedundancy)
        val btnSignUp: ImageButton = findViewById(R.id.btnSignUp)

        // 중복 확인 버튼 클릭 시 실행되는 리스너 설정
        btnCheckRedundancy.setOnClickListener {
            val inputID = etID.text.toString()
            if (inputID.isNotEmpty()) {
                viewModel.checkUserIdExists(inputID).observe(this, Observer { exists ->
                    if (exists) {
                        // 입력된 ID가 사용 중인 ID 목록에 포함되어 있는 경우
                        idConfirm2.visibility = TextView.VISIBLE // ID가 사용 중임을 나타내는 메시지 표시
                        idConfirm.visibility = TextView.GONE // ID가 사용 가능한 경우의 메시지 숨김
                    } else {
                        // 입력된 ID가 사용 중인 ID 목록에 포함되어 있지 않은 경우
                        idConfirm.visibility = TextView.VISIBLE // ID가 사용 가능한 경우의 메시지 표시
                        idConfirm2.visibility = TextView.GONE // ID가 사용 중임을 나타내는 메시지 숨김
                    }
                })
            }
        }

        // 회원가입 버튼 클릭 시 실행되는 리스너 설정
        btnSignUp.setOnClickListener {
            val newID = etID.text.toString()
            if (newID.isNotEmpty()) {
                viewModel.checkUserIdExists(newID).observe(this, Observer { exists ->
                    if (!exists) {
                        viewModel.registerUserId(newID) { success ->
                            if (success) {
                                Toast.makeText(this, "회원가입 성공", Toast.LENGTH_SHORT).show()
                                viewModel.saveUserIdToSharedPreferences(newID)
                                viewModel.navigateToNextPage(this, Page2Activity::class.java)
                            } else {
                                Toast.makeText(this, "회원가입 실패", Toast.LENGTH_SHORT).show()
                            }
                        }
                    } else {
                        Toast.makeText(this, "아이디가 이미 사용 중입니다.", Toast.LENGTH_SHORT).show()
                    }
                })
            }
        }
    }
}

    /*private fun goToNextActivity(userId: String) {
    // 다음 화면으로 이동하는 Intent 생성
    val intent = Intent(this, Page2Activity::class.java) // Page2로 이동
    startActivity(intent)
    // 현재 액티비티 종료
    finish()
}*/