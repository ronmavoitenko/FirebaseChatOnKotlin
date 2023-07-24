package com.example.mychat.activities

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import com.example.mychat.adapters.ChatAdapter
import com.example.mychat.databinding.ActivityChatBinding
import com.example.mychat.models.ChatMessage
import com.example.mychat.models.User
import com.example.mychat.utilities.Constants
import com.example.mychat.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import java.io.Serializable
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var receiverUser: User
    private lateinit var chatMessages: ArrayList<ChatMessage>
    private lateinit var chatAdapter: ChatAdapter
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var database: FirebaseFirestore
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
        loadReceiverDetails()
        init()
        listenMessages()
    }

    private fun init()
    {
        preferenceManager = PreferenceManager(applicationContext)
        chatMessages = ArrayList()
        chatAdapter = ChatAdapter(
            getBitmapFromEncodedString(receiverUser.image),
            chatMessages,
            preferenceManager.getString(Constants.KEY_USER_ID)
        )
        binding.chatRecyclerView.adapter = chatAdapter
        database = FirebaseFirestore.getInstance()
    }
    private fun sendMessage()
    {
        val message = hashMapOf<String, Any>()
        message[Constants.KEY_SENDER_ID] = preferenceManager.getString(Constants.KEY_USER_ID)
        message[Constants.KEY_RECEIVED_ID] = receiverUser.id
        message[Constants.KEY_MESSAGE] = binding.inputMessage.text.toString()
        message[Constants.KEY_TIMESTAMP] = Date()
        database.collection(Constants.KEY_COLLECTION_CHAT).add(message)
        binding.inputMessage.text = null
    }

    private fun listenMessages()
    {
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
            .whereEqualTo(Constants.KEY_RECEIVED_ID, receiverUser.id)
            .addSnapshotListener(eventListener)
        database.collection(Constants.KEY_COLLECTION_CHAT)
            .whereEqualTo(Constants.KEY_SENDER_ID, receiverUser.id)
            .whereEqualTo(Constants.KEY_RECEIVED_ID, preferenceManager.getString(Constants.KEY_USER_ID))
            .addSnapshotListener(eventListener)
    }

    private val eventListener = EventListener<QuerySnapshot> { value, error ->
        if (error != null) {
            return@EventListener
        }
        if (value != null) {
            val count = chatMessages.size
            for (documentChange in value.documentChanges) {
                if(documentChange.type == DocumentChange.Type.ADDED)
                {
                    val chatMessage = ChatMessage()
                    chatMessage.senderId = documentChange.document.getString(Constants.KEY_SENDER_ID).toString()
                    chatMessage.receivedId = documentChange.document.getString(Constants.KEY_RECEIVED_ID).toString()
                    chatMessage.message = documentChange.document.getString(Constants.KEY_MESSAGE).toString()
                    chatMessage.dataTime = getReadableDateTime(documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!)
                    chatMessage.dateObject = documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!
                    chatMessages.add(chatMessage)
                }
            }
            chatMessages.sortWith(Comparator { obj1, obj2 -> obj1.dateObject.compareTo(obj2.dateObject) })
            if(count == 0)
            {
                chatAdapter.notifyDataSetChanged()
            }
            else
            {
                chatAdapter.notifyItemRangeInserted(chatMessages.size, chatMessages.size)
                binding.chatRecyclerView.smoothScrollToPosition(chatMessages.size - 1)
            }
            binding.chatRecyclerView.visibility = View.VISIBLE
        }
        binding.progressBar.visibility = View.GONE
    }

    private fun getBitmapFromEncodedString(encodedImage: String): Bitmap
    {
        val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
        return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
    }

    private inline fun <reified T : Serializable> Intent.customGetSerializable(key: String): T? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getSerializableExtra(key, T::class.java)
        } else {
            @Suppress("DEPRECATION")
            getSerializableExtra(key) as? T
        }
    }

    private fun loadReceiverDetails()
    {
        receiverUser = intent.customGetSerializable<User>(Constants.KEY_USER)!!
        binding.textName.text = receiverUser?.name
    }

    private fun setListeners()
    {
        binding.imageBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
        binding.layoutSend.setOnClickListener {
            sendMessage()
        }
    }

    private fun getReadableDateTime(date: Date): String
    {
        return SimpleDateFormat("MMMM dd, yyyy - hh:mm a", Locale.getDefault()).format(date)
    }
}