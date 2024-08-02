package com.example.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.talkssogi.ActivityAnalysisViewModel
import com.example.talkssogi.R
import androidx.lifecycle.Observer

class fragmentPage10Result : Fragment() {

    private lateinit var senderResult: TextView
    private val viewModel: ActivityAnalysisViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_page10_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        senderResult = view.findViewById(R.id.SenderResult)

        // ViewModel의 LiveData를 관찰하여 결과를 업데이트합니다
        viewModel.predictionResult.observe(viewLifecycleOwner, Observer { result ->
            senderResult.text = "Predicted sender: \"$result\""
        })

        // 여기에서 ViewModel의 predictSender 메서드를 호출하여 예측을 요청할 수 있습니다
        // 예를 들어, 초기화나 특정 이벤트에서 호출할 수 있습니다.
        val query = "some query" // 실제 쿼리로 변경해야 합니다
        viewModel.predictSender(query)
    }

}
