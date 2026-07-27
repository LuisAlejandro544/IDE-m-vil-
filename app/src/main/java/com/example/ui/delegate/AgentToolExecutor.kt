package com.example.ui.delegate

import com.example.data.db.ProjectFileEntity
import com.example.data.repository.IdeRepository
import org.json.JSONObject

class AgentToolExecutor(
    private val repository: IdeRepository
) {

    suspend fun executeTool(
        projectId: Long,
        toolName: String,
        args: JSONObject,
        filesList: List<ProjectFileEntity>,
        activeFilePath: String?,
        onFileContentUpdated: (path: String, newContent: String) -> Unit,
        onFileSelected: (path: String) -> Unit,
        onTabClosed: (path: String) -> Unit
    ): String {
        return when (toolName) {
            "get_project_structure" -> {
                if (filesList.isEmpty()) "Proyecto vacío"
                else filesList.joinToString("\n") { f ->
                    if (f.isDirectory) "📁 [DIR] ${f.path}" else "📄 ${f.path} (${f.content.length} chars)"
                }
            }

            "read_file" -> {
                val path = args.optString("path")
                if (path.isBlank()) "❌ Ruta de archivo requerida."
                else {
                    val f = repository.getFileByPath(projectId, path)
                    if (f == null) "❌ Archivo '$path' no encontrado."
                    else "📄 Contenido de '$path':\n```\n${f.content}\n```"
                }
            }

            "edit_file" -> {
                val path = args.optString("path")
                val targetContent = args.optString("target_content")
                val replacementContent = args.optString("replacement_content")

                if (path.isBlank()) "❌ Ruta de archivo requerida."
                else {
                    val res = repository.editFileContentByTarget(projectId, path, targetContent, replacementContent)
                    if (activeFilePath == path) {
                        val updated = repository.getFileByPath(projectId, path)
                        if (updated != null) {
                            onFileContentUpdated(path, updated.content)
                        }
                    }
                    res
                }
            }

            "create_file" -> {
                val path = args.optString("path")
                val content = args.optString("content")
                if (path.isBlank()) "❌ Ruta de archivo requerida."
                else {
                    val cleanPath = if (path.startsWith("/")) path else "/$path"
                    val fileName = cleanPath.substringAfterLast('/')
                    val parent = if (cleanPath.count { it == '/' } > 1) cleanPath.substringBeforeLast('/') else "/"
                    repository.createFile(projectId, fileName, cleanPath, content, parentPath = parent)
                    onFileSelected(cleanPath)
                    "✅ Éxito: Archivo '$cleanPath' creado y abierto."
                }
            }

            "delete_file" -> {
                val path = args.optString("path")
                if (path.isBlank()) "❌ Ruta requerida."
                else {
                    val res = repository.deleteFileByPath(projectId, path)
                    onTabClosed(path)
                    res
                }
            }

            "get_diagnostics" -> {
                val logs = repository.runLinterAnalysis(projectId)
                if (logs.isEmpty()) "🟢 Consola de Diagnóstico: Sin errores reportados."
                else {
                    val sb = StringBuilder("🔍 Reporte de Consola de Diagnóstico & Linter:\n")
                    logs.forEach { log ->
                        sb.append("- [${log.level}][${log.source}] ${log.filePath ?: ""}:${log.lineNumber ?: 0} -> ${log.message}\n")
                    }
                    sb.toString()
                }
            }

            else -> "⚠️ Herramienta '$toolName' no reconocida."
        }
    }
}
