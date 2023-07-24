package com.example.mychat.models

import java.util.Date

class ChatMessage {
    var senderId:String = ""
    var receivedId:String = ""
    var message:String = ""
    var dataTime:String = ""
    lateinit var dateObject: Date
}