package com.example.mychat.adapters

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.mychat.databinding.ItemConteinerReceivedMessageBinding
import com.example.mychat.databinding.ItemConteinerSentMessageBinding
import com.example.mychat.models.ChatMessage

class ChatAdapter(
    private var receiverProfileImage: Bitmap,
    private var chatMessage: List<ChatMessage>,
    private var senderId: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    val VIEW_TYPE_SEND: Int = 1
    val VIEW_TYPE_RECEIVED: Int = 2

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        if(viewType == VIEW_TYPE_SEND)
        {
            return  SendMessageViewHolder(
                ItemConteinerSentMessageBinding.inflate(LayoutInflater.from(parent.context),
                parent,
                false
                )
            )
        }
        else
        {
            return ReceivedMessageViewHolder(
                ItemConteinerReceivedMessageBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(getItemViewType(position) == VIEW_TYPE_SEND)
        {
            (holder as SendMessageViewHolder).setData(chatMessage[position])
        }
        else
        {
            (holder as ReceivedMessageViewHolder).setData(chatMessage[position], receiverProfileImage)
        }
    }

    override fun getItemCount(): Int {
        return chatMessage.size
    }

    override fun getItemViewType(position: Int): Int {
        if(chatMessage[position].senderId == senderId)
        {
            return VIEW_TYPE_SEND
        }
        else
        {
            return VIEW_TYPE_RECEIVED
        }
    }

    inner class SendMessageViewHolder(private val binding: ItemConteinerSentMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setData(chatMessage: ChatMessage)
        {
            binding.textMessage.text = chatMessage.message
            binding.textDateTime.text = chatMessage.dataTime
        }
    }

    inner class ReceivedMessageViewHolder(private val binding: ItemConteinerReceivedMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun setData(chatMessage: ChatMessage, receiverProfileImage: Bitmap)
        {
            binding.textMessage.text = chatMessage.message
            binding.textDateTime.text = chatMessage.dataTime
            binding.imageProfile.setImageBitmap(receiverProfileImage)
        }
    }
}