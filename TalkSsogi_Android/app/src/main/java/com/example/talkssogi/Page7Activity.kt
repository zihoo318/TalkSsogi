package com.example.talkssogi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class Page7Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page7)

        val crnum = intent.getIntExtra("crnum", -1) //intent로 채팅방 번호 받음

        if (savedInstanceState == null) {
            val fragment = fragmentPage7()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }
}
