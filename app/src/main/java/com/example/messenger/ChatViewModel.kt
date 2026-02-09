package com.example.messenger

import android.app.Application
import android.content.Context
import android.util.Base64
import android.util.Log
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
import com.google.firebase.Timestamp
import com.google.firebase.firestore.ListenerRegistration
import kotlinx.coroutines.Dispatchers
import java.text.SimpleDateFormat
import java.util.*

class ChatViewModel(
    application: Application,
    val localBase: RoomDataBase.MainDb,
    val chatRepository: ChatRepository
) : AndroidViewModel(application)  {
    private val activeListeners = mutableListOf<ListenerRegistration>()
    private val messageListeners = mutableListOf<ListenerRegistration>()
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
            startFirebaseSync()
            observeLocalDatabase()
        }
    }

    private fun startFirebaseSync() {
        val context = getApplication<Application>().applicationContext

        val chatsReg = chatRepository.listenForChats(loggedInUser!!) { chatEntities ->
            viewModelScope.launch(Dispatchers.IO) {
                chatEntities.forEach { localBase.getDao().insertChat(it) }
            }
            messageListeners.forEach { it.remove() }
            messageListeners.clear()
            val currentChats = chatEntities.map { Chat(it) }
            val msgRegs = chatRepository.listenForAllMessages(currentChats, context) { messages ->
                viewModelScope.launch(Dispatchers.IO) {
                    localBase.getDao().insertMessages(messages)
                }
            }
            messageListeners.addAll(msgRegs)
            val uids = buildList {
                chatEntities.forEach { addAll(it.participants) }
                loggedInUser?.uid?.let { add(it) }
            }.distinct()
            if (uids.isNotEmpty()) {
                val usersReg = chatRepository.listenForUsersByList(uids) { userList ->
                        viewModelScope.launch(Dispatchers.IO) {
                            userList.forEach { remoteUser ->
                                updateAvatar(remoteUser)
                                localBase.getDao().insertUser(RoomDataBase.UserEntity(remoteUser))
                            }
                        }
                    }
                messageListeners.add(usersReg)
            }
        }
        activeListeners.add(chatsReg)
    }

    private fun observeLocalDatabase() {
        viewModelScope.launch {
            localBase.getDao().getChatsWithMessages()
                .map { list -> chatRepository.sortChatsByLastMessage(list.map { Chat(it) }) }
                .collect { mappedChats ->
                    chats = mappedChats
                }
        }
        viewModelScope.launch {
            localBase.getDao().getUsers()
                .map { list -> list.map { User(it) }.associateBy { it.uid } }
                .collect { mappedUsers ->
                    users = mappedUsers
                }
        }
    }

    suspend fun updateAvatar(user:User) {
        var avatarUpdatedAt = 0L
        var newAvatarUpdatedAt = 0L
        val localUser = localBase.getDao().getUser(user.uid)
        try {
            newAvatarUpdatedAt =
                user.localAvatarPath!!.substringAfter("?").toLong()
            avatarUpdatedAt =
                localUser!!.localAvatarPath!!.substringAfter("?").toLong()
        } catch (e: Exception) {
        }
        if (newAvatarUpdatedAt != avatarUpdatedAt) {
            val photo = FireBase().store
                .collection("Photo")
                .document("avatars")
                .collection("Users")
                .document(user.uid)
                .get()
                .await()
                .getString("base64String")
            if (!photo.isNullOrEmpty()) {
                try {
                    val context = getApplication<Application>().applicationContext
                    val bytes = Base64.decode(photo, Base64.DEFAULT)
                    val file = File(context.filesDir, user.localAvatarPath!!)
                    file.outputStream().use { stream -> stream.write(bytes) }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
    suspend fun sendMessage(context: Context, messageText: String, fileName: String?) {
        val currentChat = openedChat ?: return
        val currentUser = loggedInUser ?: return
        val chatId = currentChat.chatId

        var photo = ""
        if (!fileName.isNullOrEmpty()) {
            try {
                val file = File(context.filesDir, fileName)
                if (file.exists()) {
                    val bytes = file.readBytes()
                    photo = Base64.encodeToString(bytes, Base64.DEFAULT)
                }
            } catch (e: Exception) {
                Log.e("SendMessage", "Ошибка кодирования фото: ${e.message}")
            }
        }
        val messageId = UUID.randomUUID().toString()
        val message = RoomDataBase.MessageEntity(
            messageId = messageId,
            chatId = chatId,
            senderUid = currentUser.uid,
            messageText = messageText,
            status = MessageStatus.PENDING,
            dateOfSend = System.currentTimeMillis(),
            photoName = fileName
        )
        localBase.getDao().insertMessage(message)
        try {
            FireBase().store.collection("Chats")
                .document(chatId)
                .collection("Messages")
                .document(messageId)
                .set(mapOf(
                    "senderUid" to currentUser.uid,
                    "messageText" to messageText,
                    "status" to MessageStatus.SENT.name,
                    "dateOfSend" to com.google.firebase.firestore.FieldValue.serverTimestamp(),
                    "photoName" to fileName,
                    "photo" to photo
                ))
                .await()
        } catch (e: Exception) {
            Log.e("SendMessage", "Ошибка отправки в Firebase: ${e.message}")
        }
    }

    suspend fun readMessage(message: Message) {
        FireBase().store.collection("Chats")
            .document(openedChat!!.chatId)
            .collection("Messages")
            .document(message.messageId)
            .update("status", MessageStatus.READ.name)
            .await()
        localBase.getDao().updateMessageStatus(message.messageId, MessageStatus.READ)
    }

    override fun onCleared() {
        super.onCleared()
        activeListeners.forEach { it.remove() }
        messageListeners.forEach { it.remove() }
        activeListeners.clear()
        messageListeners.clear()
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
