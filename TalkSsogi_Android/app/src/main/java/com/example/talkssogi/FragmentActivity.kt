package com.example.talkssogi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

// 액티비티에 page5를 기본으로 올리고 버튼을 누르면 다른 페이지의 프래그먼트로 바뀜

class FragmentActivity : AppCompatActivity() {
    private val viewModel: MyViewModel by lazy {
        (application as MyApplication).viewModel
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_fragment_container)

        if (savedInstanceState == null) {
            val crnum = intent.getIntExtra("crnum", -1) // 채팅방 번호를 가져옴
            val fragment = fragmentPage5().apply {
                arguments = Bundle().apply {
                    putInt("crnum", crnum) // 채팅방 번호를 arguments에 추가(fragment버전의 intent같은거)
                }
            }

            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }

    fun replaceFragment(fragment: Fragment) {
        // ViewModel을 프래그먼트에 전달
        if (fragment is fragmentPage9) {
            fragment.viewModel = viewModel
        }
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)  // 옵션 1, 2, 4 클릭 후 뒤로 가기 버튼을 눌렀을 때 이전 상태로 되돌리기 위해 백 스택에 추가
            .commit()
    }
}
