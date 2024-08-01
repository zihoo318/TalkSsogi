package com.example.talkssogi

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.talkssogi.databinding.FragmentPage7SearchBinding
import android.widget.SearchView

class fragmentPage7_search : Fragment() {

    private var _binding: FragmentPage7SearchBinding? = null
    // 클래스 멤버 변수로 crnum 선언 및 초기화
    private var crnum: Int = -1 // 기본값을 -1로 설정
    private val binding get() = _binding!!
    private val rankingViewModel: RankingViewModel by viewModels()
    private lateinit var sharedPreferences: SharedPreferences // Intent를 위한 유저 아이디


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ViewBinding을 사용하여 레이아웃을 인플레이트
        // arguments로부터 crnum 값을 가져오고, null일 경우 기본값 -1 사용
        crnum = arguments?.getInt("crnum") ?: -1
        _binding = FragmentPage7SearchBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initSearchView()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // SearchView 설정
    private fun initSearchView() {
        val searchView = binding.search
        val searchTitleSecond = binding.searchTitleSecond

        // 기본적으로 검색창을 열어놓음
        searchView.isIconified = false

        // SharedPreferences에서 사용자 아이디를 가져오기 위해 초기화
        sharedPreferences = requireContext().getSharedPreferences("Session_ID", Context.MODE_PRIVATE)

        // SharedPreferences에서 저장된 사용자 토큰(아이디) 가져오기, "Unknown"은 key에 맞는 value가 없을 때 가져오는 값(기본값)
        val userId = sharedPreferences.getString("Session_ID", "Unknown") // "Session_ID"에서 "userToken"으로 키 수정

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { // 메서드 시그니처 수정
                query?.let { keyword ->
                    // 검색 버튼을 누르면 clearFocus() 호출하고, TextView 업데이트 및 데이터 로드
                    searchView.clearFocus()
                    searchTitleSecond.text = keyword

                    if (userId != null && userId != "Unknown") { // userId가 유효한 경우에만 API 호출
                        rankingViewModel.fetchSearchRankingResults(crnum, keyword)
                    }
                }
                return true // true를 반환하여 이벤트를 처리했음을 나타냄
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                // 검색 텍스트가 변경될 때의 행동 (옵션)
                return true
            }
        })

        // SearchView에 포커스가 가면 검색창이 뜨게 설정
        searchView.setOnQueryTextFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                searchView.isIconified = false
            }
        }
    }

    private fun observeViewModel() {
        rankingViewModel.searchRankingResults.observe(viewLifecycleOwner, Observer { results ->
            // 결과가 비어있다면
            if (results.isEmpty()) {
                binding.rankingResult.text = "No results found"
            } else {
                val rankingList = results.values.flatten()
                val displayText = rankingList.joinToString(separator = "\n") { name -> "순위: $name" }
                binding.rankingResult.text = displayText
            }
        })

        rankingViewModel.error.observe(viewLifecycleOwner, Observer { error ->
            error?.let {
                Log.e("fragmentPage7Search", it)
            }
        })
    }
}
