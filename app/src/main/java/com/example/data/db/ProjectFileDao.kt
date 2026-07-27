package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ProjectFileDao {
    @Query("SELECT * FROM project_files ORDER BY isDirectory DESC, name ASC")
    fun getAllFiles(): Flow<List<ProjectFileEntity>>

    @Query("SELECT * FROM project_files WHERE projectId = :projectId ORDER BY isDirectory DESC, name ASC")
    fun getFilesForProject(projectId: Long): Flow<List<ProjectFileEntity>>

    @Query("SELECT * FROM project_files WHERE projectId = :projectId AND path = :path LIMIT 1")
    suspend fun getFileByPathAndProject(projectId: Long, path: String): ProjectFileEntity?

    @Query("SELECT * FROM project_files WHERE path = :path LIMIT 1")
    suspend fun getFileByPath(path: String): ProjectFileEntity?

    @Query("SELECT * FROM project_files WHERE id = :id LIMIT 1")
    suspend fun getFileById(id: Long): ProjectFileEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFile(file: ProjectFileEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFiles(files: List<ProjectFileEntity>)

    @Update
    suspend fun updateFile(file: ProjectFileEntity)

    @Query("UPDATE project_files SET content = :content, updatedAt = :updatedAt WHERE projectId = :projectId AND path = :path")
    suspend fun updateFileContentByProjectAndPath(projectId: Long, path: String, content: String, updatedAt: Long = System.currentTimeMillis())

    @Query("UPDATE project_files SET content = :content, updatedAt = :updatedAt WHERE path = :path")
    suspend fun updateFileContentByPath(path: String, content: String, updatedAt: Long = System.currentTimeMillis())

    @Query("DELETE FROM project_files WHERE path LIKE '%.md' OR extension = 'md'")
    suspend fun deleteMarkdownFiles()

    @Query("DELETE FROM project_files WHERE id = :id")
    suspend fun deleteFileById(id: Long)

    @Query("DELETE FROM project_files WHERE projectId = :projectId AND path = :path")
    suspend fun deleteFileByProjectAndPath(projectId: Long, path: String)

    @Query("DELETE FROM project_files WHERE path = :path")
    suspend fun deleteFileByPath(path: String)

    @Query("DELETE FROM project_files WHERE projectId = :projectId AND (path = :path OR path LIKE :pathPrefix)")
    suspend fun deletePathAndChildrenByProject(projectId: Long, path: String, pathPrefix: String)

    @Query("DELETE FROM project_files WHERE path = :path OR path LIKE :pathPrefix")
    suspend fun deletePathAndChildren(path: String, pathPrefix: String)

    @Query("DELETE FROM project_files WHERE projectId = :projectId")
    suspend fun deleteFilesForProject(projectId: Long)

    @Query("SELECT COUNT(*) FROM project_files WHERE projectId = :projectId")
    suspend fun getFileCountForProject(projectId: Long): Int

    @Query("SELECT COUNT(*) FROM project_files")
    suspend fun getFileCount(): Int
}
