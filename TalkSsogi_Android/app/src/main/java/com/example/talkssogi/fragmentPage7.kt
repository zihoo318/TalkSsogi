package com.example.talkssogi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.widget.ViewPager2
import com.yourpackage.ui.main.ViewPagerAdapter
import me.relex.circleindicator.CircleIndicator3

class fragmentPage7 : Fragment() {

    private lateinit var viewPager2: ViewPager2
    private lateinit var indicator: CircleIndicator3
    private lateinit var textView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_page7, container, false)

        viewPager2 = view.findViewById(R.id.view_pager)
        indicator = view.findViewById(R.id.ranking_indicator)
        textView = view.findViewById(R.id.ranking_title)

        // Intent로부터 crnum 값 받기
        val crnum = activity?.intent?.getIntExtra("crnum", -1) ?: -1

        // ViewPager2 어댑터 설정
        val adapter = ViewPagerAdapter(requireActivity(), crnum)
        viewPager2.adapter = adapter

        // CircleIndicator3 설정
        indicator.setViewPager(viewPager2)

        // 처음 페이지 제목 업데이트(앱 실행 시 첫 페이지 제목이 짤리는 현상 해결)
        updateTextView(0)

        // 이전 버튼 클릭 리스너 설정
        view.findViewById<ImageButton>(R.id.button_previous).setOnClickListener {
            val previousItem = if (viewPager2.currentItem - 1 >= 0) viewPager2.currentItem - 1 else (viewPager2.adapter?.itemCount ?: 0) - 1
            viewPager2.setCurrentItem(previousItem, true)
        }

        // 다음 버튼 클릭 리스너 설정
        view.findViewById<ImageButton>(R.id.button_next).setOnClickListener {
            val nextItem = if (viewPager2.currentItem + 1 < viewPager2.adapter?.itemCount ?: 0) viewPager2.currentItem + 1 else 0
            viewPager2.setCurrentItem(nextItem, true)
        }

        // ViewPager2 페이지 변경 콜백 설정
        viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                // 페이지가 변경될 때마다 TextView 내용 변경
                updateTextView(position)
            }
        })

        return view
    }

    private fun updateTextView(position: Int) {
        // 페이지 번호에 따라 제목 업데이트
        when (position) {
            0 -> textView.text = "가장 많이 메시지를 보낸 사람"
            1 -> textView.text = "가장 많은 사진을 보낸 사람"
            2 -> textView.text = "가장 많이 이모티콘을 사용한 사람"
            3 -> textView.text = "가장 긴 메시지를 보낸 사람"
            4 -> textView.text = "가장 많이 오타 내는 사람"
            5 -> textView.text = "가장 많이 태그된 사람"
            6 -> textView.text = "가장 많이 초성을 사용한 사람"
            7 -> textView.text = "가장 다른 사람을 많이 언급한 사람"
            8 -> textView.text = "가장 많이 메시지를 삭제한 사람"
            // Add more cases as needed
        }
    }
}
