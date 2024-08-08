package com.example.talkssogi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.recyclerview.widget.RecyclerView
import com.example.talkssogi.model.ChatRoom
import android.widget.TextView

class DialogChatRoomAdapter(
    private var chatRooms: List<ChatRoom>,
    private val onChatRoomSelected: (ChatRoom) -> Unit // ChatRoom 선택 시 콜백
) : RecyclerView.Adapter<DialogChatRoomAdapter.ChatRoomViewHolder>() {

    var selectedCrnum: Int = -1 // 선택된 채팅방의 crnum

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatRoomViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_room_dialog, parent, false)
        return ChatRoomViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatRoomViewHolder, position: Int) {
        val chatRoom = chatRooms[position]
        holder.bind(chatRoom)

        // 라디오 버튼의 체크 상태 업데이트
        holder.radioButton.isChecked = (selectedCrnum == chatRoom.crnum)

        // View를 클릭했을 때의 리스너 설정
        holder.itemView.setOnClickListener {
            selectedCrnum = chatRoom.crnum // 선택된 crnum 저장
            notifyDataSetChanged() // 뷰 갱신
        }

        // 라디오 버튼을 클릭했을 때의 리스너 설정
        holder.radioButton.setOnClickListener {
            selectedCrnum = chatRoom.crnum // 선택된 crnum 저장
            notifyDataSetChanged() // 뷰 갱신
        }

        // 선택 상태에 따른 스타일 변화 (선택된 경우 색상 변경 등)
        holder.itemView.isSelected = (selectedCrnum == chatRoom.crnum)
    }

    override fun getItemCount() = chatRooms.size

    fun submitList(newChatRooms: List<ChatRoom>) {
        chatRooms = newChatRooms
        notifyDataSetChanged()
    }

    private fun selectChatRoom(chatRoom: ChatRoom) {
        selectedCrnum = chatRoom.crnum // 선택된 crnum 저장
        onChatRoomSelected(chatRoom) // 선택된 ChatRoom을 콜백으로 전달
        notifyDataSetChanged() // 뷰 갱신
    }

    inner class ChatRoomViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val chatRoomName: TextView = itemView.findViewById(R.id.chat_room_name)
        val radioButton: RadioButton = itemView.findViewById(R.id.radioButtonChatRoom) // 라디오 버튼 초기화

        fun bind(chatRoom: ChatRoom) {
            chatRoomName.text = chatRoom.name // ChatRoom의 이름 표시
        }
    }
}
