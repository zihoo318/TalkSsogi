package com.example.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.talkssogi.R


// 검색 결과를 표시하는 프래그먼트
class fragmentPage10Result : Fragment() {

    // 결과를 표시할 텍스트뷰 선언
    private lateinit var senderResult: TextView

    // 프래그먼트 레이아웃을 인플레이트
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_page10_result, container, false)
    }

    // 뷰가 생성된 후 호출됨
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 텍스트뷰를 레이아웃에서 찾아서 초기화
        senderResult = view.findViewById(R.id.SenderResult)

        // 번들로부터 검색 쿼리를 받아와서 텍스트뷰에 표시
        val query = arguments?.getString("query")
        senderResult.text = "Search result for \"$query\""
    }
}
