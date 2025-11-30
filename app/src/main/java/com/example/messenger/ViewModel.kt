package com.example.messenger

import android.app.Application
import android.content.Context
import android.net.Uri
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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.lang.Exception
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel( val localBase: RoomDataBase.MainDb) : ViewModel() {
    val fireBase = FireBase()

    var loggedInUser= User()
    //var selectedChat = Chat()
    var currentLocale by mutableStateOf(Settings.language.value)
        private set
    init {
        viewModelScope.launch {
            if (FirebaseAuth.getInstance().currentUser != null) {
                if (localBase.getDao().getUser(FirebaseAuth.getInstance().currentUser!!.uid) != null)
                    loggedInUser = User(localBase.getDao().getUser(FirebaseAuth.getInstance().currentUser!!.uid)!!)
                else {
                    getUserFromFirebase(FirebaseAuth.getInstance().currentUser!!.uid)
                    loggedInUser = User(localBase.getDao().getUser(FirebaseAuth.getInstance().currentUser!!.uid)!!)
                }
            }
        }
    }
    fun toggleLanguage(context: Context) {
        currentLocale = Settings.language.value
        val config = context.resources.configuration
        Locale.setDefault(Settings.language.value)
        config.setLocale(Settings.language.value)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)
    }

    suspend fun getUserFromFirebase(uid: String) {
        try {
            val snapshot = FirebaseFirestore.getInstance()
                .collection("Users")
                .document(uid)
                .get()
                .await()

            val data = snapshot.data!!

            val friends = data["friendsId"] as? List<String> ?: emptyList()
            val user = User(
                uid = snapshot.id,
                name = data["name"] as? String ?: "",
                dateOfBirth = data["dateOfBirth"] as? String ?: "",
                location = data["location"] as? String ?: "",
                friends = friends,
                isOnline = data["isOnline"] as? Boolean ?: false,
                lastSeen = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(data["dateOfSend"] as? Long ?: 0L)),
                localAvatarPath = data["localAvatarPath"] as? String?: ""
            )
            localBase.getDao().insertUser(RoomDataBase.UserEntity(user))
        } catch (e: Exception) {
            e.printStackTrace()
            User()
        }
    }

    fun addUser(uid: String, name: String, location: String, friends: List<String>)= viewModelScope.launch {
        val user = RoomDataBase.UserEntity(
            uid = uid,
            name = name,
            location = location,
            friends = friends
        )
        localBase.getDao().insertUser(user)
    }

    fun addChat(uid: String, chatName: String? = null, chatPhoto: String? = null, adminId: String? = null) {
        val list = listOf(uid, loggedInUser.uid)
        fireBase.addChat(list, chatName, chatPhoto, adminId){ chatId ->
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

    fun getRandomUsers(onResult: (List<User>) -> Unit) {
        fireBase.store.collection("Users")
            .get()
            .addOnSuccessListener { snapshot ->
                val allUsers = snapshot.documents.mapNotNull { doc ->
                    val data = doc.data ?: return@mapNotNull null
                    val friends = data["friendsId"] as? List<String> ?: emptyList()
                    User(
                        uid = doc.id,
                        name = data["name"] as? String ?: "",
                        dateOfBirth = data["dateOfBirth"] as? String ?: "",
                        location = data["location"] as? String ?: "",
                        friends = friends,
                        isOnline = data["isOnline"] as? Boolean ?: false,
                        lastSeen = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(doc.getLong("dateOfSend") ?: 0L)),
                        localAvatarPath = data["localAvatarPath"] as? String?: ""
                    )
                }
                // Перемешиваем и берём 10 случайных
                val randomUsers = allUsers.shuffled().take(10)
                onResult(randomUsers)
            }
            .addOnFailureListener {
                onResult(emptyList())
            }
    }

    fun getUsers(uid: String = loggedInUser.uid, onResult: (List<User>) -> Unit)=viewModelScope.launch {
        val users = localBase.getDao().getAllUsersExceptOwner(uid).map { User(it) }
        onResult(users)
    }

    fun addFriend(userUid: String, friendUid: String)=viewModelScope.launch {
        val user = localBase.getDao().getUser(userUid)
        if (user != null) {
            val updatedFriends = user.friends.toMutableList().apply {
                if (!contains(friendUid)) add(friendUid)
            }
            val updatedUser = user.copy(friends = updatedFriends)
            localBase.getDao().updateUser(updatedUser)
            fireBase.addFriend(userUid,friendUid)
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
                return MainViewModel(database) as T
            }
        }
    }
}

class App: Application(){
    val database by lazy { RoomDataBase.MainDb.getDb(this) }
}
