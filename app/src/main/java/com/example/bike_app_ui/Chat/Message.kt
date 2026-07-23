package com.example.bike_app_ui.Chat

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class ChatMeesage(
    val id: String = java.util.UUID.randomUUID().toString(),
    val senderPeerId: String,
    val senderName: String,
    val text: String,
    val timeStamp: Long = System.currentTimeMillis(),
    val isMine: Boolean
) {
    val formattedTime: String
        get() {
            val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
            return sdf.format(Date(timeStamp))
        }
}