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
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class Rank7_6 : Fragment() {

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
            val intent = Intent(requireContext(), Page7_search_Activity::class.java).apply {
                putExtra("crnum", crnum) // crnum 값을 Intent에 추가
            }
            startActivity(intent)
        }

        // ViewModel 데이터 관찰
        rankingViewModel.basicRankingResults.observe(viewLifecycleOwner, Observer { results ->
            val messageRankings = results["언급된"] // "언급된" 키에 대한 값 가져오기
            messageRankings?.let {
                val displayText = it.entries
                    .sortedByDescending { entry -> entry.value } // 값을 기준으로 정렬
                    .mapIndexed { index, entry -> "${index + 1}등: ${entry.key}  ${entry.value}번" }
                    .joinToString(separator = "\n")
                ranking_result.text = displayText
            }
        })

        // 데이터 가져오기 요청을 코루틴 내에서 호출
        viewLifecycleOwner.lifecycleScope.launch {
            rankingViewModel.fetchBasicRankingResults(crnum)
        }

        return view
    }

    //crnum 받기
    companion object {
        private const val ARG_CRNUM = "crnum"

        fun newInstance(crnum: Int): Rank7_6 {
            val fragment = Rank7_6()
            val args = Bundle()
            args.putInt(ARG_CRNUM, crnum)
            fragment.arguments = args
            return fragment
        }
    }
}

