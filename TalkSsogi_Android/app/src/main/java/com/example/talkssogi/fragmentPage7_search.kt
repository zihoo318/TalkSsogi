package com.example.talkssogi

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.talkssogi.databinding.FragmentPage7SearchBinding
import android.widget.SearchView
import androidx.appcompat.content.res.AppCompatResources.getDrawable
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class fragmentPage7_search : Fragment() {
    private var _binding: FragmentPage7SearchBinding? = null
    // 클래스 멤버 변수로 crnum 선언 및 초기화
    private var crnum: Int = -1 // 기본값을 -1로 설정
    private val binding get() = _binding!!
    private val rankingViewModel: RankingViewModel by viewModels()
    private lateinit var sharedPreferences: SharedPreferences // Intent를 위한 유저 아이디
    private lateinit var loadingIndicator: ProgressBar // 분석 중 띄우는 바



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ViewBinding을 사용하여 레이아웃을 인플레이트
        _binding = FragmentPage7SearchBinding.inflate(inflater, container, false)
        loadingIndicator = binding.loadingIndicator // 분석 중 띄우는 바(띄워져 있는데 안 보이게 해둠)

        // AnimationDrawable 설정
        val animationDrawable = context?.let { getDrawable(it, R.drawable.animation_loding) } as? AnimationDrawable
        loadingIndicator.indeterminateDrawable = animationDrawable
        return binding.root

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        // arguments로부터 crnum 값을 가져오고, null일 경우 기본값 -1 사용
        crnum = arguments?.getInt("crnum") ?: -1
        Log.d("searchranking", "arguments에 있는 crnum: ${arguments}")
        Log.d("searchranking", "searchranking의 crnum 값: $crnum") // crnum 값 로그 출력

        initSearchView()
        observeViewModel()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    // "분석 중" 상태를 표시하는 함수
    private fun showLoadingIndicator() {
        //loadingindicator가 작동하는지 확인하는 로그
        Log.d("fragmentPage7Search", "LoadingIndicator 작동")
        // 애니메이션 시작
        (loadingIndicator.indeterminateDrawable as? AnimationDrawable)?.start()
        loadingIndicator.visibility = View.VISIBLE
    }

    // "분석 중" 상태를 숨기는 함수
    public fun hideLoadingIndicator() {
        //loadingindicator가 꺼지는지 확인하는 로그
        Log.d("fragmentPage7Search", "LoadingIndicator 꺼짐")
        // 애니메이션 멈춤
        (loadingIndicator.indeterminateDrawable as? AnimationDrawable)?.stop()
        loadingIndicator.visibility = View.GONE
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
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { keyword ->
                    // 검색 버튼을 누르면 clearFocus() 호출하고, TextView 업데이트 및 데이터 로드
                    searchView.clearFocus()
                    searchTitleSecond.text = keyword
                    showLoadingIndicator()

                    if (userId != null && userId != "Unknown") {
                        // 데이터 가져오기 요청을 코루틴 내에서 호출
                        viewLifecycleOwner.lifecycleScope.launch {
                            rankingViewModel.fetchSearchRankingResults(crnum, keyword)
                        }
                    } else {
                        hideLoadingIndicator()
                    }
                }
                return true
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
            hideLoadingIndicator()
            if (results != null) {
                val messageRankings = results["검색어"]
                messageRankings?.let {
                    val displayText = it.entries
                        .sortedByDescending { entry -> entry.value.toInt() }
                        .mapIndexed { index, entry -> "${index + 1}등: ${entry.key}  ${entry.value}개" }
                        .joinToString(separator = "\n")
                    binding.rankingResult.text = displayText
                } ?: run {
                    binding.rankingResult.text = "해당 검색어를 보낸 사람이 없습니다."
                }
            } else {
                binding.rankingResult.text = "해당 검색어를 보낸 사람이 없습니다."
            }
        })

        rankingViewModel.error.observe(viewLifecycleOwner, Observer { error ->
            hideLoadingIndicator()
            error?.let {
                Log.e("fragmentPage7Search", it)
            }
        })
    }


    companion object {
        private const val ARG_CRNUM = "crnum"

        fun newInstance(crnum: Int): fragmentPage7_search {
            val fragment = fragmentPage7_search()
            val args = Bundle()
            args.putInt(ARG_CRNUM, crnum)
            fragment.arguments = args
            return fragment
        }
    }
}
