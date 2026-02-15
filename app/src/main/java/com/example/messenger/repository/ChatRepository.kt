package com.example.messenger.repository

import android.content.Context
import android.util.Base64
import com.example.messenger.Chat
import com.example.messenger.DataBase.FireBaseService
import com.example.messenger.DataBase.RoomDataBase
import com.example.messenger.MessageStatus
import com.example.messenger.User
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.ListenerRegistration
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale


interface IChatRepository {
    fun listenForChats(loggedInUser: FirebaseUser, onUpdate: (List<RoomDataBase.ChatEntity>) -> Unit): ListenerRegistration

    fun listenForUsersByList(uids: List<String>, onResult: (List<User>) -> Unit): ListenerRegistration

    fun listenForAllMessages(
        chats: List<Chat>,
        context: Context,
        onChatMessagesUpdate: (List<RoomDataBase.MessageEntity>) -> Unit
    ): List<ListenerRegistration>

    fun sortChatsByLastMessage(list: List<Chat>): List<Chat>
}

class ChatRepository(
    private val dao: RoomDataBase.MainDb,
    private val firebaseService: FireBaseService
) : IChatRepository {
    override fun listenForChats(loggedInUser: FirebaseUser, onUpdate: (List<RoomDataBase.ChatEntity>) -> Unit): ListenerRegistration {
        return firebaseService.store.collection("Chats")
            .whereArrayContains("participants", loggedInUser.uid)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    println("Firestore listener error: ${error.message}")
                    return@addSnapshotListener
                }
                val chats = snapshot?.documents?.mapNotNull { doc ->
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
    override fun listenForUsersByList(uids: List<String>, onResult: (List<User>) -> Unit): ListenerRegistration {
        return firebaseService.store.collection("Users")
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
                        city = data["location"] as? String ?: "",
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

    override fun listenForAllMessages(chats: List<Chat>, context: Context, onChatMessagesUpdate: (List<RoomDataBase.MessageEntity>) -> Unit): List<ListenerRegistration> {
        return chats.map { chat ->
             firebaseService.store.collection("Chats")
                .document(chat.chatId)
                .collection("Messages")
                .orderBy("dateOfSend")
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
                        val isPending = doc.metadata.hasPendingWrites()
                        RoomDataBase.MessageEntity(
                            messageId = doc.id,
                            chatId = chat.chatId,
                            senderUid = doc.getString("senderUid") ?: "",
                            messageText = doc.getString("messageText") ?: "",
                            status = if (isPending) MessageStatus.PENDING else MessageStatus.fromString(doc.getString("status") ?: ""),
                            dateOfSend = doc.getTimestamp("dateOfSend")?.toDate()?.time ?: System.currentTimeMillis(),
                            photoName = doc.getString("photoName")
                        )
                    } ?: emptyList()
                    onChatMessagesUpdate(messages)
                }
        }
    }
    override fun sortChatsByLastMessage(list: List<Chat>): List<Chat> {
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