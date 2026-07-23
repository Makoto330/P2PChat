package com.example.bike_app_ui.Chat

import android.R
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun MessageBubble(message: ChatMeesage) {
    val alignment = if (message.isMine) Alignment.CenterEnd else Alignment.CenterStart
    val bubbleColor = if (message.isMine) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.surfaceVariant
    }
    val textColor = if (message.isMine){
        MaterialTheme.colorScheme.onPrimary
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    val shape = if (message.isMine) {
        RoundedCornerShape(16.dp, 16.dp, 2.dp,16.dp)
    } else {
        RoundedCornerShape(16.dp, 16.dp, 16.dp, 2.dp)
    }

    Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = alignment
    ) {
        Column(
            horizontalAlignment = if (message.isMine) Alignment.End else Alignment.Start,
            modifier = Modifier.widthIn(max = 280.dp)
        ) {
            if (!message.isMine) {
                Text(
                    text = message.senderName,
                    fontSize = 10.sp,
                    color = Color.Gray,
                    modifier = Modifier.padding(start = 4.dp, bottom = 2.dp)
                )
            }

            Surface(
                color = bubbleColor,
                shape = shape,
                shadowElevation = 1.dp
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(
                        text = message.text,
                        color = textColor,
                        fontSize = 15.sp
                    )
                    Text(
                        text = message.formattedTime,
                        color = textColor.copy(alpha = 0.7f),
                        fontSize = 10.sp,
                        modifier = Modifier.align(Alignment.End)
                    )
                }
            }
        }
    }
}