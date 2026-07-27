package com.example.data.repository.delegate

import com.example.data.db.ProjectDao
import com.example.data.db.ProjectFileDao
import com.example.data.db.ProjectFileEntity

class FileOperationsDelegate(
    private val projectDao: ProjectDao,
    private val projectFileDao: ProjectFileDao,
    private val linterDelegate: LinterEngineDelegate
) {

    suspend fun createFile(projectId: Long, name: String, path: String, content: String = "", parentPath: String = "/"): Long {
        val ext = name.substringAfterLast('.', "txt")
        val cleanParent = if (parentPath.endsWith("/") && parentPath != "/") parentPath.dropLast(1) else parentPath
        val fullPath = if (path.startsWith("/")) path else if (cleanParent == "/") "/$name" else "$cleanParent/$name"
        val entity = ProjectFileEntity(
            projectId = projectId,
            name = name,
            path = fullPath,
            extension = ext,
            content = content,
            isDirectory = false,
            parentPath = cleanParent
        )
        val fileId = projectFileDao.insertFile(entity)
        projectDao.updateProjectTimestamp(projectId)
        linterDelegate.runLinterAnalysis(projectId)
        return fileId
    }

    suspend fun createDirectory(projectId: Long, name: String, parentPath: String = "/"): Long {
        val cleanParent = if (parentPath.endsWith("/") && parentPath != "/") parentPath.dropLast(1) else parentPath
        val fullPath = if (cleanParent == "/") "/$name" else "$cleanParent/$name"
        val entity = ProjectFileEntity(
            projectId = projectId,
            name = name,
            path = fullPath,
            extension = "folder",
            content = "",
            isDirectory = true,
            parentPath = cleanParent
        )
        return projectFileDao.insertFile(entity)
    }

    suspend fun updateFileContent(projectId: Long, path: String, content: String) {
        projectFileDao.updateFileContentByProjectAndPath(projectId, path, content)
        projectDao.updateProjectTimestamp(projectId)
        linterDelegate.runLinterAnalysis(projectId)
    }

    suspend fun deleteFile(projectId: Long, id: Long) {
        val file = projectFileDao.getFileById(id)
        if (file != null) {
            if (file.isDirectory) {
                projectFileDao.deletePathAndChildrenByProject(projectId, file.path, "${file.path}/%")
            } else {
                projectFileDao.deleteFileById(id)
            }
            projectDao.updateProjectTimestamp(projectId)
            linterDelegate.runLinterAnalysis(projectId)
        }
    }

    suspend fun getFileByPath(projectId: Long, path: String): ProjectFileEntity? {
        return projectFileDao.getFileByPathAndProject(projectId, path)
    }

    suspend fun editFileContentByTarget(projectId: Long, path: String, targetContent: String, replacementContent: String): String {
        val existing = projectFileDao.getFileByPathAndProject(projectId, path)
            ?: return "❌ Error: El archivo '$path' no existe en este proyecto."

        return if (existing.content.contains(targetContent)) {
            val updatedContent = existing.content.replace(targetContent, replacementContent)
            projectFileDao.updateFileContentByProjectAndPath(projectId, path, updatedContent)
            projectDao.updateProjectTimestamp(projectId)
            linterDelegate.runLinterAnalysis(projectId)
            "✅ Éxito: Se editó correctamente '$path'."
        } else {
            if (targetContent.isBlank()) {
                projectFileDao.updateFileContentByProjectAndPath(projectId, path, replacementContent)
                projectDao.updateProjectTimestamp(projectId)
                linterDelegate.runLinterAnalysis(projectId)
                "✅ Éxito: Se reemplazó el contenido completo de '$path'."
            } else {
                "⚠️ No se encontró la coincidencia exacta en '$path'."
            }
        }
    }

    suspend fun deleteFileByPath(projectId: Long, path: String): String {
        val file = projectFileDao.getFileByPathAndProject(projectId, path)
            ?: return "❌ El archivo o carpeta '$path' no existe."

        if (file.isDirectory) {
            projectFileDao.deletePathAndChildrenByProject(projectId, file.path, "${file.path}/%")
        } else {
            projectFileDao.deleteFileByProjectAndPath(projectId, path)
        }
        projectDao.updateProjectTimestamp(projectId)
        linterDelegate.runLinterAnalysis(projectId)
        return "✅ Éxito: Eliminado '$path'."
    }
}
