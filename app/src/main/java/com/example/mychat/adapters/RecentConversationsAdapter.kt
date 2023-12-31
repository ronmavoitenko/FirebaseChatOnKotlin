package com.example.mychat.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mychat.databinding.ItemConteinerRecentConversionBinding
import com.example.mychat.listeners.ConversionListener
import com.example.mychat.models.ChatMessage
import com.example.mychat.models.User

class RecentConversationsAdapter(
    private val chatMessage: List<ChatMessage>,
    private val conversionListener: ConversionListener) : RecyclerView.Adapter<RecentConversationsAdapter.ConversionViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversionViewHolder {
        return  ConversionViewHolder(
            ItemConteinerRecentConversionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: ConversionViewHolder, position: Int) {
        holder.setData(chatMessage[position])
    }

    override fun getItemCount(): Int {
        return chatMessage.size
    }

    inner class ConversionViewHolder(private val binding: ItemConteinerRecentConversionBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun setData(chatMessage: ChatMessage)
        {
            binding.imageProfile.setImageBitmap(getConversionImage(chatMessage.conversionImage))
            binding.textName.text = chatMessage.conversionName
            binding.textRecentMessage.text = chatMessage.message
            binding.root.setOnClickListener {
                val user = User()
                user.id = chatMessage.conversionId
                user.name = chatMessage.conversionName
                user.image = chatMessage.conversionImage
                conversionListener.onConversionClicked(user)
            }
        }
    }


    private fun getConversionImage(encodedImage: String): Bitmap
    {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }
}