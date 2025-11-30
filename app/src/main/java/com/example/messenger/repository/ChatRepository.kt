package com.example.messenger.repository

import android.app.Application
import android.content.Context
import android.util.Base64
import com.example.messenger.Chat
import com.example.messenger.DataBase.FireBase
import com.example.messenger.DataBase.RoomDataBase
import com.example.messenger.MessageStatus
import com.example.messenger.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldPath
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.collections.forEach

class ChatRepository(
    private val dao: RoomDataBase.MainDb,
) {
    fun listenForChats(loggedInUser: FirebaseUser, onUpdate: (List<RoomDataBase.ChatEntity>) -> Unit) {
        FireBase().store.collection("Chats")
            .whereArrayContains("participants", loggedInUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Firestore listener error: ${error.message}")
                    return@addSnapshotListener
                }
                var chats = snapshot?.documents?.mapNotNull { doc ->
                    RoomDataBase.ChatEntity(
                        chatId = doc.id,
                        participants = (doc.get("participants") as? List<*>)
                            ?.filterIsInstance<String>()
                            ?: emptyList(),
                        chatName = doc.getString("chatName"),
                        chatPhoto = doc.getString("groupPhoto"),
                        adminId = doc.getString("adminId")
                    )
                }
                onUpdate(chats!!)
            }
    }
    fun listenForUsersByList(loggedInUser: FirebaseUser, chats: List<Chat>, onResult: (List<User>) -> Unit) {
        val uids = buildList {
            chats.forEach { addAll(it.participants) }
            loggedInUser?.uid?.let { add(it) }
        }.distinct()
        if (uids.isEmpty()) {
            onResult(emptyList())
            return
        }
        FireBase().store.collection("Users")
            .whereIn(FieldPath.documentId(), uids)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Firestore listener error (Users): ${error.message}")
                    onResult(emptyList())
                    return@addSnapshotListener
                }
                val result = snapshot?.documents?.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    val friends = data["friendsId"] as? List<String> ?: emptyList()
                    User(
                        uid = doc.id,
                        name = data["name"] as? String ?: "",
                        dateOfBirth = data["dateOfBirth"] as? String ?: "",
                        location = data["location"] as? String ?: "",
                        friends = friends,
                        isOnline = data["isOnline"] as? Boolean ?: false,
                        lastSeen = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            .format(Date(data["lastSeen"] as? Long ?: 0L)),
                        localAvatarPath = data["localAvatarPath"] as? String?: ""
                    )
                } ?: emptyList()

                onResult(result)
            }
    }

    fun listenForAllMessages(chats: List<Chat>, context: Context, onChatMessagesUpdate: (List<RoomDataBase.MessageEntity>) -> Unit) {
        chats.forEach { chat ->
            FireBase().store.collection("Chats")
                .document(chat.chatId)
                .collection("Messages")
                .orderBy("dateOfSend") // 👈 сортировка по дате
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        println("Firestore listener error (Messages): ${error.message}")
                        return@addSnapshotListener
                    }
                    var messages = snapshot?.documents?.mapNotNull { doc ->
                        val photoName = doc.getString("photoName")
                        val photo = doc.getString("photo")
                        if (photoName!=""&&photo!=""&&photoName!=null&&photo!=null) {
                            val bytes = Base64.decode(photo, Base64.DEFAULT)
                            val file = File(context.filesDir, photoName)
                            file.outputStream().use { stream -> stream.write(bytes) }
                        }
                        RoomDataBase.MessageEntity(
                            messageId = doc.id,
                            chatId = chat.chatId,
                            senderUid = doc.getString("senderUid") ?: "",
                            messageText = doc.getString("messageText") ?: "",
                            status = MessageStatus.fromString(doc.getString("status") ?: ""),
                            dateOfSend = doc.getLong("dateOfSend") ?: 0L,
                            photoName = doc.getString("photoName")
                        )
                    } ?: emptyList()
                    onChatMessagesUpdate(messages)
                }
        }
    }
    fun sortChatsByLastMessage(list: List<Chat>): List<Chat> {
        return list.sortedByDescending { chat: Chat ->
            dateAsLong(chat.listMessage.lastOrNull()?.dateOfSend ?: "")
        }
    }
    fun dateAsLong(string: String): Long {
        return try {
            SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).parse(string)?.time ?: 0L
        } catch (e: Exception) {
            0L
        }
    }
}