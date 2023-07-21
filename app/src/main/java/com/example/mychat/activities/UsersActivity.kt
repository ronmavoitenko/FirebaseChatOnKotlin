package com.example.mychat.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.mychat.R
import com.example.mychat.adapters.UsersAdapter
import com.example.mychat.databinding.ActivityUsersBinding
import com.example.mychat.listeners.UserListener
import com.example.mychat.models.User
import com.example.mychat.utilities.Constants
import com.example.mychat.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QueryDocumentSnapshot

class UsersActivity : AppCompatActivity(), UserListener {
    private lateinit var binding:ActivityUsersBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityUsersBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        setListeners()
        getUsers()
    }

    private fun setListeners()
    {
        binding.imageBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }
    }

    private fun showErrorMessage()
    {
        binding.textErrorMessage.text = String.format("%s", "No user available")
        binding.textErrorMessage.visibility = View.VISIBLE
    }

    private fun getUsers()
    {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        database.collection(Constants.KEY_COLLECTION_USERS)
            .get()
            .addOnCompleteListener {task ->
                loading(false)
                val currentUserId = preferenceManager.getString(Constants.KEY_USER_ID)
                if(task.isSuccessful && task.result != null)
                {
                    val users = ArrayList<User>()
                    for(queryDocumentSnapshot: QueryDocumentSnapshot in task.result)
                    {
                        if(currentUserId == queryDocumentSnapshot.id)
                        {
                            continue
                        }
                        val user = User()
                        user.name = queryDocumentSnapshot.getString(Constants.KEY_NAME).toString()
                        user.email = queryDocumentSnapshot.getString(Constants.KEY_EMAIL).toString()
                        user.image = queryDocumentSnapshot.getString(Constants.KEY_IMAGE).toString()
                        user.token = queryDocumentSnapshot.getString(Constants.KEY_FCM_TOKEN).toString()
                        users.add(user)
                    }
                    if(users.size > 0)
                    {
                        val usersAdapter = UsersAdapter(users, this)
                        binding.usersRecyclerView.adapter = usersAdapter
                        binding.usersRecyclerView.visibility = View.VISIBLE
                    }
                    else
                    {
                         showErrorMessage()
                    }
                }
                else
                {
                    showErrorMessage()
                }
            }
    }

    private fun loading(isLoading: Boolean)
    {
        if(isLoading)
        {
            binding.progressBar.visibility = View.VISIBLE
        }
        else
        {
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onUserClicked(user: User) {
        intent = Intent(applicationContext, ChatActivity::class.java)
        intent.putExtra(Constants.KEY_USER, user)
        startActivity(intent)
        finish()
    }
}