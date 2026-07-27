package com.example.data.repository.delegate

import com.example.data.db.DiagnosticLogDao
import com.example.data.db.DiagnosticLogEntity
import com.example.data.db.ProjectFileDao
import com.example.data.db.ProjectFileEntity

class LinterEngineDelegate(
    private val projectFileDao: ProjectFileDao,
    private val diagnosticLogDao: DiagnosticLogDao
) {

    suspend fun addDiagnosticLog(
        projectId: Long,
        level: String,
        source: String,
        message: String,
        filePath: String? = null,
        lineNumber: Int? = null
    ) {
        diagnosticLogDao.insertLog(
            DiagnosticLogEntity(
                projectId = projectId,
                level = level,
                source = source,
                message = message,
                filePath = filePath,
                lineNumber = lineNumber,
                timestamp = System.currentTimeMillis()
            )
        )
    }

    suspend fun clearDiagnosticLogs(projectId: Long) {
        diagnosticLogDao.clearLogsForProject(projectId)
    }

    suspend fun runLinterAnalysis(projectId: Long): List<DiagnosticLogEntity> {
        diagnosticLogDao.clearLinterLogs(projectId)
        val logs = mutableListOf<DiagnosticLogEntity>()

        val dummyFiles = listOf("/index.html", "/style.css", "/script.js", "/src/main/java/MainActivity.kt", "/src/main.rs", "/cpp/engine.cpp", "/server.js")
        val projectFiles = mutableListOf<ProjectFileEntity>()
        for (p in dummyFiles) {
            val f = projectFileDao.getFileByPathAndProject(projectId, p)
            if (f != null && !f.isDirectory) projectFiles.add(f)
        }

        if (projectFiles.isEmpty()) {
            val systemLog = DiagnosticLogEntity(
                projectId = projectId,
                level = "INFO",
                source = "System",
                message = "Análisis Linter completado. Estado del proyecto normal.",
                timestamp = System.currentTimeMillis()
            )
            diagnosticLogDao.insertLog(systemLog)
            logs.add(systemLog)
            return logs
        }

        for (file in projectFiles) {
            val lines = file.content.lines()

            when (file.extension.lowercase()) {
                "html" -> {
                    var openDivs = 0
                    lines.forEachIndexed { index, line ->
                        if (line.contains("<div")) openDivs += line.split("<div").size - 1
                        if (line.contains("</div>")) openDivs -= line.split("</div>").size - 1

                        if (line.contains("<script") && !line.contains("src=") && !line.contains("</script>")) {
                            val log = DiagnosticLogEntity(
                                projectId = projectId,
                                level = "WARNING",
                                source = "Linter",
                                message = "Etiqueta <script> abierta sin atributo src ni cierre en la misma línea.",
                                filePath = file.path,
                                lineNumber = index + 1
                            )
                            logs.add(log)
                        }
                    }
                    if (openDivs != 0) {
                        val log = DiagnosticLogEntity(
                            projectId = projectId,
                            level = "ERROR",
                            source = "Linter",
                            message = "Desbalance de etiquetas <div>...</div>. Faltan ${if (openDivs > 0) "$openDivs '</div>'" else "${-openDivs} '<div>'"}.",
                            filePath = file.path
                        )
                        logs.add(log)
                    }
                }

                "css" -> {
                    var openBraces = 0
                    lines.forEachIndexed { index, line ->
                        openBraces += line.count { it == '{' } - line.count { it == '}' }
                        if (line.contains("color:") && !line.contains(";") && !line.endsWith("{")) {
                            val log = DiagnosticLogEntity(
                                projectId = projectId,
                                level = "WARNING",
                                source = "Linter",
                                message = "Propiedad CSS sin punto y coma final ';'.",
                                filePath = file.path,
                                lineNumber = index + 1
                            )
                            logs.add(log)
                        }
                    }
                    if (openBraces != 0) {
                        val log = DiagnosticLogEntity(
                            projectId = projectId,
                            level = "ERROR",
                            source = "Linter",
                            message = "Sintaxis CSS inválida: Llaves '{' y '}' desbalanceadas.",
                            filePath = file.path
                        )
                        logs.add(log)
                    }
                }

                "js", "ts" -> {
                    var openParens = 0
                    lines.forEachIndexed { index, line ->
                        openParens += line.count { it == '(' } - line.count { it == ')' }
                        if (line.contains("console.log")) {
                            val log = DiagnosticLogEntity(
                                projectId = projectId,
                                level = "INFO",
                                source = "Linter",
                                message = "Instrucción de depuración console.log detectada.",
                                filePath = file.path,
                                lineNumber = index + 1
                            )
                            logs.add(log)
                        }
                    }
                    if (openParens != 0) {
                        val log = DiagnosticLogEntity(
                            projectId = projectId,
                            level = "ERROR",
                            source = "Linter",
                            message = "Sintaxis JS: Paréntesis '(' y ')' desbalanceados.",
                            filePath = file.path
                        )
                        logs.add(log)
                    }
                }

                "kt" -> {
                    lines.forEachIndexed { index, line ->
                        if (line.trim().startsWith("fun ") && !line.contains("(") && !line.contains(")")) {
                            val log = DiagnosticLogEntity(
                                projectId = projectId,
                                level = "ERROR",
                                source = "Linter",
                                message = "Declaración de función Kotlin sin lista de parámetros.",
                                filePath = file.path,
                                lineNumber = index + 1
                            )
                            logs.add(log)
                        }
                    }
                }

                "rs" -> {
                    lines.forEachIndexed { index, line ->
                        if (line.contains("println!") && !line.contains(";")) {
                            val log = DiagnosticLogEntity(
                                projectId = projectId,
                                level = "WARNING",
                                source = "Linter",
                                message = "Macro println! en Rust sin ';' final.",
                                filePath = file.path,
                                lineNumber = index + 1
                            )
                            logs.add(log)
                        }
                    }
                }

                "cpp" -> {
                    lines.forEachIndexed { index, line ->
                        if (line.startsWith("#include") && !line.contains("<") && !line.contains("\"")) {
                            val log = DiagnosticLogEntity(
                                projectId = projectId,
                                level = "ERROR",
                                source = "Linter",
                                message = "Directiva #include en C++ con formato de cabecera inválido.",
                                filePath = file.path,
                                lineNumber = index + 1
                            )
                            logs.add(log)
                        }
                    }
                }
            }
        }

        if (logs.isEmpty()) {
            val successLog = DiagnosticLogEntity(
                projectId = projectId,
                level = "SUCCESS",
                source = "Linter",
                message = "🟢 Linter en Vivo: Sin errores ni advertencias de sintaxis detectados.",
                timestamp = System.currentTimeMillis()
            )
            logs.add(successLog)
        }

        diagnosticLogDao.insertLogs(logs)
        return logs
    }
}
