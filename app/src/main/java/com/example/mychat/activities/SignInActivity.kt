package com.example.mychat.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Message
import android.util.Patterns
import android.view.View
import android.widget.Toast
import com.example.mychat.R
import com.example.mychat.databinding.ActivitySignInBinding
import com.example.mychat.utilities.Constants
import com.example.mychat.utilities.PreferenceManager
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import java.lang.Exception
import java.util.Objects

class SignInActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySignInBinding
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        if(preferenceManager.getBoolean(Constants.KEY_IS_SIGNED_IN))
        {
            val intent = Intent(applicationContext, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
        setListeners()
    }

    private fun setListeners()
    {
        binding.textCreateNewAccount.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
        binding.buttonSignIn.setOnClickListener {
            if(isValidSignInDdetails())
            {
                signIn()
            }
        }
    }

    private fun showToast(message: String)
    {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun signIn()
    {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        val user = hashMapOf<String, Any>()
        database.collection(Constants.KEY_COLLECTION_USERS)
            .whereEqualTo(Constants.KEY_EMAIL, binding.inputEmail.text.toString())
            .whereEqualTo(Constants.KEY_PASSWORD, binding.inputPassword.text.toString())
            .get()
            .addOnCompleteListener{ task->
                if(task.isSuccessful && task.getResult() != null && task.getResult().documents.size > 0)
                {
                    val documentSnapshot = task.getResult().documents.get(0)
                    preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                    preferenceManager.putString(Constants.KEY_USER_ID, documentSnapshot.id)
                    preferenceManager.putString(Constants.KEY_NAME, documentSnapshot.getString(Constants.KEY_NAME).toString())
                    preferenceManager.putString(Constants.KEY_IMAGE, documentSnapshot.getString(Constants.KEY_IMAGE).toString())
                    val intent = Intent(applicationContext, MainActivity::class.java)
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                    startActivity(intent)
                }
                else
                {
                    loading(false)
                    showToast("Unable to sign in")
                }
            }
    }

    private fun loading(isLoading:Boolean)
    {
        if(isLoading)
        {
            binding.buttonSignIn.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        }
        else
        {
            binding.progressBar.visibility = View.INVISIBLE
            binding.buttonSignIn.visibility = View.VISIBLE
        }
    }

    private fun isValidSignInDdetails(): Boolean
    {
        if(binding.inputEmail.text.toString().trim().isEmpty())
        {
            showToast("Enter email")
            return false
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString()).matches())
        {
            showToast("Enter valid email")
            return false
        }
        else if(binding.inputPassword.text.toString().trim().isEmpty())
        {
            showToast("Enter password")
            return false
        }
        else
        {
            return true
        }
    }

}