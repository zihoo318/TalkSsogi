package com.example.talkssogi

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer

class Rank7_9 : Fragment() {

    // ViewModel 초기화
    private val rankingViewModel: RankingViewModel by viewModels()

    // 클래스 멤버 변수로 crnum 선언 및 초기화
    private var crnum: Int = -1 // 기본값을 -1로 설정

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.rank7_board, container, false)

        // arguments로부터 crnum 값을 가져오고, null일 경우 기본값 -1 사용
        crnum = arguments?.getInt("crnum") ?: -1

        // UI 요소 초기화
        val search = view.findViewById<ImageView>(R.id.button_search)
        val ranking_result = view.findViewById<TextView>(R.id.ranking_result)

        // 검색 버튼 클릭 시 동작
        search.setOnClickListener {
            val intent = Intent(requireContext(), Page7_search_Activity::class.java)
            startActivity(intent)
        }

        // ViewModel 데이터 관찰
        rankingViewModel.basicRankingResults.observe(viewLifecycleOwner, Observer { results ->
            // "주제9"의 랭킹을 가져와 표시
            val rankingList = results["주제9"]
            rankingList?.let {
                val displayText = it.joinToString(separator = "\n") { name -> "순위: $name" }
                ranking_result.text = displayText
            }
        })

        // 데이터 가져오기 요청
        rankingViewModel.fetchBasicRankingResults(crnum)

        return view
    }
}
