package com.example.data.repository

import com.example.data.db.ChatMessageDao
import com.example.data.db.ChatMessageEntity
import com.example.data.db.DiagnosticLogDao
import com.example.data.db.DiagnosticLogEntity
import com.example.data.db.ProjectDao
import com.example.data.db.ProjectEntity
import com.example.data.db.ProjectFileDao
import com.example.data.db.ProjectFileEntity
import com.example.data.repository.delegate.ChatOperationsDelegate
import com.example.data.repository.delegate.FileOperationsDelegate
import com.example.data.repository.delegate.LinterEngineDelegate
import com.example.data.repository.delegate.ProjectTemplateDelegate
import kotlinx.coroutines.flow.Flow

class IdeRepository(
    private val projectDao: ProjectDao,
    private val projectFileDao: ProjectFileDao,
    private val chatMessageDao: ChatMessageDao,
    private val diagnosticLogDao: DiagnosticLogDao
) {
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()

    private val templateDelegate = ProjectTemplateDelegate(projectFileDao)
    private val linterDelegate = LinterEngineDelegate(projectFileDao, diagnosticLogDao)
    private val fileDelegate = FileOperationsDelegate(projectDao, projectFileDao, linterDelegate)
    private val chatDelegate = ChatOperationsDelegate(chatMessageDao)

    fun getFilesForProject(projectId: Long): Flow<List<ProjectFileEntity>> =
        projectFileDao.getFilesForProject(projectId)

    fun getChatMessagesForProject(projectId: Long): Flow<List<ChatMessageEntity>> =
        chatMessageDao.getMessagesForProject(projectId)

    fun getDiagnosticLogsForProject(projectId: Long): Flow<List<DiagnosticLogEntity>> =
        diagnosticLogDao.getLogsForProject(projectId)

    suspend fun ensureDefaultProjectsExist() {
        if (projectDao.getProjectCount() == 0) {
            val defaultProject1 = ProjectEntity(
                name = "Mi Aplicación Web",
                description = "Aplicación Web Single Page con HTML5, CSS y JavaScript interactivo.",
                framework = "HTML5 / JS / CSS",
                iconEmoji = "🌐"
            )
            val p1Id = projectDao.insertProject(defaultProject1)
            templateDelegate.createWebProjectTemplate(p1Id)

            val defaultProject2 = ProjectEntity(
                name = "Android Jetpack Compose",
                description = "Proyecto nativo de Android en Kotlin con interfaz Jetpack Compose.",
                framework = "Kotlin + Compose",
                iconEmoji = "📱"
            )
            val p2Id = projectDao.insertProject(defaultProject2)
            templateDelegate.createAndroidComposeTemplate(p2Id)

            val defaultProject3 = ProjectEntity(
                name = "Rust HTTP Microservice",
                description = "Servidor Web ligero en Rust integrado para ejecuciones de alto rendimiento.",
                framework = "Rust HTTP Server",
                iconEmoji = "🦀"
            )
            val p3Id = projectDao.insertProject(defaultProject3)
            templateDelegate.createRustServerTemplate(p3Id)

            val defaultProject4 = ProjectEntity(
                name = "C++ Native Core",
                description = "Motor de procesamiento matemático en C++ compilado vía JNI.",
                framework = "C++ JNI",
                iconEmoji = "⚡"
            )
            val p4Id = projectDao.insertProject(defaultProject4)
            templateDelegate.createCppNativeTemplate(p4Id)
        }
    }

    suspend fun createProject(name: String, description: String, framework: String): Long {
        val emoji = when {
            framework.contains("Compose", true) || framework.contains("Android", true) -> "📱"
            framework.contains("Rust", true) -> "🦀"
            framework.contains("C++", true) -> "⚡"
            framework.contains("Node", true) -> "💚"
            else -> "🌐"
        }
        val project = ProjectEntity(
            name = name,
            description = description,
            framework = framework,
            iconEmoji = emoji
        )
        val projectId = projectDao.insertProject(project)

        when {
            framework.contains("Compose", true) || framework.contains("Android", true) -> templateDelegate.createAndroidComposeTemplate(projectId)
            framework.contains("Rust", true) -> templateDelegate.createRustServerTemplate(projectId)
            framework.contains("C++", true) -> templateDelegate.createCppNativeTemplate(projectId)
            framework.contains("Node", true) -> templateDelegate.createNodeApiTemplate(projectId)
            else -> templateDelegate.createWebProjectTemplate(projectId)
        }

        chatMessageDao.insertMessage(
            ChatMessageEntity(
                projectId = projectId,
                sender = "agent",
                text = "¡Hola! He preparado el nuevo proyecto '$name' ($framework). ¿En qué puedo ayudarte hoy?",
                timestamp = System.currentTimeMillis()
            )
        )

        linterDelegate.runLinterAnalysis(projectId)

        return projectId
    }

    suspend fun deleteProject(projectId: Long) {
        projectFileDao.deleteFilesForProject(projectId)
        chatMessageDao.clearHistoryForProject(projectId)
        diagnosticLogDao.clearLogsForProject(projectId)
        projectDao.deleteProjectById(projectId)
    }

    suspend fun getProjectById(projectId: Long): ProjectEntity? {
        return projectDao.getProjectById(projectId)
    }

    // --- File Operations Delegated ---

    suspend fun createFile(projectId: Long, name: String, path: String, content: String = "", parentPath: String = "/"): Long =
        fileDelegate.createFile(projectId, name, path, content, parentPath)

    suspend fun createDirectory(projectId: Long, name: String, parentPath: String = "/"): Long =
        fileDelegate.createDirectory(projectId, name, parentPath)

    suspend fun updateFileContent(projectId: Long, path: String, content: String) =
        fileDelegate.updateFileContent(projectId, path, content)

    suspend fun deleteFile(projectId: Long, id: Long) =
        fileDelegate.deleteFile(projectId, id)

    suspend fun getFileByPath(projectId: Long, path: String): ProjectFileEntity? =
        fileDelegate.getFileByPath(projectId, path)

    suspend fun editFileContentByTarget(projectId: Long, path: String, targetContent: String, replacementContent: String): String =
        fileDelegate.editFileContentByTarget(projectId, path, targetContent, replacementContent)

    suspend fun deleteFileByPath(projectId: Long, path: String): String =
        fileDelegate.deleteFileByPath(projectId, path)

    // --- Chat Operations Delegated ---

    suspend fun addChatMessage(projectId: Long, message: ChatMessageEntity): Long =
        chatDelegate.addChatMessage(projectId, message)

    suspend fun updateChatMessageContent(id: Long, text: String, targetFilePath: String?, proposedCode: String?) =
        chatDelegate.updateChatMessageContent(id, text, targetFilePath, proposedCode)

    suspend fun setMessageApplied(id: Long) =
        chatDelegate.setMessageApplied(id)

    suspend fun clearChatHistory(projectId: Long) =
        chatDelegate.clearChatHistory(projectId)

    // --- Linter & Diagnostics Delegated ---

    suspend fun addDiagnosticLog(
        projectId: Long,
        level: String,
        source: String,
        message: String,
        filePath: String? = null,
        lineNumber: Int? = null
    ) = linterDelegate.addDiagnosticLog(projectId, level, source, message, filePath, lineNumber)

    suspend fun clearDiagnosticLogs(projectId: Long) =
        linterDelegate.clearDiagnosticLogs(projectId)

    suspend fun runLinterAnalysis(projectId: Long): List<DiagnosticLogEntity> =
        linterDelegate.runLinterAnalysis(projectId)
}
