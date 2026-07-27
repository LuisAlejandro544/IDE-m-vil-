package com.example.data.db

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "diagnostic_logs")
data class DiagnosticLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val projectId: Long = 1,
    val level: String, // "ERROR", "WARNING", "INFO", "SUCCESS"
    val source: String, // "Linter", "Compiler", "Rust Server", "AI Agent", "C++ Engine"
    val message: String,
    val filePath: String? = null,
    val lineNumber: Int? = null,
    val timestamp: Long = System.currentTimeMillis()
)
