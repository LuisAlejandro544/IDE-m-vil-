package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface DiagnosticLogDao {
    @Query("SELECT * FROM diagnostic_logs WHERE projectId = :projectId ORDER BY timestamp DESC")
    fun getLogsForProject(projectId: Long): Flow<List<DiagnosticLogEntity>>

    @Query("SELECT * FROM diagnostic_logs WHERE projectId = :projectId AND level = :level ORDER BY timestamp DESC")
    fun getLogsByLevel(projectId: Long, level: String): Flow<List<DiagnosticLogEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLog(log: DiagnosticLogEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertLogs(logs: List<DiagnosticLogEntity>)

    @Query("DELETE FROM diagnostic_logs WHERE projectId = :projectId")
    suspend fun clearLogsForProject(projectId: Long)

    @Query("DELETE FROM diagnostic_logs WHERE projectId = :projectId AND source = 'Linter'")
    suspend fun clearLinterLogs(projectId: Long)
}
