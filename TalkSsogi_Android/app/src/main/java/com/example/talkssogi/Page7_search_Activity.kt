package com.example.talkssogi

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class Page7_search_Activity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page7_search)

        if (savedInstanceState == null) {
            val fragment = fragmentPage7_search()
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit()
        }
    }
}
