package com.example.messenger


import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.messenger.DataBase.RoomDataBase
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

enum class MessageStatus {
    PENDING,
    SENT,
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
        fun MessageStatusIcon(status: MessageStatus, color: Color, modifier: Modifier) {
            Row {
                when (status) {
                    PENDING -> {
                        Icon(
                            imageVector = Icons.Default.Schedule,
                            contentDescription = "Delivered",
                            tint = color,
                            modifier = modifier
                        )
                    }
                    SENT -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Sent",
                            tint = color,
                            modifier = modifier
                        )
                    }
                    READ -> {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Read",
                            tint = color,
                            modifier = modifier
                        )
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Read",
                            tint = color,
                            modifier = modifier
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
        dateOfSend = if (message.dateOfSend!=0L) SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(message.dateOfSend)) else "",
        photoName = message.photoName
    )
}


data class Chat (
    val chatId: String = "",
    val participants: List<String> = emptyList(),
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
        val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("d MMMM", Locale.getDefault())
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

@Composable
fun timeAfterMassage(dateOfSend: String): String{
    val inputFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
    val messageDate = inputFormat.parse(dateOfSend) ?: return ""
    val now = Date()
    val diffInMillis = now.time - messageDate.time
    if (diffInMillis <= 0) return "now"
    val seconds = TimeUnit.MILLISECONDS.toSeconds(diffInMillis)
    val minutes = TimeUnit.MILLISECONDS.toMinutes(diffInMillis)
    val hours = TimeUnit.MILLISECONDS.toHours(diffInMillis)
    val days = TimeUnit.MILLISECONDS.toDays(diffInMillis)
    return when {
        days > 30 -> {
            val outputFormat = SimpleDateFormat("d MMM", Locale.ENGLISH)
            outputFormat.format(messageDate)
        }
        days > 0 -> "${days}${stringResource(R.string.days)}"
        hours > 0 -> "${hours}${stringResource(R.string.hours)}"
        minutes > 0 -> "${minutes}${stringResource(R.string.minutes)}"
        else -> "1${stringResource(R.string.minutes)}"
    }
}

fun unreadMessages(chat: Chat):Int{
    var n = 1
    val N = chat.listMessage.size
    try {
        while (chat.listMessage[N-n-1].status!= MessageStatus.READ)
            n++
    }
    catch (e: Exception){}
    return n
}