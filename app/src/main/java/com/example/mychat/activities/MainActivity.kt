package com.example.mychat.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Base64
import android.view.View
import android.widget.Toast
import com.example.mychat.adapters.RecentConversationsAdapter
import com.example.mychat.databinding.ActivityMainBinding
import com.example.mychat.listeners.ConversionListener
import com.example.mychat.models.ChatMessage
import com.example.mychat.models.User
import com.example.mychat.utilities.Constants
import com.example.mychat.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.EventListener
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.messaging.FirebaseMessaging
import java.util.Collections


class MainActivity : AppCompatActivity(), ConversionListener {
    private lateinit var binding: ActivityMainBinding
    private lateinit var preferenceManager: PreferenceManager
    private lateinit var conversations: ArrayList<ChatMessage>
    private lateinit var conversationsAdapter: RecentConversationsAdapter
    private lateinit var database: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        init()
        loadUserDetails()
        getToken()
        setListeners()
        listenConversation()
    }

    private fun init()
    {
        conversations = ArrayList()
        conversationsAdapter = RecentConversationsAdapter(conversations, this)
        binding.conversationsRecyclerView.adapter = conversationsAdapter
        database = FirebaseFirestore.getInstance()
    }

    private fun setListeners()
    {
        binding.imageSignOut.setOnClickListener {
            signOut()
        }
        binding.fabNewChat.setOnClickListener {
            startActivity(Intent(applicationContext, UsersActivity::class.java))
        }
    }

    private fun loadUserDetails()
    {
        binding.textName.text = preferenceManager.getString(Constants.KEY_NAME)
        val bytes = Base64.decode(preferenceManager.getString(Constants.KEY_IMAGE), Base64.DEFAULT)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        binding.imageProfile.setImageBitmap(bitmap)
    }

    private fun showToast(message: String)
    {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun listenConversation()
    {
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_SENDER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
            .addSnapshotListener(eventListener)
        database.collection(Constants.KEY_COLLECTION_CONVERSATIONS)
            .whereEqualTo(Constants.KEY_RECEIVER_ID, preferenceManager.getString(Constants.KEY_USER_ID))
            .addSnapshotListener(eventListener)
    }

    @SuppressLint("NotifyDataSetChanged")
    private var eventListener = EventListener<QuerySnapshot> { value, error ->
        if(error != null)
        {
            return@EventListener
        }
        if(value != null)
        {
            for(documentChange: DocumentChange in value.documentChanges)
            {
                if(documentChange.type == DocumentChange.Type.ADDED)
                {
                    val senderId: String = documentChange.document.getString(Constants.KEY_SENDER_ID)!!
                    val receivedId: String = documentChange.document.getString(Constants.KEY_RECEIVER_ID)!!
                    val chatMessage = ChatMessage()
                    chatMessage.senderId = senderId
                    chatMessage.receivedId = receivedId
                    if(preferenceManager.getString(Constants.KEY_USER_ID) == senderId)
                    {
                        chatMessage.conversionImage = documentChange.document.getString(Constants.KEY_RECEIVER_IMAGE)!!
                        chatMessage.conversionName = documentChange.document.getString(Constants.KEY_RECEIVER_NAME)!!
                        chatMessage.conversionId = documentChange.document.getString(Constants.KEY_RECEIVER_ID)!!
                    }
                    else
                    {
                        chatMessage.conversionImage = documentChange.document.getString(Constants.KEY_SENDER_IMAGE)!!
                        chatMessage.conversionName = documentChange.document.getString(Constants.KEY_SENDER_NAME)!!
                        chatMessage.conversionId = documentChange.document.getString(Constants.KEY_SENDER_ID)!!
                    }
                    chatMessage.message = documentChange.document.getString(Constants.KEY_LAST_MESSAGE)!!
                    chatMessage.dateObject = documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!
                    conversations.add(chatMessage)
                }
                else if(documentChange.type == DocumentChange.Type.ADDED)
                {
                    for(i in 0 until conversations.size)
                    {
                        val senderId: String = documentChange.document.getString(Constants.KEY_SENDER_ID)!!
                        val receiverId: String = documentChange.document.getString(Constants.KEY_RECEIVER_ID)!!
                        if(conversations[i].senderId == senderId && conversations[i].receivedId == receiverId)
                        {
                            conversations[i].message = documentChange.document.getString(Constants.KEY_LAST_MESSAGE)!!
                            conversations[i].dateObject = documentChange.document.getDate(Constants.KEY_TIMESTAMP)!!
                            break
                        }
                    }
                }
            }
            conversations.sortWith(compareByDescending { it.dateObject })
            conversationsAdapter.notifyDataSetChanged()
            binding.conversationsRecyclerView.smoothScrollToPosition(0)
            binding.conversationsRecyclerView.visibility = View.VISIBLE
            binding.progressBar.visibility = View.GONE
        }
    }

    private fun getToken()
    {
        FirebaseMessaging.getInstance().token.addOnSuccessListener(this::updateToken)
    }

    private fun updateToken(token: String)
    {
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
            preferenceManager.getString(Constants.KEY_USER_ID)
        )
        documentReference.update(Constants.KEY_FCM_TOKEN, token)
            .addOnFailureListener {
                showToast("Unable to update token")
            }
    }
    private fun signOut()
    {
        showToast("Signing out...")
        val database = FirebaseFirestore.getInstance()
        val documentReference = database.collection(Constants.KEY_COLLECTION_USERS).document(
            preferenceManager.getString(Constants.KEY_USER_ID)
        )
        val updates = hashMapOf<String, Any>()
        updates[Constants.KEY_FCM_TOKEN] = FieldValue.delete()
        documentReference.update(updates)
            .addOnSuccessListener {
                preferenceManager.clear()
                startActivity(Intent(applicationContext, SignInActivity::class.java))
                finish()
            }
            .addOnFailureListener {
                showToast("Unable to sign out")
            }
    }

    override fun onConversionClicked(user: User) {
        val intent = Intent(applicationContext, ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER, user)
        startActivity(intent)
    }
}