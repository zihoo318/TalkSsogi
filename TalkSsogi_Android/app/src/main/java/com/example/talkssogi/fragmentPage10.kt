// fragmentPage10.kt
package com.example.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.talkssogi.ActivityAnalysisViewModel
import com.example.talkssogi.R

class fragmentPage10 : Fragment() {

    private val viewModel: ActivityAnalysisViewModel by viewModels()
    private lateinit var searchView: SearchView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_page10, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        searchView = view.findViewById(R.id.searchView)

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let {
                    viewModel.predictSender(it)
                }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })

        viewModel.predictionResult.observe(viewLifecycleOwner) { result ->
            // 검색 결과를 표시하는 로직 추가
            performSearch(result)
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { message ->
            // 에러 메시지를 표시하는 로직 추가
            // 예: Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
        }
    }

    private fun performSearch(result: String) {
        val fragment = fragmentPage10Result().apply {
            arguments = Bundle().apply {
                putString("result", result)
            }
        }
        childFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }
}
