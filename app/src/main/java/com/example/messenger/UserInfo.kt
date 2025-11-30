package com.example.messenger

import com.example.messenger.DataBase.RoomDataBase

data class User (
    val uid: String="",
    val name:String = "",
    val dateOfBirth: String = "",
    val location:String="",
    val friends: List <String> =  emptyList(),
    val isOnline: Boolean = false,
    val lastSeen: String = "",
    val localAvatarPath: String? = null,
){
    constructor(user: RoomDataBase.UserEntity) : this(
        uid = user.uid,
        name = user.name,
        dateOfBirth = user.dateOfBirth,
        location = user.location,
        friends = user.friends,
        isOnline = user.isOnline,
        lastSeen = user.lastSeen,
        localAvatarPath = user.localAvatarPath,
    )
}

