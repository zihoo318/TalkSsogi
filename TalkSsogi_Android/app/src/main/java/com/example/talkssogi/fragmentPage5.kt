package com.example.talkssogi

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.app.fragmentPage10Result
import fragmentPage10

class fragmentPage5 : Fragment() {
    private var crnum: Int = -1 // 채팅방 번호를 저장할 변수
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.page5_fragment_activity, container, false)

        // arguments에서 채팅방 번호를 가져옴
        crnum = arguments?.getInt("crnum", -1) ?: -1

        val btnBack: ImageView = view.findViewById(R.id.imageView) //뒤로가기
        val option1 = view.findViewById<TextView>(R.id.option_1) //페이지6
        val option2 = view.findViewById<TextView>(R.id.option_2) //페이지7
        val option3 = view.findViewById<TextView>(R.id.option_3) //페이지10
        val option4 = view.findViewById<TextView>(R.id.option_4) //페이지8

        // 뒤로가기 이미지 리스너
        btnBack.setOnClickListener {
            // 액티비티2로 이동
            val intent = Intent(requireContext(), Page2Activity::class.java)
            startActivity(intent)
        }

        option1.setOnClickListener {
            // 채팅방 번호를 arguments로 설정하여 다음 프래그먼트로 전달
            val fragment = fragmentPage6().apply {
                arguments = Bundle().apply {
                    putInt("crnum", crnum)
                }
            }
            (requireActivity() as FragmentActivity).replaceFragment(fragment)
        }

        option2.setOnClickListener {
            // 채팅방 번호를 Intent로 전달
            val intent = Intent(requireContext(), Page7Activity::class.java).apply {
                putExtra("crnum", crnum)
            }
            startActivity(intent)
        }

        option3.setOnClickListener {
            // 채팅방 번호를 arguments로 설정하여 다음 프래그먼트로 전달
            val fragment = fragmentPage10().apply {
                arguments = Bundle().apply {
                    putInt("crnum", crnum)
                }
            }
            (requireActivity() as FragmentActivity).replaceFragment(fragment)
        }


        option4.setOnClickListener {
            // 채팅방 번호를 arguments로 설정하여 다음 프래그먼트로 전달
            val fragment = fragmentPage8().apply {
                arguments = Bundle().apply {
                    putInt("crnum", crnum)
                }
            }
            (requireActivity() as FragmentActivity).replaceFragment(fragment)
        }

        return view
    }
}