package com.example.messenger.DataBase

import android.util.Log
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await

interface FireBaseService {
    val store: FirebaseFirestore
    suspend fun signIn(email: String, password: String): Result<String>
    suspend fun signUp(email: String, password: String): Result<String>
    suspend fun signUpInfo(uid: String, name: String, dateOfBirth: String, location: String)
    suspend fun signOut(): Result<Unit>
    suspend fun deleteAccount(email: String, password: String): Result<Unit>
    suspend fun addChat(list: List<String>, chatName: String?, chatPhoto: String?, adminId: String?): String
    suspend fun addDeviceToken(uid: String)
    suspend fun removeDeviceToken(uid: String)
    suspend fun getDeviceToken(): String?
}

class FireBase(
    private val auth: FirebaseAuth,
    override val store: FirebaseFirestore,
    private val messaging: FirebaseMessaging
) : FireBaseService {

    override suspend fun signUp(email: String, password: String): Result<String> {
        return try {
            val result = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("Не удалось получить UID")
            Result.success(uid)
        } catch (e: Exception) {
            Log.e("DataSign", "SignUp error: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun signIn(email: String, password: String): Result<String> {
        return try {
            val result = auth.signInWithEmailAndPassword(email, password).await()
            val uid = result.user?.uid ?: throw Exception("UID не найден")
            addDeviceToken(uid)
            Result.success(uid)
        } catch (e: Exception) {
            Log.e("DataSign", "SignIn error: ${e.message}")
            Result.failure(e)
        }
    }
    override suspend fun signUpInfo(uid: String, name: String,dateOfBirth: String, location: String) {
        val token = getDeviceToken()
        val data = mutableMapOf(
            "name" to name,
            "dateOfBirth" to dateOfBirth,
            "location" to location,
            "friendsId" to emptyList<String>(),
            "isOnline" to true,
            "lastSeen" to System.currentTimeMillis()
        )
        token?.let { data["tokens"] = FieldValue.arrayUnion(it) }

        try {
            store.collection("Users").document(uid).set(data, SetOptions.merge()).await()
        } catch (e: Exception) {
            Log.e("FireBase", "Error signUpInfo", e)
        }
    }

    override suspend fun signOut(): Result<Unit> {
        return try {
            val currentUser = auth.currentUser
            if (currentUser != null) {
                removeDeviceToken(currentUser.uid)
            }
            auth.signOut()
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("DataSign", "SignOut error: ${e.message}")
            Result.failure(e)
        }
    }
    override suspend fun deleteAccount(email: String, password: String): Result<Unit> {
        val user = auth.currentUser ?: return Result.failure(Exception("Пользователь не авторизован"))
        val credential = EmailAuthProvider.getCredential(email, password)
        return try {
            user.reauthenticate(credential).await()
            store.collection("Users").document(user.uid).update(
                "tokens", FieldValue.delete(),
                "isOnline", false,
                "lastSeen", System.currentTimeMillis(),
            ).await()

            user.delete().await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
    override suspend fun addChat(list:List<String>, chatName: String?, chatPhoto: String?, adminId: String?): String{
        val deterministicChatId = list.sorted().joinToString("_")
        val docRef = if (list.size==2) store.collection("Chats").document(deterministicChatId)
        else store.collection("Chats").document()
        val chatId = docRef.id
        try {
            val snapshot = docRef.get().await()
            if (snapshot.exists()) {
                return chatId
            } else {
                val chatData = mapOf(
                    "participants" to list,
                    "chatName" to chatName,
                    "chatPhoto" to chatPhoto,
                    "adminId" to adminId
                )
                docRef.set(chatData).await()

                list.forEach { userId ->
                    store.collection("Users")
                        .document(userId)
                        .collection("chatIds")
                        .document(chatId)
                        .set(mapOf("chatId" to chatId))
                        .await()
                }
                return chatId
            }
        } catch (e: Exception) {
            println("Error in addChat: ${e.message}")
            return ""
        }
    }

    override suspend fun getDeviceToken(): String? {
        return try {
            messaging.token.await()
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun addDeviceToken(uid: String) {
        val token = getDeviceToken() ?: return
        try {
            store.collection("Users").document(uid).set(
                mapOf("tokens" to FieldValue.arrayUnion(token)),
                SetOptions.merge()
            ).await()
        } catch (e: Exception) {
            Log.e("FireBase", "Error adding token", e)
        }
    }

    override suspend fun removeDeviceToken(uid: String) {
        val token = getDeviceToken() ?: return
        try {
            store.collection("Users").document(uid).update(
                "tokens", FieldValue.arrayRemove(token)
            ).await()
        } catch (e: Exception) {
            Log.e("FireBase", "Error removing token", e)
        }
    }
}

