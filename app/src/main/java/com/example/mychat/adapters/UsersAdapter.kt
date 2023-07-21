package com.example.mychat.adapters

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.ViewGroup

import androidx.recyclerview.widget.RecyclerView
import com.example.mychat.databinding.ItemConteinerUserBinding
import com.example.mychat.listeners.UserListener
import com.example.mychat.models.User

class UsersAdapter : RecyclerView.Adapter<UsersAdapter.UserViewHolder> {
    private var users: List<User>
    private var userListener: UserListener

    constructor(user: List<User>, userListener: UserListener) {
        this.users = user
        this.userListener = userListener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val itemContainerUserBinding = ItemConteinerUserBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return UserViewHolder(itemContainerUserBinding)
    }

    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        holder.setUserData(users.get(position))
    }

    override fun getItemCount(): Int {
        return users.size
    }

    inner class UserViewHolder(private val binding: ItemConteinerUserBinding) : RecyclerView.ViewHolder(binding.root) {

        fun setUserData(user: User)
        {
            binding.textName.setText(user.name).toString()
            binding.textEmail.setText(user.email).toString()
            binding.imageProfile.setImageBitmap(getUserImage(user.image))
            binding.root.setOnClickListener{
                userListener.onUserClicked(user)
            }
        }

        private fun getUserImage(encodedImage:String): Bitmap
        {
            val bytes = Base64.decode(encodedImage, Base64.DEFAULT)
            return BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
        }
    }
}