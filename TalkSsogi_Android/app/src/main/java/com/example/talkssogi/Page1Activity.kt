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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

class Page1Activity : AppCompatActivity() {

    private val viewModel: MyViewModel by lazy {
        ViewModelProvider(
            this,
            ViewModelProvider.AndroidViewModelFactory.getInstance(application)
        ).get(MyViewModel::class.java)
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
        val btnMoveToSignUpPage: ImageButton =
            findViewById(R.id.btnMoveToSignUpPage) // 회원가입 페이지로 이동하는 버튼 추가

        // Check ID existence on text change
        // EditText에 입력된 텍스트의 변화를 감지하는 TextWatcher 추가
        etID.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // 입력된 ID의 중복 여부를 확인하는 로직은 뷰모델의 LiveData를 통해 처리하도록 변경
                val inputID = s.toString()
                if (inputID.isNotEmpty()) {
                    viewModel.checkUserIdExists(inputID)
                        .observe(this@Page1Activity, Observer { exists ->
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
                } else {
                    // 입력 필드가 비어있는 경우
                    idConfirm.visibility = TextView.GONE // ID가 사용 가능한 경우의 메시지 숨김
                    idConfirm2.visibility = TextView.GONE // ID가 사용 중임을 나타내는 메시지 숨김
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        btnUploadName.setOnClickListener {
            val inputID = etID.text.toString()
            if (inputID.isNotEmpty()) {
                viewModel.loginUserId(inputID) { success ->
                    if (success) {
                        viewModel.saveUserIdToSharedPreferences(inputID)
                        viewModel.navigateToNextPage(this, Page2Activity::class.java)
                    } else {
                        Toast.makeText(this, "잘못된 아이디입니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }

        btnMoveToSignUpPage.setOnClickListener {
            startActivity(Intent(this, Page1_2Activity::class.java))
        }
    }
}

        /*// 확인 버튼 클릭 시 실행되는 리스너 설정
        btnUploadName.setOnClickListener {
            val inputID = etID.text.toString()
            if (inputID.isNotEmpty()) {
                viewModel.checkUserIdExists(inputID)
                    .observe(this@Page1Activity, Observer { exists ->
                        if (exists) {
                            viewModel.saveUserIdToSharedPreferences(inputID)
                            viewModel.navigateToNextPage(this, Page2Activity::class.java)
                        } else {
                            Toast.makeText(this, "잘못된 아이디입니다.", Toast.LENGTH_SHORT).show()
                        }
                    })
            }
        }

        // 회원가입 버튼 클릭 시 실행되는 리스너 설정
        btnMoveToSignUpPage.setOnClickListener {
            startActivity(Intent(this, Page1_2Activity::class.java))
        }

        // 기타 초기화 작업
        imageView.setImageResource(R.drawable.happy) // 이미지 뷰에 smile2 이미지 설정
    }
}*/

    /*private fun goToNextActivity(userId: String) {
        // 다음 화면으로 이동하는 Intent 생성
        val intent = Intent(this, Page2Activity::class.java)  //페이지2로 가기
        startActivity(intent)
        // 현재 액티비티 종료
        finish()
    }*/

/*// ViewModel에서 사용자 ID 리스트를 관찰
viewModel.userIds.observe(this, Observer { userIds ->
    // EditText에 입력된 텍스트의 변화를 감지하는 TextWatcher 추가
    // EditText에 입력된 텍스트의 변화를 감지하는 TextWatcher 추가
    etID.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            val inputID = s.toString()
            if (inputID.isNotEmpty()) {
                viewModel.userIds.value?.let { userIds ->
                    if (userIds.contains(inputID)) {
                        // 입력된 ID가 사용 중인 ID 목록에 포함되어 있는 경우
                        idConfirm2.visibility = TextView.VISIBLE // ID가 사용 중임을 나타내는 메시지 표시
                        idConfirm.visibility = TextView.GONE // ID가 사용 가능한 경우의 메시지 숨김
                    } else {
                        // 입력된 ID가 사용 중인 ID 목록에 포함되어 있지 않은 경우
                        idConfirm.visibility = TextView.VISIBLE // ID가 사용 가능한 경우의 메시지 표시
                        idConfirm2.visibility = TextView.GONE // ID가 사용 중임을 나타내는 메시지 숨김
                    }
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

    // ViewModel에서 사용자 ID 리스트를 관찰
    viewModel.userIds.observe(this, Observer { userIds ->
        // 사용자 ID 리스트가 갱신될 때마다 EditText의 텍스트를 확인하여 메시지 업데이트
        etID.text?.let { etID.text = it }
    })

    btnUploadName.setOnClickListener {
        val newID = etID.text.toString()
        if (newID.isNotEmpty()) {
            viewModel.checkUserId(newID).observe(this, Observer { response ->
                if (response == "사용 가능한 아이디입니다") {
                    viewModel.registerUser(newID).observe(this, Observer { response ->
                        if (response.isSuccessful) {
                            // 응답 본문을 사용
                            val responseBody = response.body()
                            // 응답 본문이 Map<String, Any> 형태라고 가정하고 처리
                            if (responseBody != null && responseBody["status"] == "success") {
                                // Shared Preferences에 사용자 아이디 저장
                                val sharedPreferences = getSharedPreferences("Session_ID", Context.MODE_PRIVATE)
                                sharedPreferences.edit().putString("Session_ID", newID).apply()

                                // 다음 화면으로 이동
                                goToNextActivity(newID)
                            } else {
                                // 사용자 등록 실패 처리
                                // 예를 들어, Toast 메시지로 실패 알림
                                Toast.makeText(this, "사용자 등록에 실패했습니다", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            // 사용자 등록 실패 처리
                            // 예를 들어, Toast 메시지로 실패 알림
                            Toast.makeText(this, "사용자 등록에 실패했습니다", Toast.LENGTH_SHORT).show()
                        }
                    })
                } else {
                    // 아이디가 이미 사용 중인 경우
                    // 예를 들어, Toast 메시지로 알림
                    Toast.makeText(this, "아이디가 이미 사용 중입니다", Toast.LENGTH_SHORT).show()
                }
            })
        }
    }

    // 기타 초기화 작업
    imageView.setImageResource(R.drawable.happy) // 이미지 뷰에 smile2 이미지 설정
})
}
private fun goToNextActivity(userId: String) {
// 다음 화면으로 이동하는 Intent 생성
val intent = Intent(this, Page2Activity::class.java)  // 페이지2로 가기
intent.putExtra("USER_ID", userId) // 사용자 아이디를 Intent에 추가
startActivity(intent)
// 현재 액티비티 종료
finish()
}
}*/