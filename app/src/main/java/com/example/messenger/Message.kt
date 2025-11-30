package com.example.messenger


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.messenger.DataBase.RoomDataBase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

enum class MessageStatus {
    SENT,
    DELIVERED,
    READ;

    companion object {
        fun fromString(str: String?): MessageStatus {
            return try {
                if (str != null) valueOf(str) else SENT
            } catch (e: IllegalArgumentException) {
                SENT
            }
        }
        @Composable
        fun MessageStatusIcon(status: MessageStatus, color: Color) {
            Row {
                when (status) {
                    SENT -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Sent",
                            tint = color,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    DELIVERED -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Delivered",
                            tint = color,
                            modifier = Modifier.size(16.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Delivered",
                            tint = color,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    READ -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Read",
                            tint = color,
                            modifier = Modifier.size(16.dp)
                        )
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Read",
                            tint = color,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }
            }
        }
    }
}

data class Message(
    val messageId: String = "",
    val senderUid: String = "",
    val messageText: String = "",
    val status: MessageStatus= MessageStatus.SENT,
    val dateOfSend: String,
    val photoName: String? = null
){
    constructor(message: RoomDataBase.MessageEntity) : this(
        messageId = message.messageId,
        senderUid = message.senderUid,
        messageText = message.messageText,
        status = message.status,
        dateOfSend = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(message.dateOfSend)),
        photoName = message.photoName
    )
}


data class Chat (
    val chatId: String = "",
    val participants: List<String> = emptyList<String>(),
    val listMessage: List <Message> =  emptyList(),
    val chatName: String? = null,
    val chatPhoto: String? = null,
    val adminId: String? = null
){
    constructor(chat: RoomDataBase.ChatWithMessages) : this(
        chatId = chat.chat.chatId,
        participants = chat.chat.participants,
        listMessage =chat.listMessage.map { Message(it) },
        chatName = chat.chat.chatName,
        chatPhoto = chat.chat.chatPhoto,
        adminId = chat.chat.adminId
    )
    constructor(chat: RoomDataBase.ChatEntity) : this(
        chatId = chat.chatId,
        participants = chat.participants,
        listMessage = emptyList(),
        chatName = chat.chatName,
        chatPhoto = chat.chatPhoto,
        adminId = chat.adminId
    )
}

fun dateConvertor(dateOfSend: String): String{
   return try {
        // Используем старый добрый SimpleDateFormat
        val inputFormat = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
        val outputFormat = java.text.SimpleDateFormat("d MMMM", java.util.Locale("ru"))
        val date = inputFormat.parse(dateOfSend)
        if (date != null) {
            outputFormat.format(date)
        } else {
            dateOfSend.substringBefore(" ")
        }
    } catch (e: Exception) {
        dateOfSend.substringBefore(" ")
    }
}