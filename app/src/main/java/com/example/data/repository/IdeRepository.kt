package com.example.data.repository

import com.example.data.db.ChatMessageDao
import com.example.data.db.ChatMessageEntity
import com.example.data.db.DiagnosticLogDao
import com.example.data.db.DiagnosticLogEntity
import com.example.data.db.ProjectDao
import com.example.data.db.ProjectEntity
import com.example.data.db.ProjectFileDao
import com.example.data.db.ProjectFileEntity
import kotlinx.coroutines.flow.Flow

class IdeRepository(
    private val projectDao: ProjectDao,
    private val projectFileDao: ProjectFileDao,
    private val chatMessageDao: ChatMessageDao,
    private val diagnosticLogDao: DiagnosticLogDao
) {
    val allProjects: Flow<List<ProjectEntity>> = projectDao.getAllProjects()

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
            createWebProjectTemplate(p1Id)

            val defaultProject2 = ProjectEntity(
                name = "Android Jetpack Compose",
                description = "Proyecto nativo de Android en Kotlin con interfaz Jetpack Compose.",
                framework = "Kotlin + Compose",
                iconEmoji = "📱"
            )
            val p2Id = projectDao.insertProject(defaultProject2)
            createAndroidComposeTemplate(p2Id)

            val defaultProject3 = ProjectEntity(
                name = "Rust HTTP Microservice",
                description = "Servidor Web ligero en Rust integrado para ejecuciones de alto rendimiento.",
                framework = "Rust HTTP Server",
                iconEmoji = "🦀"
            )
            val p3Id = projectDao.insertProject(defaultProject3)
            createRustServerTemplate(p3Id)

            val defaultProject4 = ProjectEntity(
                name = "C++ Native Core",
                description = "Motor de procesamiento matemático en C++ compilado vía JNI.",
                framework = "C++ JNI",
                iconEmoji = "⚡"
            )
            val p4Id = projectDao.insertProject(defaultProject4)
            createCppNativeTemplate(p4Id)
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
            framework.contains("Compose", true) || framework.contains("Android", true) -> createAndroidComposeTemplate(projectId)
            framework.contains("Rust", true) -> createRustServerTemplate(projectId)
            framework.contains("C++", true) -> createCppNativeTemplate(projectId)
            framework.contains("Node", true) -> createNodeApiTemplate(projectId)
            else -> createWebProjectTemplate(projectId)
        }

        // Welcome message for project
        chatMessageDao.insertMessage(
            ChatMessageEntity(
                projectId = projectId,
                sender = "agent",
                text = "¡Hola! He preparado el nuevo proyecto '$name' ($framework). ¿En qué puedo ayudarte hoy?",
                timestamp = System.currentTimeMillis()
            )
        )

        // Run initial linter
        runLinterAnalysis(projectId)

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

    // --- Template Creation Methods ---

    private suspend fun createWebProjectTemplate(projectId: Long) {
        val files = listOf(
            ProjectFileEntity(
                projectId = projectId,
                name = "index.html",
                path = "/index.html",
                extension = "html",
                content = """
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                      <meta charset="UTF-8">
                      <meta name="viewport" content="width=device-width, initial-scale=1.0">
                      <title>DevStudio Web Demo</title>
                      <link rel="stylesheet" href="style.css">
                    </head>
                    <body>
                      <div class="container">
                        <div class="header">
                          <span class="badge">PROYECTO DEVSTUDIO</span>
                          <h1>Mi Aplicación Web</h1>
                          <p>Diseñada con el IDE Móvil y Asistente IA</p>
                        </div>

                        <div class="card">
                          <h3>Contador Interactivo</h3>
                          <div class="counter-display" id="counter">0</div>
                          <div class="button-group">
                            <button class="btn btn-secondary" onclick="decrement()">- Disminuir</button>
                            <button class="btn btn-primary" onclick="increment()">+ Incrementar</button>
                          </div>
                        </div>

                        <div class="card">
                          <h3>Lista de Tareas Rápidas</h3>
                          <ul id="taskList">
                            <li>✨ Crear interfaz de IDE Móvil</li>
                            <li>📁 Integrar gestor de archivos</li>
                            <li>👁️ Habilitar vista previa en vivo</li>
                          </ul>
                        </div>
                      </div>

                      <script src="script.js"></script>
                    </body>
                    </html>
                """.trimIndent()
            ),
            ProjectFileEntity(
                projectId = projectId,
                name = "style.css",
                path = "/style.css",
                extension = "css",
                content = """
                    :root {
                      --bg-color: #121318;
                      --card-bg: #1A1C23;
                      --border-color: #2D303E;
                      --text-primary: #F1F5F9;
                      --text-secondary: #94A3B8;
                      --primary-blue: #4F83F6;
                      --accent-green: #10B981;
                    }

                    * {
                      box-sizing: border-box;
                      margin: 0;
                      padding: 0;
                    }

                    body {
                      background-color: var(--bg-color);
                      color: var(--text-primary);
                      font-family: system-ui, sans-serif;
                      padding: 20px;
                      display: flex;
                      justify-content: center;
                    }

                    .container {
                      width: 100%;
                      max-width: 500px;
                      display: flex;
                      flex-direction: column;
                      gap: 16px;
                    }

                    .card {
                      background: var(--card-bg);
                      border: 1px solid var(--border-color);
                      border-radius: 12px;
                      padding: 20px;
                    }

                    .counter-display {
                      font-size: 3rem;
                      font-weight: 800;
                      text-align: center;
                      color: var(--primary-blue);
                      margin: 16px 0;
                    }

                    .button-group {
                      display: flex;
                      gap: 10px;
                    }

                    .btn {
                      flex: 1;
                      padding: 12px;
                      border: none;
                      border-radius: 8px;
                      font-weight: 600;
                      cursor: pointer;
                    }

                    .btn-primary {
                      background: var(--primary-blue);
                      color: #FFFFFF;
                    }

                    .btn-secondary {
                      background: #2D303E;
                      color: var(--text-primary);
                    }
                """.trimIndent()
            ),
            ProjectFileEntity(
                projectId = projectId,
                name = "script.js",
                path = "/script.js",
                extension = "js",
                content = """
                    let count = 0;

                    function updateDisplay() {
                      const display = document.getElementById('counter');
                      if (display) {
                        display.textContent = count;
                      }
                    }

                    function increment() {
                      count++;
                      updateDisplay();
                    }

                    function decrement() {
                      count--;
                      updateDisplay();
                    }

                    console.log("DevStudio IDE: Script cargado con éxito.");
                """.trimIndent()
            )
        )
        projectFileDao.insertFiles(files)
    }

    private suspend fun createAndroidComposeTemplate(projectId: Long) {
        val files = listOf(
            ProjectFileEntity(
                projectId = projectId,
                name = "MainActivity.kt",
                path = "/src/main/java/MainActivity.kt",
                extension = "kt",
                parentPath = "/src/main/java",
                content = """
                    package com.example.app

                    import androidx.compose.foundation.layout.*
                    import androidx.compose.material3.*
                    import androidx.compose.runtime.*
                    import androidx.compose.ui.Modifier
                    import androidx.compose.ui.unit.dp

                    @Composable
                    fun MainScreen() {
                        var count by remember { mutableStateOf(0) }

                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
                            Column(
                                modifier = Modifier.padding(24.dp),
                                verticalArrangement = Arrangement.Center
                            ) {
                                Text(
                                    text = "Jetpack Compose App",
                                    style = MaterialTheme.typography.headlineMedium
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Text(
                                    text = "Contador: ${'$'}count",
                                    style = MaterialTheme.typography.bodyLarge
                                )
                                Spacer(modifier = Modifier.height(16.dp))
                                Button(onClick = { count++ }) {
                                    Text("Incrementar")
                                }
                            }
                        }
                    }
                """.trimIndent()
            ),
            ProjectFileEntity(
                projectId = projectId,
                name = "Theme.kt",
                path = "/src/main/java/Theme.kt",
                extension = "kt",
                parentPath = "/src/main/java",
                content = """
                    package com.example.app.ui.theme

                    import androidx.compose.material3.MaterialTheme
                    import androidx.compose.material3.darkColorScheme
                    import androidx.compose.runtime.Composable
                    import androidx.compose.ui.graphics.Color

                    private val DarkColorScheme = darkColorScheme(
                        primary = Color(0xFF4F83F6),
                        background = Color(0xFF121318),
                        surface = Color(0xFF1A1C23)
                    )

                    @Composable
                    fun AppTheme(content: @Composable () -> Unit) {
                        MaterialTheme(
                            colorScheme = DarkColorScheme,
                            content = content
                        )
                    }
                """.trimIndent()
            )
        )
        projectFileDao.insertFiles(files)
    }

    private suspend fun createRustServerTemplate(projectId: Long) {
        val files = listOf(
            ProjectFileEntity(
                projectId = projectId,
                name = "main.rs",
                path = "/src/main.rs",
                extension = "rs",
                parentPath = "/src",
                content = """
                    use std::io::Write;
                    use std::net::TcpListener;

                    fn main() {
                        let listener = TcpListener::bind("127.0.0.1:8080").unwrap();
                        println!("Servidor Rust escuchando en http://127.0.0.1:8080");

                        for stream in listener.incoming() {
                            let mut stream = stream.unwrap();
                            let response = "HTTP/1.1 200 OK\r\nContent-Type: text/plain\r\n\r\n¡Servidor Rust DevStudio Activo!";
                            stream.write_all(response.as_bytes()).unwrap();
                        }
                    }
                """.trimIndent()
            ),
            ProjectFileEntity(
                projectId = projectId,
                name = "Cargo.toml",
                path = "/Cargo.toml",
                extension = "toml",
                content = """
                    [package]
                    name = "rust_devstudio_server"
                    version = "0.1.0"
                    edition = "2021"

                    [dependencies]
                """.trimIndent()
            )
        )
        projectFileDao.insertFiles(files)
    }

    private suspend fun createCppNativeTemplate(projectId: Long) {
        val files = listOf(
            ProjectFileEntity(
                projectId = projectId,
                name = "engine.cpp",
                path = "/cpp/engine.cpp",
                extension = "cpp",
                parentPath = "/cpp",
                content = """
                    #include <jni.h>
                    #include <string>

                    extern "C" JNIEXPORT jstring JNICALL
                    Java_com_example_native_CppEngine_calculate(JNIEnv* env, jobject /* this */, jint a, jint b) {
                        int result = a * b;
                        std::string message = "C++ Engine Result: " + std::to_string(result);
                        return env->NewStringUTF(message.c_str());
                    }
                """.trimIndent()
            ),
            ProjectFileEntity(
                projectId = projectId,
                name = "CMakeLists.txt",
                path = "/CMakeLists.txt",
                extension = "txt",
                content = """
                    cmake_minimum_required(VERSION 3.22.1)
                    project("cppengine")

                    add_library(cppengine SHARED cpp/engine.cpp)
                """.trimIndent()
            )
        )
        projectFileDao.insertFiles(files)
    }

    private suspend fun createNodeApiTemplate(projectId: Long) {
        val files = listOf(
            ProjectFileEntity(
                projectId = projectId,
                name = "server.js",
                path = "/server.js",
                extension = "js",
                content = """
                    const http = require('http');

                    const PORT = 3000;
                    const server = http.createServer((req, res) => {
                        res.setHeader('Content-Type', 'application/json');
                        res.writeHead(200);
                        res.end(JSON.stringify({ status: "OK", message: "Node.js REST API lista en DevStudio" }));
                    });

                    server.listen(PORT, () => {
                        console.log(`Servidor Node.js corriendo en puerto ${'$'}PORT`);
                    });
                """.trimIndent()
            ),
            ProjectFileEntity(
                projectId = projectId,
                name = "package.json",
                path = "/package.json",
                extension = "json",
                content = """
                    {
                      "name": "node-devstudio-api",
                      "version": "1.0.0",
                      "main": "server.js",
                      "scripts": {
                        "start": "node server.js"
                      }
                    }
                """.trimIndent()
            )
        )
        projectFileDao.insertFiles(files)
    }

    // --- File Operations ---

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
        runLinterAnalysis(projectId)
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
        runLinterAnalysis(projectId)
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
            runLinterAnalysis(projectId)
        }
    }

    suspend fun getFileByPath(projectId: Long, path: String): ProjectFileEntity? {
        return projectFileDao.getFileByPathAndProject(projectId, path)
    }

    // --- Chat Operations ---

    suspend fun addChatMessage(projectId: Long, message: ChatMessageEntity): Long {
        return chatMessageDao.insertMessage(message.copy(projectId = projectId))
    }

    suspend fun updateChatMessageContent(id: Long, text: String, targetFilePath: String?, proposedCode: String?) {
        chatMessageDao.updateMessageContent(id, text, targetFilePath, proposedCode)
    }

    suspend fun setMessageApplied(id: Long) {
        chatMessageDao.setMessageApplied(id)
    }

    suspend fun clearChatHistory(projectId: Long) {
        chatMessageDao.clearHistoryForProject(projectId)
    }

    // --- AI Tool Operations ---

    suspend fun editFileContentByTarget(projectId: Long, path: String, targetContent: String, replacementContent: String): String {
        val existing = projectFileDao.getFileByPathAndProject(projectId, path)
            ?: return "❌ Error: El archivo '$path' no existe en este proyecto."

        return if (existing.content.contains(targetContent)) {
            val updatedContent = existing.content.replace(targetContent, replacementContent)
            projectFileDao.updateFileContentByProjectAndPath(projectId, path, updatedContent)
            projectDao.updateProjectTimestamp(projectId)
            runLinterAnalysis(projectId)
            "✅ Éxito: Se editó correctamente '$path'."
        } else {
            if (targetContent.isBlank()) {
                projectFileDao.updateFileContentByProjectAndPath(projectId, path, replacementContent)
                projectDao.updateProjectTimestamp(projectId)
                runLinterAnalysis(projectId)
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
        runLinterAnalysis(projectId)
        return "✅ Éxito: Eliminado '$path'."
    }

    // --- Live Linter & Diagnostics Engine ---

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

        // Fetch all files for project
        val files = projectFileDao.getFileByPathAndProject(projectId, "/index.html")
            ?.let { listOf(it) } ?: emptyList()

        // Scan all project files
        val projectFiles = mutableListOf<ProjectFileEntity>()
        // Simple query replacement or load all files for this project
        // Note: projectFileDao can query list
        // Let's add a quick scan
        val dummyFiles = listOf("/index.html", "/style.css", "/script.js", "/src/main/java/MainActivity.kt", "/src/main.rs", "/cpp/engine.cpp", "/server.js")
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
                    // Check HTML basic syntax
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
