package com.example.talkssogi

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class fragmentPage8 : Fragment() {

    private val viewModel: ActivityAnalysisViewModel by viewModels()
    private var crnum: Int = -1

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
        val view = inflater.inflate(R.layout.fragment_page8, container, false)

        // UI 요소 초기화
        val btnBack: ImageView = view.findViewById(R.id.imageView) // 뒤로가기
        val imageView1: ImageView = view.findViewById(R.id.imageView)
        val imageView2: ImageView = view.findViewById(R.id.imgResult)
        val textView: TextView = view.findViewById(R.id.txtViewResult)
        val imageView3: ImageView = view.findViewById(R.id.BtnGotoPage9)

        // 뒤로가기 버튼 클릭 리스너
        btnBack.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        // 프래그먼트9로 이동할 버튼 클릭 리스너
        imageView3.setOnClickListener {
            (requireActivity() as FragmentActivity).replaceFragment(fragmentPage9())
        }

        // API 호출 및 결과 가공 후 TextView에 출력
        lifecycleScope.launch {
            try {
                // API 호출
                val results = viewModel.fetchActivityAnalysis(crnum)

                // 결과 가공
                val displayText = buildString {
                    results.forEach { (key, value) ->
                        append("$key:\n")
                        value.forEach { item ->
                            append("- $item\n")
                        }
                        append("\n")
                    }
                }

                // 결과 출력
                textView.text = displayText

            } catch (e: Exception) {
                textView.text = "데이터를 가져오는 데 실패했습니다."
            }
        }

        return view
    }
}
