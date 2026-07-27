package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ChatMessageDao {
    @Query("SELECT * FROM chat_messages ORDER BY timestamp ASC")
    fun getAllMessages(): Flow<List<ChatMessageEntity>>

    @Query("SELECT * FROM chat_messages WHERE projectId = :projectId ORDER BY timestamp ASC")
    fun getMessagesForProject(projectId: Long): Flow<List<ChatMessageEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertMessage(message: ChatMessageEntity): Long

    @Update
    suspend fun updateMessage(message: ChatMessageEntity)

    @Query("UPDATE chat_messages SET text = :text, targetFilePath = :targetFilePath, proposedCode = :proposedCode WHERE id = :id")
    suspend fun updateMessageContent(id: Long, text: String, targetFilePath: String?, proposedCode: String?)

    @Query("UPDATE chat_messages SET isApplied = :isApplied WHERE id = :id")
    suspend fun setMessageApplied(id: Long, isApplied: Boolean = true)

    @Query("DELETE FROM chat_messages WHERE projectId = :projectId")
    suspend fun clearHistoryForProject(projectId: Long)

    @Query("DELETE FROM chat_messages")
    suspend fun clearHistory()
}
