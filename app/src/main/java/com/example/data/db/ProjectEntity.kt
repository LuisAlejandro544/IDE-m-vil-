package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class ProjectEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val description: String,
    val framework: String, // e.g. "Web (HTML/JS/CSS)", "Android (Kotlin Compose)", "Rust Server", "C++ JNI", "Node.js REST API"
    val iconEmoji: String = "⚡",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
