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
import com.example.messenger.DataBase.FireBaseService
import com.example.messenger.DataBase.RoomDataBase
import com.example.messenger.Screens.SettingsRepository
import com.example.messenger.Screens.SettingsRepositoryImpl
import com.example.messenger.repository.ChatRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainViewModel( private val localBase: RoomDataBase.MainDb,
                     private val chatRepository: ChatRepository,
                     private val fireBase: FireBaseService,
                     private val settingsRepository: SettingsRepository
) : ViewModel() {
    var loggedInUser by mutableStateOf<User>(User())
    private var userListener: ListenerRegistration? = null

    var isDarkTheme by mutableStateOf(false)
    var textSizeScale by mutableStateOf(1f)
    var currentLocale by mutableStateOf(Locale.getDefault())

    init {
        viewModelScope.launch {
            settingsRepository.themeFlow.collect { isDarkTheme = it }
        }
        viewModelScope.launch {
            settingsRepository.textSizeFlow.collect { textSizeScale = it }
        }
        viewModelScope.launch {
            settingsRepository.languageFlow.collect {currentLocale = Locale.forLanguageTag(it)}
        }
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid != null) {
            observeLocalUser(uid)
            syncUserWithFirestore(uid)
        }
    }

    fun updateTheme() {
        viewModelScope.launch {
            settingsRepository.updateTheme(!isDarkTheme)
        }
    }

    fun updateTextSize(newSize: Float) {
        viewModelScope.launch {
            settingsRepository.updateTextSize(newSize)
        }
    }

    fun updateLanguage(newLocale: Locale) {
        viewModelScope.launch {
            settingsRepository.updateLanguage(newLocale.toLanguageTag())
        }
    }

    private fun observeLocalUser(uid: String) {
        viewModelScope.launch {
            localBase.getDao().flowUser(uid).collect { entity ->
                entity?.let {
                    loggedInUser = User(it)
                }
            }
        }
    }

    private fun syncUserWithFirestore(uid: String) {
        userListener = chatRepository.listenForUsersByList(listOf(uid)) { userList ->
            viewModelScope.launch(Dispatchers.IO) {
                userList.forEach { remoteUser ->
                    localBase.getDao().insertUser(RoomDataBase.UserEntity(remoteUser))
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
        val collection = fireBase.store.collection("Users")
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
                        fireBase.store
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

    suspend fun deleteAccount(email: String, password: String): Result<Unit> {
        val result = fireBase.deleteAccount(email, password)
        if (result.isSuccess) {
            clearDatabase()
        }

        return result
    }

    suspend fun signIn(email: String, password: String): Result<String> {
        val result = fireBase.signIn(email, password)
        if (result.isSuccess) {
            setUserOnline(true)
            val uid = FirebaseAuth.getInstance().currentUser?.uid
            if (uid != null) {
                observeLocalUser(uid)
                syncUserWithFirestore(uid)
            }
        }
        return result
    }
    suspend fun signUp(email: String, password: String,  name: String, dateOfBirth: String, location: String): Result<String> {
        val result = fireBase.signUp(email, password)
        if (result.isSuccess) {
            val uid = result.getOrNull()
            if (uid != null) {
                fireBase.signUpInfo(uid, name, dateOfBirth, location)
                observeLocalUser(uid)
                syncUserWithFirestore(uid)
            }
        }
        return result
    }

    suspend fun signOut(): Result<Unit> {
        val result = fireBase.signOut()
        if (result.isSuccess) {
            setUserOnline(false)
            clearDatabase()
        }
        return result
    }

    override fun onCleared() {
        super.onCleared()
        userListener?.remove()
    }

    companion object{
        val factory: ViewModelProvider.Factory = object : ViewModelProvider.Factory{
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(
                modelClass: Class<T>,
                extras: CreationExtras): T {
                val application = checkNotNull(extras[APPLICATION_KEY]) as App
                val database = application.database
                val firebaseService = FireBase(
                    auth = FirebaseAuth.getInstance(),
                    store = FirebaseFirestore.getInstance(),
                    messaging = FirebaseMessaging.getInstance()
                )
                val chatRepository = ChatRepository(database, firebaseService)
                val settingsRepository = SettingsRepositoryImpl(application)
                return MainViewModel(database, chatRepository, firebaseService, settingsRepository) as T
            }
        }
    }
}

class App: Application(){
    val database by lazy { RoomDataBase.MainDb.getDb(this) }
}
