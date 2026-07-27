package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long = 1,
    val sender: String, // "user" or "agent"
    val text: String,
    val codeSnippet: String? = null,
    val targetFilePath: String? = null,
    val proposedCode: String? = null,
    val isApplied: Boolean = false,
    val timestamp: Long = System.currentTimeMillis()
)
