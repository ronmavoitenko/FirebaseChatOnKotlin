package com.example.mychat.listeners

import com.example.mychat.models.User

interface UserListener {
    fun onUserClicked(user: User)
}