package com.example.mychat.listeners

import com.example.mychat.models.User


interface ConversionListener {
    fun onConversionClicked(user: User)
}