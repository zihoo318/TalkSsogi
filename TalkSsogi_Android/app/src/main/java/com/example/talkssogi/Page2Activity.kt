package com.example.talkssogi

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.app.AlertDialog

class Page2Activity : AppCompatActivity() {

    private lateinit var user_name: TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var chatRoomAdapter: ChatRoomAdapter
    private lateinit var bottomNavigationView : BottomNavigationView
    private val viewModel: MyViewModel by lazy {
        (application as MyApplication).viewModel
    }

    private lateinit var sharedPreferences: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.page2)

        // SharedPreferences에서 사용자 아이디를 가져온다
        sharedPreferences = getSharedPreferences("Session_ID", Context.MODE_PRIVATE)
        // user_name TextView 초기화
        user_name = findViewById(R.id.user_name)
        // SharedPreferences에서 사용자 아이디를 가져온다
        val userId = sharedPreferences.getString("Session_ID", "Unknown User")
        // 사용자 아이디를 TextView에 설정
        user_name.text = userId

        bottomNavigationView = findViewById(R.id.bottomNavigationView)
        recyclerView = findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(this)
        //chatRoomAdapter = ChatRoomAdapter(emptyList()) // 초기화는 빈 리스트로
        // Adapter 생성 시 클릭 리스너 전달
        chatRoomAdapter = ChatRoomAdapter(emptyList(), { chatRoom ->
            // 클릭 시 처리할 로직
            val intent = Intent(this, FragmentActivity::class.java)
            intent.putExtra("chatRoomId", chatRoom.crnum) // 채팅방 ID를 전달
            startActivity(intent)
        }, { chatRoom ->
                // 길게 눌렀을 때 삭제 다이얼로그 표시
                showDeleteConfirmationDialog(chatRoom)
        })
        recyclerView.adapter = chatRoomAdapter

        // SharedPreferences에서 사용자 아이디를 가져온다
        sharedPreferences = getSharedPreferences("Session_ID", Context.MODE_PRIVATE)

        // 실시간으로 변화 확인하면서 화면 출력(=api요청 시 실행 됨)
        viewModel.chatRoomList.observe(this, { chatRooms ->
            Log.d("fetchChatRooms", "옵저버 감지 Received chat rooms: $chatRooms") // 로그 추가
            chatRooms?.let {
                chatRoomAdapter.submitList(it)
            }
        })

        // BottomNavigationView의 아이템 선택 리스너 설정
        bottomNavigationView.setOnItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.navigation_update -> {
                    // 업데이트 선택 시 처리
                    true
                }
                R.id.navigation_add -> {
                    // 추가 선택 시 처리: Page3Activity로 이동
                    val userToken = sharedPreferences.getString("Session_ID", null)
                    val intent = Intent(this, Page3Activity::class.java)
                    intent.putExtra("Session_ID", userToken)
                    startActivity(intent)
                    true
                }
                R.id.navigation_logout -> {
                    // 로그아웃 선택 시 처리(로그아웃할건지 한번 물어보고, 액티비티1로 이동)
                    showLogoutConfirmationDialog()
                    true
                }
                else -> false
            }
        }

    }
    override fun onResume() {
        super.onResume()
        // 데이터 갱신
        viewModel.fetchChatRooms()
        Log.d("fetchChatRooms", "2에서 resume으로 갱신 Number of chat rooms")
    }

    private fun showDeleteConfirmationDialog(chatRoom: ChatRoom) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("채팅방 삭제")
        builder.setMessage("${chatRoom.name} 채팅방을 삭제하시겠습니까?")
        builder.setPositiveButton("삭제") { dialog, _ ->
            viewModel.deleteChatRoom(chatRoom.crnum)
            dialog.dismiss()
        }
        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    /* 바로 위의 코드와 기능적으로는 동일하며, 사용자에게 삭제 확인 대화상자를 보여주는데, 코드의 스타일과 간결성 측면에서 차이점이 있음
    private fun showDeleteConfirmationDialog(chatRoom: ChatRoom) {
        AlertDialog.Builder(this)
            .setTitle("채팅방 삭제")
            .setMessage("${chatRoom.name} 채팅방을 삭제하시겠습니까?")
            .setPositiveButton("삭제") { _, _ -> // Kotlin에서 _, _는 함수의 매개변수를 무시할 때 사용하는 방법
                viewModel.deleteChatRoom(chatRoom.crnum)
            }
            .setNegativeButton("취소", null)
            .show()
    }
    */

    /*override fun onResume() {
        super.onResume()
        // 데이터 갱신
        viewModel.fetchChatRooms()
    }*/

    private fun showLogoutConfirmationDialog() {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("로그아웃")
        builder.setMessage("정말 로그아웃 하시겠습니까?")
        builder.setPositiveButton("로그아웃") { dialog, _ ->
            logout()
            dialog.dismiss()
        }
        builder.setNegativeButton("취소") { dialog, _ ->
            dialog.dismiss()
        }
        builder.show()
    }

    private fun logout() {
        // SharedPreferences의 세션 아이디 제거
        sharedPreferences.edit().remove("Session_ID").apply()
        // Page1Activity로 이동
        val intent = Intent(this, Page1Activity::class.java)
        //새로운 Activity를 수행하고 현재 Activity를 스텍에서 제거
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK // 백 스택을 모두 지움
        startActivity(intent) // 새로운 태스크로 Page1Activity를 시작
        finish()
    }

}
