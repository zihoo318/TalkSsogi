package com.example.app

import android.content.Context
import android.content.SharedPreferences
import android.graphics.drawable.AnimationDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import android.widget.SearchView
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.talkssogi.ApiService
import com.example.talkssogi.CallerPredictionViewModel
import com.example.talkssogi.CallerPredictionViewModelFactory
import com.example.talkssogi.Constants
import com.example.talkssogi.R
import com.example.talkssogi.databinding.FragmentPage10Binding
import com.google.gson.GsonBuilder
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.util.concurrent.TimeUnit


class fragmentPage10 : Fragment() {
    private var _binding: FragmentPage10Binding? = null
    private val binding get() = _binding!!
    private var crnum: Int = -1 // 기본값을 -1로 설정
    private lateinit var callerPredictionViewModel: CallerPredictionViewModel
    private lateinit var sharedPreferences: SharedPreferences // Intent를 위한 유저 아이디
    private lateinit var loadingIndicator: ProgressBar // 분석 중 띄우는 바

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ViewBinding을 사용하여 레이아웃을 인플레이트
        _binding = FragmentPage10Binding.inflate(inflater, container, false)
        loadingIndicator = binding.loadingIndicator // 분석 중 띄우는 바(띄워져 있는데 안 보이게 해둠)

        // AnimationDrawable 설정
        val animationDrawable = context?.let {
            AppCompatResources.getDrawable(
                it,
                R.drawable.animation_loding
            )
        } as? AnimationDrawable
        loadingIndicator.indeterminateDrawable = animationDrawable
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        crnum = arguments?.getInt("crnum") ?: -1
        Log.d("fragmentPage10", "crnum in fragmentPage10: $crnum") // crnum 값 로그 출력
        initViewModel()
        initSearchView()
        observeViewModel()
    }

    // "분석 중" 상태를 표시하는 함수
    private fun showLoadingIndicator() {
        //loadingindicator가 작동하는지 확인하는 로그
        Log.d("fragmentPage10", "LoadingIndicator 작동")
        // 애니메이션 시작
        (loadingIndicator.indeterminateDrawable as? AnimationDrawable)?.start()
        loadingIndicator.visibility = View.VISIBLE
    }

    // "분석 중" 상태를 숨기는 함수
    public fun hideLoadingIndicator() {
        //loadingindicator가 꺼지는지 확인하는 로그
        Log.d("fragmentPage10", "LoadingIndicator 꺼짐")
        // 애니메이션 멈춤
        (loadingIndicator.indeterminateDrawable as? AnimationDrawable)?.stop()
        loadingIndicator.visibility = View.GONE
    }

    private fun initViewModel()  {
        // OkHttpClient에 타임아웃 설정 추가
        val okHttpClient = OkHttpClient.Builder()
            .connectTimeout(60, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(60, TimeUnit.SECONDS)
            .build()

        val gson = GsonBuilder()
            .setLenient()
            .create()

        // Retrofit 인스턴스 생성
        val retrofit = Retrofit.Builder()
            .baseUrl(Constants.BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(ScalarsConverterFactory.create())
            .addConverterFactory(GsonConverterFactory.create(gson))
            .build()

        // ApiService 생성
        val apiService: ApiService = retrofit.create(ApiService::class.java)

        // ViewModelFactory 생성 및 ViewModel 초기화
        val factory = CallerPredictionViewModelFactory(apiService)
        callerPredictionViewModel = ViewModelProvider(this, factory).get(CallerPredictionViewModel::class.java)
    }

    // SearchView 설정
    private fun initSearchView() {
        val searchView = binding.searchView

        // SharedPreferences에서 사용자 아이디를 가져오기 위해 초기화
        sharedPreferences = requireContext().getSharedPreferences("Session_ID", Context.MODE_PRIVATE)

        // SharedPreferences에서 저장된 사용자 토큰(아이디) 가져오기, "Unknown"은 key에 맞는 value가 없을 때 가져오는 값(기본값)
        val userId = sharedPreferences.getString("Session_ID", "Unknown") // "Session_ID"에서 "userToken"으로 키 수정

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean { // 메서드 시그니처 수정
                query?.let { keyword ->
                    if (userId != null && userId != "Unknown") { // userId가 유효한 경우에만 API 호출
                        searchView.clearFocus()
                        showLoadingIndicator()
                        // 데이터 가져오기 요청을 코루틴 내에서 호출
                        viewLifecycleOwner.lifecycleScope.launch {
                            callerPredictionViewModel.fetchCallerPrediction(crnum, keyword)
                        }
                    } else{
                        hideLoadingIndicator()
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
        callerPredictionViewModel.callerPrediction.observe(viewLifecycleOwner, Observer { result ->
            hideLoadingIndicator()
            if (result != null) {
                performSearch(result)
            }
        })

        callerPredictionViewModel.error.observe(viewLifecycleOwner, Observer { error ->
            hideLoadingIndicator()
            error?.let {
                Log.e("fragmentPage10", it)
            }
        })
    }

    private fun performSearch(result: String) {
        // 키보드를 숨김
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view?.windowToken, 0)
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

    companion object {
        private const val ARG_CRNUM = "crnum"

        fun newInstance(crnum: Int): fragmentPage10 {
            val fragment = fragmentPage10()
            val args = Bundle()
            args.putInt(ARG_CRNUM, crnum)
            fragment.arguments = args
            return fragment
        }
    }
}