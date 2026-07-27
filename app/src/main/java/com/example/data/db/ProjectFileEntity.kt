package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "project_files")
data class ProjectFileEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long = 1,
    val name: String,
    val path: String, // e.g. "/index.html" or "/src/style.css"
    val content: String,
    val extension: String,
    val isDirectory: Boolean = false,
    val parentPath: String = "/",
    val updatedAt: Long = System.currentTimeMillis()
)
