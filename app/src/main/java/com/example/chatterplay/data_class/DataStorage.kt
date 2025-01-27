package com.example.chatterplay.data_class

import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters
import com.google.common.reflect.TypeToken
import com.google.gson.Gson

@Entity(tableName = "user_profile")

data class UserProfileStorage(
    @PrimaryKey val userId: String = "",
    val fname: String = "",
    val lname: String = "",
    val gender: String = "",
    val dob: DateOfBirth = DateOfBirth(month = "", day = "", year = ""),
    val age: String = "",
    val location: String = "",
    val imageUrl: String = "",
    val about: String = "",
    val pending: String = "NotPending",
    val selectedProfile: String = "self",
    val gameRoomId: String = "0"
)
@Dao
interface UserProfileDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertUserProfile(profile: UserProfileStorage)

    @Query("select * from user_profile where userId = :userId")
    suspend fun getUserProfile(userId: String): UserProfileStorage?
}






@Entity(tableName = "friends")
data class FriendsListStorage(
    @PrimaryKey val friendId: String,
    val userId: String,
    val name: String
)
@Dao
interface FriendsDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFriend(friend: FriendsListStorage)

    @Query("SELECT * FROM friends WHERE userId = :userId")
    suspend fun getFriendsForUser(userId: String): List<FriendsListStorage>
}








@Entity(tableName = "chat_rooms")

data class ChatRoomStorage(
    @PrimaryKey val roomId: String = "",
    val roomName: String = "",
    val members: List<String> = listOf()
)
@Dao
interface ChatRoomDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertChatRoom(chatRoom: ChatRoomStorage)

    @Query("SELECT * FROM chat_rooms WHERE roomId = :userId")
    suspend fun getChatRoomsForUser(userId: String): List<ChatRoomStorage>
}


class StringListConverter {

    @TypeConverter
    fun fromListToString(list: List<String>?): String {
        return Gson().toJson(list) // Convert the list to a JSON string
    }

    @TypeConverter
    fun fromStringToList(data: String?): List<String> {
        val listType = object : TypeToken<List<String>>() {}.type
        return Gson().fromJson(data, listType) ?: emptyList() // Convert JSON string back to list
    }
}

class DateOfBirthConverter {

    @TypeConverter
    fun fromDateOfBirth(dateOfBirth: DateOfBirth): String {
        return Gson().toJson(dateOfBirth) // Convert object to JSON string
    }

    @TypeConverter
    fun toDateOfBirth(data: String): DateOfBirth {
        val type = object : TypeToken<DateOfBirth>() {}.type
        return Gson().fromJson(data, type) // Convert JSON string to object
    }
}


@Database(entities = [UserProfileStorage:: class, FriendsListStorage::class, ChatRoomStorage::class], version =1)
@TypeConverters(StringListConverter::class, DateOfBirthConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userProfileDao(): UserProfileDao
    abstract fun friendsDao() : FriendsDao
    abstract fun chatRoomDao() : ChatRoomDao
}




// Repository
/*
val db = Room.databaseBuilder(
    context,
    AppDatabase::class.java,
    "chat_app_database"
).build()

val userProfileDao = db.UserProfileDao()
val friendsListDao = db.FriendsDao()
val chatRoomDao = db.ChatRoomDao()

 */