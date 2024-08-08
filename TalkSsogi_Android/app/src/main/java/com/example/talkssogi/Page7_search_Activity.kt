package com.example.talkssogi

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity

class Page7_search_Activity : AppCompatActivity() {
    private var crnum: Int = -1 // 기본값 설정
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page7_search)

        // Intent로부터 crnum 값을 받아옴
        crnum = intent.getIntExtra("crnum", -1)
        Log.d("Page7_search_Activity", "Page7_search_Activity crnum 값: $crnum")

        if (savedInstanceState == null) {
            val fragment = fragmentPage7_search.newInstance(crnum)
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }
}
