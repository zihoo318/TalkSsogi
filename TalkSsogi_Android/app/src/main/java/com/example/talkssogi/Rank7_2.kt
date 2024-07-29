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

class Rank7_2 : Fragment() {

    private val rankingViewModel: RankingViewModel by viewModels()
    private lateinit var sharedPreferences: SharedPreferences //intent를 위한 유저 아이

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.rank7_board, container, false)

        val search = view.findViewById<ImageView>(R.id.button_search)
        val ranking_result = view.findViewById<TextView>(R.id.ranking_result)


        search.setOnClickListener {
            val intent = Intent(requireContext(), Page7_search_Activity::class.java)
            startActivity(intent)
        }
        // SharedPreferences에서 사용자 아이디를 가져오기 위해 초기화
        sharedPreferences = requireContext().getSharedPreferences("Session_ID", Context.MODE_PRIVATE)

        // SharedPreferences에서 저장된 사용자 토큰(아이디) 가져오기, "Unknown"은 key에 맞는 value가 없을 때 가져오는 값(기본값)
        val userId = sharedPreferences.getString("Session_ID", "Unknown") ?: "Unknown"

        // ViewModel 데이터 관찰
        rankingViewModel.basicRankingResults.observe(viewLifecycleOwner, Observer { results ->
            // "주제2"의 랭킹을 가져와 표시
            val rankingList = results["주제2"]
            rankingList?.let {
                val displayText = it.joinToString(separator = "\n") { name -> "순위: $name" }
                ranking_result.text = displayText
            }
        })

        // 데이터 가져오기 요청
        rankingViewModel.fetchBasicRankingResults(userId)

        return view
    }
}
