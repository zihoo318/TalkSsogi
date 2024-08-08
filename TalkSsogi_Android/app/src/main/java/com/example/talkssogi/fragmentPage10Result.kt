package com.example.app

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.talkssogi.R

class fragmentPage10Result : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_page10_result, container, false)
        val resultTextView = view.findViewById<TextView>(R.id.SenderResult)

        val result = arguments?.getString("result") ?: "No Result"
        resultTextView.text = result

        return view
    }
}
