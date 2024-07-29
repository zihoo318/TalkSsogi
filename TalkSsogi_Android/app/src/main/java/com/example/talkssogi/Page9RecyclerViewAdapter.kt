package com.example.talkssogi

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class Page9RecyclerViewAdapter(private val itemList: List<ImageURL>) :
    RecyclerView.Adapter<Page9RecyclerViewAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.item_image)

        fun bind(imageURL: ImageURL) {
            // 초기 화면 설정
            imageView.setImageResource(R.drawable.phone) // 초기 이미지 설정

            Glide.with(itemView.context)
                .load(imageURL.imageUrl)
                .placeholder(R.drawable.happy2) // 로딩 중 보여줄 이미지
                .error(R.drawable.error) // 에러 발생 시 보여줄 이미지
                .into(imageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.recyclerview_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(itemList[position])
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}
