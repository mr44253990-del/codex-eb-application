package com.ebchat.data

import android.content.Context
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Room
import androidx.room.RoomDatabase
import kotlinx.coroutines.flow.Flow

@Dao
interface CachedMessageDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(message: CachedMessage)

    @Query("SELECT * FROM cached_messages WHERE chatId = :chatId ORDER BY createdAt ASC")
    fun observeChat(chatId: String): Flow<List<CachedMessage>>

    @Query("SELECT * FROM cached_messages WHERE pendingSync = 1 ORDER BY createdAt ASC")
    suspend fun pendingSync(): List<CachedMessage>
}

@Database(entities = [CachedMessage::class], version = 1, exportSchema = false)
abstract class EBChatDatabase : RoomDatabase() {
    abstract fun cachedMessageDao(): CachedMessageDao
}

object LocalCache {
    lateinit var database: EBChatDatabase
        private set

    fun init(context: Context) {
        database = Room.databaseBuilder(
            context.applicationContext,
            EBChatDatabase::class.java,
            "eb_chat.db",
        ).build()
    }
}
