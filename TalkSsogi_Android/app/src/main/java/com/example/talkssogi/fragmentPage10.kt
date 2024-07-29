package com.example.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import com.example.talkssogi.R


// 검색 기능을 처리하는 프래그먼트
class fragmentNewPage10 : Fragment() {

    // 검색 뷰 선언
    private lateinit var searchView: SearchView

    // 프래그먼트 레이아웃을 인플레이트
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_page10, container, false)
    }

    // 뷰가 생성된 후 호출됨
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 검색 뷰를 레이아웃에서 찾아서 초기화
        searchView = view.findViewById(R.id.searchView)

        // 검색 뷰에 리스너 설정
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            // 검색 쿼리를 제출했을 때 호출
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    performSearch(it) // 검색 수행
                }
                return true
            }

            // 검색 쿼리 텍스트가 변경될 때 호출
            override fun onQueryTextChange(newText: String?): Boolean {
                return false // 여기서는 처리하지 않음
            }
        })
    }

    // 검색을 수행하고 결과 프래그먼트를 교체하는 함수
    private fun performSearch(query: String) {
        val fragment = fragmentPage10Result().apply {
            arguments = Bundle().apply {
                putString("query", query) // 검색 쿼리를 번들에 저장
            }
        }
        // FrameLayout을 통해 새로운 프래그먼트로 교체
        childFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null) // 뒤로 가기 버튼을 누를 때 이전 프래그먼트로 돌아가기 위해 백스택에 추가
            .commit()
    }
}
