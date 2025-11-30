package com.example.messenger.DataBase

import androidx.navigation.NavController
import com.example.messenger.Screen
import com.example.messenger.User
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class FireBase {
    val store = Firebase.firestore

    fun addChat(list:List<String>, chatName: String?, chatPhoto: String?, adminId: String?, onResult: (String) -> Unit){
        val docRef = store.collection("Chats").document()
        docRef.set(mapOf(
            "participants" to list,
            "chatName" to chatName,
            "chatPhoto" to chatPhoto,
            "adminId" to adminId
        ))
            .addOnCompleteListener {
                if(it.isSuccessful){
                    store.collection("Users")
                        .document(list[0])
                        .collection("chatId")
                        .document().set(mapOf("chatId" to docRef.id))
                    store.collection("Users")
                        .document(list[1])
                        .collection("chatId")
                        .document().set(mapOf("chatId" to docRef.id))
                    onResult(docRef.id)
                }
            }
            .addOnFailureListener { println("addChat error") }
    }
    fun addFriend(loggedInUserUid: String, uid: String){
        store.collection("Users")
            .document(loggedInUserUid)
            .collection("friendsId")
            .document(uid).set(mapOf("chatUid" to "cwvwfwfwdwcwd"))
    }

    fun signUpInfo(uid: String, name: String, location:String) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                store.collection("Users")
                    .document(uid).set(
                        mapOf(
                            "name" to name,
                            "location" to  location,
                            "token" to token
                        )
                    )
            }
        }
    }

    fun getInfo(uid: String, onResult: (User?) -> Unit) {
        store.collection("Users")
            .document(uid).get()
            .addOnSuccessListener { snapshot ->
                snapshot?.let { doc ->
                    val data = doc.data ?: emptyMap<String, Any>()
                    val friends = data["friendsId"] as? List<String> ?: emptyList()
                    onResult(
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
                    )
                }
            }
            .addOnFailureListener {
                onResult(null)
            }
    }

}

class DataSign {
    val auth = Firebase.auth
    fun signUp(
        email: String,
        password: String,
        uid:(String?) -> Unit
    ){
        if(email.isBlank()||password.isBlank()){

            return
        }
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(it.isSuccessful) uid(it.result.user?.uid)
            }
            .addOnFailureListener {
                println(it.message)
            }

    }
    fun signIn(email: String,
               password: String,
               uid:(String?) -> Unit
    ){
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener {
                if(it.isSuccessful) uid(it.result.user?.uid)
            }
            .addOnFailureListener {
                println("       error")
            }
    }
    fun signOut(navController: NavController){
        auth.signOut()
        navController.navigate(Screen.LogIn.route)
    }
    fun deleteAccount(navController: NavController, email: String,password: String){
        val credential = EmailAuthProvider.getCredential(email, password)
        auth.currentUser?.reauthenticate(credential)?.addOnCompleteListener {
            if(it.isSuccessful)
                auth.currentUser?.delete()?.addOnCompleteListener {
                    navController.navigate(Screen.LogIn.route)
                }
            else println("error delete")
        }
            ?.addOnFailureListener {
                println(it.message)
            }
    }
}