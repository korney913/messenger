package com.example.messenger

import android.app.Application
import android.content.Context
import android.util.Base64
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.messenger.DataBase.FireBase
import com.example.messenger.DataBase.RoomDataBase
import kotlinx.coroutines.launch
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.messenger.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.tasks.await
import java.io.File
import java.lang.System
import java.util.UUID
import kotlin.String

class ChatViewModel(
    application: Application,
    val localBase: RoomDataBase.MainDb,
    val chatRepository: ChatRepository
) : AndroidViewModel(application)  {
    val loggedInUser = FirebaseAuth.getInstance().currentUser
    var users by mutableStateOf<Map<String, User>>(emptyMap())
    var chats by mutableStateOf<List<Chat>>(emptyList())
    var openedChat by mutableStateOf<Chat?>(null)
    var text = mutableStateOf("")
        private set
    fun updateText(newText: String) {
        text.value = newText
    }
    var photo = mutableStateOf("")
        private set
    fun updatePhoto(newText: String) {
        photo.value = newText
    }
    init {
        if (loggedInUser != null) {
            chatRepository.listenForChats(loggedInUser!!) { it ->
                chats = it.map { chatWithMessages ->   ///?
                    Chat(chatWithMessages)
                }    ///?
                viewModelScope.launch {
                    it.forEach { chat ->
                        localBase.getDao().insertChat(chat)
                    }
                }
                val context = getApplication<Application>().applicationContext
                chatRepository.listenForAllMessages(chats, context) { messages ->
                    viewModelScope.launch {
                        localBase.getDao().insertMessages(messages)
                    }
                }

                chatRepository.listenForUsersByList(loggedInUser, chats) { userList ->
                    viewModelScope.launch {
                        userList.forEach {
                            var localAvatarPath: String? = ""
                            var avatarUpdatedAt = 0L
                            var newAvatarUpdatedAt = 0L
                            val localUser = localBase.getDao().getUser(it.uid)
                            try {
                                newAvatarUpdatedAt =
                                    it.localAvatarPath!!.substringAfter("?").toLong()
                                avatarUpdatedAt =
                                    localUser!!.localAvatarPath!!.substringAfter("?").toLong()
                            } catch (e: Exception) {
                            }
                            if (newAvatarUpdatedAt != avatarUpdatedAt) {
                                val photo = FireBase().store
                                    .collection("Photo")
                                    .document("avatars")
                                    .collection("Users")
                                    .document(it.uid)
                                    .get()
                                    .await()
                                    .getString("base64String")
                                if (!photo.isNullOrEmpty()) {
                                    try {
                                        val context =
                                            getApplication<Application>().applicationContext
                                        val bytes = Base64.decode(photo, Base64.DEFAULT)
                                        val file = File(context.filesDir, it.localAvatarPath!!)
                                        file.outputStream().use { stream -> stream.write(bytes) }
                                    } catch (e: Exception) {
                                        e.printStackTrace()
                                    }
                                }
                            }
                            localBase.getDao().insertUser(RoomDataBase.UserEntity(it))
                        }
                    }
                }
            }

            viewModelScope.launch {
                localBase.getDao().getChatsWithMessages()
                    .map { chatList -> chatList.map { Chat(it) } }
                    .collect { mappedChats ->
                        chats = chatRepository.sortChatsByLastMessage(mappedChats)
                    }
            }
            viewModelScope.launch {
                localBase.getDao().getUsers()
                    .map { list -> list.map { User(it) } }
                    .collect { mapped ->
                        users = mapped.associateBy { it.uid }
                    }
            }
        }
    }

    fun updataAvatar(){}
    fun sendMessage(context: Context, messageText: String, fileName: String?)=viewModelScope.launch {
        var photo = ""
        if (fileName != "") {
            val file = File(context.filesDir, fileName)
            val bytes = file.readBytes()
            photo = Base64.encodeToString(bytes, Base64.DEFAULT)
        }
        val messageId = UUID.randomUUID().toString()
        val message = RoomDataBase.MessageEntity(
            messageId = messageId,
            chatId = openedChat!!.chatId,
            senderUid = loggedInUser!!.uid,
            messageText = messageText,
            status = MessageStatus.SENT,
            dateOfSend = System.currentTimeMillis(),
            photoName = fileName
        )
        localBase.getDao().insertMessage(message)

        FireBase().store.collection("Chats")
            .document(openedChat!!.chatId)
            .collection("Messages")
            .document(messageId).set(mapOf(
                "senderUid" to message.senderUid,
                "messageText" to  message.messageText,
                "status" to message.status,
                "dateOfSend" to message.dateOfSend,
                "photoName" to fileName,
                "photo" to photo
            ))
    }

    fun readMessage(message: Message)=viewModelScope.launch {
        localBase.getDao().updateMessageStatus(message.messageId, MessageStatus.READ)
        FireBase().store.collection("Chats")
            .document(openedChat!!.chatId)
            .collection("Messages")
            .document(message.messageId)
            .update("status", MessageStatus.READ.toString())
    }

    fun addChat(listUid: List<String>, chatName: String? = null, chatPhoto: String? = null, adminId: String? = null) {
        val list = listUid + loggedInUser!!.uid
        FireBase().addChat(list, chatName, chatPhoto, adminId){ chatId ->
            viewModelScope.launch {
                val chat = RoomDataBase.ChatEntity(
                    participants = list,
                    chatId = chatId,
                    chatName = chatName,
                    chatPhoto = chatPhoto,
                    adminId = adminId
                )
                localBase.getDao().insertChat(chat)
            }
        }
    }
    companion object{
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory{
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY]) as Application
                val database = (checkNotNull(extras[APPLICATION_KEY]) as App).database
                val chatRepository = ChatRepository(database)
                return ChatViewModel(application, database, chatRepository) as T            }
        }
    }
}
