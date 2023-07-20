package com.example.mychat.activities

import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.util.Base64
import android.util.Patterns
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.view.isInvisible
import com.example.mychat.R
import com.example.mychat.databinding.ActivitySignUpBinding
import com.example.mychat.utilities.Constants
import com.example.mychat.utilities.PreferenceManager
import com.google.firebase.firestore.FirebaseFirestore
import java.io.ByteArrayOutputStream
import java.io.FileNotFoundException
import java.io.InputStream

class SignUpActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySignUpBinding
    private lateinit var encodedImage: String
    private lateinit var preferenceManager: PreferenceManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        preferenceManager = PreferenceManager(applicationContext)
        encodedImage = ""
        setListeners()
    }

    private fun setListeners()
    {
        binding.textSignIn.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
        }
        binding.buttonSignUp.setOnClickListener {
            if(isValidSignUpDetails())
            {
                signUp()
            }
        }
        binding.layoutImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            pickImage.launch(intent)
        }
    }

    private fun showToast(message: String)
    {
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }

    private fun signUp()
    {
        loading(true)
        val database = FirebaseFirestore.getInstance()
        val user = hashMapOf<String, Any>()
        user[Constants.KEY_NAME] = binding.inputName.text.toString()
        user[Constants.KEY_EMAIL] = binding.inputEmail.text.toString()
        user[Constants.KEY_PASSWORD] = binding.inputPassword.text.toString()
        user[Constants.KEY_IMAGE] = encodedImage
        database.collection(Constants.KEY_COLLECTION_USERS)
            .add(user)
            .addOnSuccessListener {documentReference ->
                loading(false)
                preferenceManager.putBoolean(Constants.KEY_IS_SIGNED_IN, true)
                preferenceManager.putString(Constants.KEY_USER_ID, documentReference.id)
                preferenceManager.putString(Constants.KEY_NAME, binding.inputName.text.toString())
                preferenceManager.putString(Constants.KEY_IMAGE, encodedImage)
                intent = Intent(applicationContext, MainActivity::class.java)
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
                startActivity(intent)
            }.addOnFailureListener {exception ->
                loading(false)
                showToast(exception.message.toString())
            }
    }

    private fun encodeImage(bitmap: Bitmap): String
    {
        val previewWidth = 150
        val previewHeight = bitmap.height * previewWidth / bitmap.width
        val previewBitmap:Bitmap = Bitmap.createScaledBitmap(bitmap, previewWidth, previewHeight, false)
        val byteArrayOutputStream = ByteArrayOutputStream()
        previewBitmap.compress(Bitmap.CompressFormat.JPEG, 50, byteArrayOutputStream)
        val bytes = byteArrayOutputStream.toByteArray()
        return Base64.encodeToString(bytes, Base64.DEFAULT)
    }

    private val pickImage = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val imageUri: Uri? = data?.data
            try {
                val inputStream: InputStream? = contentResolver.openInputStream(imageUri!!)
                val bitmap = BitmapFactory.decodeStream(inputStream)
                binding.imageProfile.setImageBitmap(bitmap)
                binding.textAddImage.visibility = View.GONE
                encodedImage = encodeImage(bitmap)
            } catch (e: FileNotFoundException) {
                e.printStackTrace()
            }
        }
    }

    private fun isValidSignUpDetails() : Boolean
    {
        if(encodedImage.isEmpty())
        {
            showToast("Select profile image")
            loading(false)
            return false
        }
        else if(binding.inputName.text.toString().trim().isEmpty())
        {
            showToast("Enter name")
            return false
        }
        else if(binding.inputEmail.text.toString().trim().isEmpty())
        {
            showToast("Enter email")
            return false
        }
        else if(!Patterns.EMAIL_ADDRESS.matcher(binding.inputEmail.text.toString()).matches())
        {
            showToast("Enter valid image")
            return false
        }
        else if(binding.inputPassword.text.toString().trim().isEmpty())
        {
            showToast("Enter password")
            return false
        }
        else if(binding.inputConfirmPassword.text.toString().trim().isEmpty())
        {
            showToast("Confirm your password")
            return false
        }
        else if(binding.inputPassword.text.toString() != binding.inputConfirmPassword.text.toString())
        {
            showToast("Password & confirm password must be same")
            return false
        }
        else
        {
            return true
        }
    }

    private fun loading(isLoading:Boolean)
    {
        if(isLoading)
        {
            binding.buttonSignUp.visibility = View.INVISIBLE
            binding.progressBar.visibility = View.VISIBLE
        }
        else
        {
            binding.progressBar.visibility = View.INVISIBLE
            binding.buttonSignUp.visibility = View.VISIBLE
        }
    }
}