package com.example.messenger.DataBase

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.Relation
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.Transaction
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import androidx.room.Update
import com.example.messenger.MessageStatus
import com.example.messenger.User
import kotlinx.coroutines.flow.Flow
import kotlin.String

class RoomDataBase {
    class Converters {
        @TypeConverter
        fun fromStringList(list: List<String>?): String = list?.joinToString(",") ?: ""

        @TypeConverter
        fun toStringList(data: String?): List<String> =
            data?.split(",")?.filter { it.isNotEmpty() } ?: emptyList()
    }

    @Database(entities = [UserEntity::class, ChatEntity::class, MessageEntity::class], version = 1)
    @TypeConverters(Converters::class)
    abstract class MainDb : RoomDatabase() {
        abstract fun getDao(): UserDao

        companion object {
            fun getDb(context: Context): MainDb {
                //context.deleteDatabase("test.db")
                return Room.databaseBuilder(
                    context,
                    MainDb::class.java,
                    "test.db"
                )
                    .build()
            }
        }
    }

    @Entity(tableName = "users")
    data class UserEntity(
        @PrimaryKey val uid: String,
        val name: String,
        val location: String = "",
        val dateOfBirth: String = "",
        val friends: List<String> = emptyList(),
        val isOnline: Boolean = false,
        val lastSeen: String = "",
        val localAvatarPath: String? = null,
    ){
        constructor(user: User) : this(
            uid = user.uid,
            name = user.name,
            location = user.city,
            dateOfBirth = user.dateOfBirth,
            friends = user.friends,
            isOnline = user.isOnline,
            lastSeen = user.lastSeen,
            localAvatarPath = user.localAvatarPath,
        )
    }

    @Entity(tableName = "chats")
    data class ChatEntity(
        @PrimaryKey val chatId: String = "",
        val participants: List<String> = emptyList(),
        val chatName: String? = null,
        val chatPhoto: String? = null,
        val adminId: String? = null
    )

    @Entity(tableName = "messages")
    data class MessageEntity(
        @PrimaryKey val messageId: String,
        val chatId: String,
        val senderUid: String,
        val messageText: String,
        val status: MessageStatus,
        val dateOfSend: Long,
        val photoName: String?
    )

    data class ChatWithMessages(
        @Embedded val chat: ChatEntity,

        @Relation(
            parentColumn = "chatId",
            entityColumn = "chatId"
        )
        val listMessage: List<MessageEntity>
    )

    @Dao
    interface UserDao {
        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertUser(user: UserEntity)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertChat(chat: ChatEntity)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertMessage(message: MessageEntity)

        @Insert(onConflict = OnConflictStrategy.REPLACE)
        suspend fun insertMessages(messages: List<MessageEntity>)

        @Query("SELECT * FROM users WHERE uid = :uid")
        suspend fun getUser(uid: String): UserEntity?

        @Query("SELECT * FROM users WHERE uid = :uid")
        fun flowUser(uid: String): Flow<UserEntity?>

        @Query("SELECT * FROM users")
        fun getUsers(): Flow<List<UserEntity>>

        @Query("SELECT * FROM chats")
        suspend fun getChats(): List<ChatEntity>

        @Query("SELECT * FROM users WHERE uid != :ownerUid")
        suspend fun getAllUsersExceptOwner(ownerUid: String): List<UserEntity>

        @Transaction
        @Query("SELECT * FROM chats WHERE chatId = :chatId")
         fun getChatsWithMessages(chatId: String): Flow<ChatWithMessages>

        @Transaction
        @Query("SELECT * FROM chats")
         fun getChatsWithMessages(): Flow<List<ChatWithMessages>>

        @Update
        suspend fun updateUser(user: UserEntity)

        @Update
        suspend fun updateMessage(messageEntity: MessageEntity)

        @Query("UPDATE messages SET status = :status WHERE messageId = :messageId")
        suspend fun updateMessageStatus(messageId: String, status: MessageStatus)
    }
}