package com.example.talkssogi

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

class MainActivity : AppCompatActivity() {

    private lateinit var sharedPreferences: SharedPreferences // 사용자 아이디를 위한 정적변수
    private val viewModel: MyViewModel by lazy {
        (application as MyApplication).viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // SharedPreferences에서 사용자 아이디를 가져온다
        sharedPreferences = getSharedPreferences("Session_ID", Context.MODE_PRIVATE)

        // 사용자 아이디가 이미 저장되어 있으면 Page2Activity로 이동한다
        val userToken = sharedPreferences.getString("Session_ID", null)
        if (userToken != null && userToken.isNotEmpty()) {
            val intent = Intent(this, Page2Activity::class.java)
            intent.putExtra("userId", userToken)
            startActivity(intent)
            finish() // MainActivity 종료
        } else {
            // 사용자 아이디가 저장되어 있지 않으면 Page1Activity로 이동한다
            val intent = Intent(this, Page1Activity::class.java)
            startActivity(intent)
            finish() // MainActivity 종료
        }
    }

//        // Page2Activity로 이동하는 버튼 설정
//        val buttonOpenPage2 = findViewById<Button>(R.id.button_open_page2)
//        buttonOpenPage2.setOnClickListener {
//            val intent = Intent(this, Page2Activity::class.java)
//            startActivity(intent)
//        }
//
//        ////////////page5로 가기/////////
//        val buttonOpenPage5 = findViewById<Button>(R.id.button_open_page5)
//        buttonOpenPage5.setOnClickListener {
//            val intent = Intent(this, FragmentActivity::class.java)
//            startActivity(intent)
//        }
//        // 테스트를 위해 본인이 필요한 곳으로 가는 버튼을 만들어서 실행해보기!!!
//
//        // Page1Activity로 이동하는 버튼 설정
//        val buttonOpenPage1 = findViewById<Button>(R.id.button_open_page1)
//        buttonOpenPage1.setOnClickListener {
//            val intent = Intent(this, Page1Activity::class.java)
//            startActivity(intent)
//        }
//
//        // Page3Activity로 이동하는 버튼 설정
//        val buttonOpenPage3 = findViewById<Button>(R.id.button_open_page3)
//        buttonOpenPage3.setOnClickListener {
//            val intent = Intent(this, Page3Activity::class.java)
//            startActivity(intent)
//        }

}