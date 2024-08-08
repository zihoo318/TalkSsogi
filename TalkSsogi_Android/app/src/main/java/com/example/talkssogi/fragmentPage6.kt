package com.example.talkssogi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

// 이미지 URL 데이터 클래스
//data class ImageURL(
//    val imageUrl: String
//)

class fragmentPage6 : Fragment() {

    private val activityAnalysisViewModel: ActivityAnalysisViewModel by viewModels() // 뷰모델 초기화
    private var crnum: Int = -1 // 채팅방 번호를 저장할 변수

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            crnum = it.getInt("crnum", -1)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_page6, container, false)

        val btnBack: ImageView = view.findViewById(R.id.imageView) //뒤로가기 버튼
        val searchbtn : ImageButton = view.findViewById(R.id.create) //검색 버튼
        val spinner: Spinner = view.findViewById(R.id.spinner)
        val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView_wordclould) //이미지를 표시할 리사이클러뷰
        recyclerView.layoutManager = LinearLayoutManager(context)

        // 뒤로가기 버튼 클릭 리스너
        btnBack.setOnClickListener {
            // 프래그먼트 매니저를 통해 뒤로 가기 동작
            requireActivity().supportFragmentManager.popBackStack()
        }

        // 검색 버튼 클릭 리스너 설정
        searchbtn.setOnClickListener {
            //val selectedItem = spinner.selectedItem.toString().toInt() // String을 Int로 변환
            val selectedItem = spinner.selectedItem.toString()

            // API 호출을 통해 이미지 URL 가져오기
            activityAnalysisViewModel.loadWordCloudImageUrl(crnum, selectedItem)

            // 워드 클라우드 이미지 URL을 LiveData로 관찰하여 RecyclerView 업데이트
            activityAnalysisViewModel.wordCloudImageUrl.observe(viewLifecycleOwner, { wordCloudImageUrl ->
                val adapter = Page9RecyclerViewAdapter(wordCloudImageUrl)
                recyclerView.adapter = adapter
            })
        }

        // 스피너 아이템 설정
        // 뷰모델에서 대화방 참가자 목록 로드 및 스피너 업데이트
        activityAnalysisViewModel.loadParticipants(crnum)
        activityAnalysisViewModel.participants.observe(viewLifecycleOwner, { names ->
            val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, names)
            spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = spinnerAdapter
        })

        // 인플레이트된 뷰를 반환합니다.
        return view
    }
}