package com.example.mychat.activities

import android.content.Intent
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.mychat.R
import com.example.mychat.databinding.ActivityChatBinding
import com.example.mychat.models.User
import com.example.mychat.utilities.Constants
import java.io.Serializable

class ChatActivity : AppCompatActivity() {
    private lateinit var binding: ActivityChatBinding
    private lateinit var receiverUser: User
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityChatBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setListeners()
        loadReceiverDetails()
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
    }
}