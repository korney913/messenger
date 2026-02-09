package com.example.messenger

import android.app.Application
import android.content.Context
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProvider.AndroidViewModelFactory.Companion.APPLICATION_KEY
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.CreationExtras
import com.example.messenger.DataBase.FireBase
import com.example.messenger.DataBase.RoomDataBase
import com.example.messenger.Screens.Settings
import com.example.messenger.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel( val localBase: RoomDataBase.MainDb,
                     val chatRepository: ChatRepository
) : ViewModel() {
    val fireBase = FireBase()
    var loggedInUser by mutableStateOf<User>(User())
    init {
        viewModelScope.launch {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null) {
                chatRepository.listenForUsersByList(listOf(user.uid)){
                    viewModelScope.launch {
                        it.forEach { user ->
                            localBase.getDao().insertUser(RoomDataBase.UserEntity(user))
                        }
                        localBase.getDao().flowUser(user.uid)
                            .collect { mappedUsers ->
                                loggedInUser = User(mappedUsers!!)
                            }
                    }
            }
        }
    }
    }

    suspend fun addChat(listUid: List<String>, chatName: String? = null, chatPhoto: String? = null, adminId: String? = null): String {
        val list = listUid + loggedInUser.uid
        if(list.size==2) {
            val chatId = list.sorted().joinToString("_")
            val localChats = localBase.getDao().getChats()
            if (localChats.any { it.chatId == chatId }) { return chatId }
        }
        val newChatId = fireBase.addChat(list, chatName, chatPhoto, adminId)
        if (newChatId.isNotEmpty()) {
            val chat = RoomDataBase.ChatEntity(
                participants = list,
                chatId = newChatId,
                chatName = chatName,
                chatPhoto = chatPhoto,
                adminId = adminId
                )
            localBase.getDao().insertChat(chat)
            }
        return newChatId
    }

    suspend fun getTenUsers(uids: List<String> = emptyList(), startUid: String? = null):
            List<User> = coroutineScope {
        val collection = FireBase().store.collection("Users")
        val query = if (uids.isEmpty()) {
            if (startUid != null) collection.orderBy(FieldPath.documentId()).limit(10)
                .startAfter(startUid) else collection.orderBy(FieldPath.documentId()).limit(10)
        } else {
            collection.whereIn(FieldPath.documentId(), uids)
        }
        try {
            val snapshot = query.get().await()
            val userTasks = snapshot.documents.map { doc ->
                async {
                    val data = doc.data ?: return@async null
                    val uid = doc.id
                    val friends = data["friendsId"] as? List<String> ?: emptyList()
                    val base64Photo = try {
                        FireBase().store
                            .collection("Photo")
                            .document("avatars")
                            .collection("Users")
                            .document(uid)
                            .get()
                            .await()
                            .getString("base64String")
                    } catch (e: java.lang.Exception) {
                        null
                    }
                    User(
                        uid = uid,
                        name = data["name"] as? String ?: "",
                        dateOfBirth = data["dateOfBirth"] as? String ?: "",
                        city = data["location"] as? String ?: "",
                        friends = friends,
                        isOnline = data["isOnline"] as? Boolean ?: false,
                        lastSeen = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
                            .format(Date(data["lastSeen"] as? Long ?: 0L)),
                        localAvatarPath = base64Photo ?: ""
                    )
                }
            }
            userTasks.awaitAll().filterNotNull()
        } catch (e: java.lang.Exception) {
            println("Error fetching users: ${e.message}")
            emptyList()
        }
    }
    suspend fun addFriend(userUid: String, friendUid: String) {
        val user = localBase.getDao().getUser(userUid)
        if (user != null) {
            val updatedFriends = user.friends.toMutableList().apply {
                if (!contains(friendUid)) add(friendUid)
            }
            val updatedUser = user.copy(friends = updatedFriends)
            localBase.getDao().updateUser(updatedUser)
            fireBase.store.collection("Users").document(loggedInUser.uid).set(
                mapOf("friendsId" to FieldValue.arrayUnion(friendUid)),
                SetOptions.merge()
            )
                .await()
            loggedInUser = User(updatedUser)
        }
    }

    suspend fun removeFriend(userUid: String, friendUid: String) {
        val user = localBase.getDao().getUser(userUid)
        if (user != null) {
            val updatedFriends = user.friends.toMutableList().apply {
                remove(friendUid)            }
            val updatedUser = user.copy(friends = updatedFriends)
            localBase.getDao().updateUser(updatedUser)
            fireBase.store.collection("Users").document(loggedInUser.uid).update(
                "friendsId", FieldValue.arrayRemove(friendUid)
            )
                .await()
            loggedInUser = User(updatedUser)
        }
    }

    fun clearDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            localBase.clearAllTables()
        }
    }

    fun changeAvatar(context: Context,localAvatarPath: String)=viewModelScope.launch {
        val user = localBase.getDao().getUser( loggedInUser.uid)!!
        val file = File(context.filesDir, localAvatarPath)
        val bytes = file.readBytes()
        val base64String = Base64.encodeToString(bytes, Base64.DEFAULT)
        val updatedUser = user.copy(
            localAvatarPath = localAvatarPath,
        )
        println(base64String.length)
        loggedInUser = User(updatedUser)
        localBase.getDao().updateUser(updatedUser)
        fireBase.store.collection("Photo")
            .document("avatars")
            .collection("Users")
            .document(loggedInUser.uid)
            .set(mapOf(
                "base64String" to base64String,
            ))
            .addOnSuccessListener {
                fireBase.store.collection("Users")
                    .document(loggedInUser.uid)
                    .update( "localAvatarPath", updatedUser.localAvatarPath)
            }
    }

    companion object{
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory{
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras): T {
                val database = (checkNotNull(extras[APPLICATION_KEY]) as App).database
                val chatRepository = ChatRepository(database)
                return MainViewModel(database, chatRepository) as T
            }
        }
    }
}

class App: Application(){
    val database by lazy { RoomDataBase.MainDb.getDb(this) }
}
