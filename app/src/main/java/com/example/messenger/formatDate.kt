package com.example.messenger

import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit

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
fun timeAfterMessage(dateOfSend: String): String{
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

@Composable
fun formatLastSeen(isOnline: Boolean, lastSeenString: String): String {
    if (isOnline) return stringResource(R.string.last_seen_online)
    val lastSeenTimestamp = try {
        SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(lastSeenString)?.time ?: 0L
    } catch (e: Exception) {
        0L
    }
    if (lastSeenTimestamp <= 0L) return ""

    val context = LocalContext.current
    val resources = context.resources
    val now = System.currentTimeMillis()
    val diffMillis = now - lastSeenTimestamp
    val diffMinutes = (diffMillis / 60000).toInt()
    val diffHours = (diffMillis / 3600000).toInt()

    val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
    val timeString = timeFormat.format(Date(lastSeenTimestamp))

    return when {
        diffMinutes < 1 -> resources.getString(R.string.last_seen_just_now)

        diffMinutes < 60 -> resources.getQuantityString(R.plurals.minutes_ago, diffMinutes, diffMinutes)

        diffHours < 24 && isSameDay(now, lastSeenTimestamp) ->
            resources.getString(R.string.last_seen_today, timeString)

        diffHours < 48 && isYesterday(now, lastSeenTimestamp) ->
            resources.getString(R.string.last_seen_yesterday, timeString)

        else -> {
            val dateFormat = SimpleDateFormat("d MMMM в HH:mm", Locale.getDefault())
            dateFormat.format(Date(lastSeenTimestamp))
        }
    }
}

fun isSameDay(now: Long, timestamp: Long): Boolean {
    val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    return fmt.format(Date(now)) == fmt.format(Date(timestamp))
}

fun isYesterday(now: Long, timestamp: Long): Boolean {
    val fmt = SimpleDateFormat("yyyyMMdd", Locale.getDefault())
    val yesterday = now - 86400000
    return fmt.format(Date(yesterday)) == fmt.format(Date(timestamp))
}