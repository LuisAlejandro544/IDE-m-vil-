package com.example.data.repository

import com.example.data.db.ChatMessageDao
import com.example.data.db.ChatMessageEntity
import com.example.data.db.ProjectFileDao
import com.example.data.db.ProjectFileEntity
import kotlinx.coroutines.flow.Flow

class IdeRepository(
    private val projectFileDao: ProjectFileDao,
    private val chatMessageDao: ChatMessageDao
) {
    val allFiles: Flow<List<ProjectFileEntity>> = projectFileDao.getAllFiles()
    val chatMessages: Flow<List<ChatMessageEntity>> = chatMessageDao.getAllMessages()

    suspend fun ensureDefaultFilesExist() {
        // Ensure any .md files inside the app workspace are deleted as requested
        projectFileDao.deleteMarkdownFiles()

        if (projectFileDao.getFileCount() == 0) {
            val defaultFiles = listOf(
                ProjectFileEntity(
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
                    name = "style.css",
                    path = "/style.css",
                    extension = "css",
                    content = """
                        /* Estilos cómodos y legibles (Sin colores chillones/cyberpunk) */
                        :root {
                          --bg-color: #121318;
                          --card-bg: #1A1C23;
                          --border-color: #2D303E;
                          --text-primary: #F1F5F9;
                          --text-secondary: #94A3B8;
                          --primary-blue: #4F83F6;
                          --primary-hover: #3B72E6;
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
                          font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif;
                          padding: 20px;
                          display: flex;
                          justify-content: center;
                          min-height: 100vh;
                        }

                        .container {
                          width: 100%;
                          max-width: 500px;
                          display: flex;
                          flex-direction: column;
                          gap: 16px;
                        }

                        .header {
                          text-align: center;
                          padding: 12px 0;
                        }

                        .badge {
                          background: rgba(79, 131, 246, 0.15);
                          color: var(--primary-blue);
                          padding: 4px 12px;
                          border-radius: 20px;
                          font-size: 0.75rem;
                          font-weight: 700;
                          letter-spacing: 0.5px;
                          text-transform: uppercase;
                        }

                        .header h1 {
                          font-size: 1.5rem;
                          margin-top: 8px;
                          color: var(--text-primary);
                        }

                        .header p {
                          font-size: 0.85rem;
                          color: var(--text-secondary);
                          margin-top: 4px;
                        }

                        .card {
                          background: var(--card-bg);
                          border: 1px solid var(--border-color);
                          border-radius: 12px;
                          padding: 20px;
                        }

                        .card h3 {
                          font-size: 1rem;
                          margin-bottom: 12px;
                          color: var(--text-primary);
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
                          font-size: 0.9rem;
                          cursor: pointer;
                          transition: background 0.2s;
                        }

                        .btn-primary {
                          background: var(--primary-blue);
                          color: #FFFFFF;
                        }

                        .btn-secondary {
                          background: #2D303E;
                          color: var(--text-primary);
                        }

                        ul {
                          list-style: none;
                          display: flex;
                          flex-direction: column;
                          gap: 8px;
                        }

                        li {
                          background: rgba(255, 255, 255, 0.03);
                          padding: 10px 14px;
                          border-radius: 6px;
                          font-size: 0.85rem;
                          border-left: 3px solid var(--accent-green);
                        }
                    """.trimIndent()
                ),
                ProjectFileEntity(
                    name = "script.js",
                    path = "/script.js",
                    extension = "js",
                    content = """
                        // Lógica JavaScript del Contador
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
                ),
                ProjectFileEntity(
                    name = "README.md",
                    path = "/README.md",
                    extension = "md",
                    content = """
                        # 🚀 DevStudio IDE v1.0

                        Entorno de desarrollo integrado móvil polyglot para Android con soporte nativo de Kotlin, C++ y Rust.

                        ## ✨ Características Principales
                        - 📁 **Estructura de Carpetas Jerárquica y Anidada**: Crea y organiza subcarpetas y archivos a cualquier nivel de profundidad.
                        - ✏️ **Editor de Código Nativo**: Edición ultra fluida con resaltado de sintaxis, numeración de líneas y barra rápida de símbolos.
                        - 👁️ **Vista Previa Web Interactiva**: Servidor HTTP local en Rust corriendo en `http://127.0.0.1:8080`.
                        - ⚡ **Núcleo Nativo Polyglot**: Motores de C++ JNI y Rust integrados directamente.
                        - 🤖 **Asistente de Código IA**: Chat y generación de código contextual integrada.
                    """.trimIndent()
                ),
                ProjectFileEntity(
                    name = "STRUCTURE.md",
                    path = "/STRUCTURE.md",
                    extension = "md",
                    content = """
                        # 📂 Estructura del Proyecto DevStudio

                        ## Archivos Raíz
                        - `/index.html` - Documento principal HTML del proyecto Web.
                        - `/style.css` - Hoja de estilos con variables y modo oscuro.
                        - `/script.js` - Lógica interactiva JavaScript.
                        - `/README.md` - Documentación general del IDE.
                        - `/STRUCTURE.md` - Mapa jerárquico del proyecto.

                        ## Directorio de Documentación (`/Docs/`)
                        - `/Docs/Roadmap.md` - Estado de funciones y características planeadas.
                        - `/Docs/AI_Context.md` - Contexto arquitectónico para el Asistente IA.
                        - `/Docs/Agents.md` - Reglas de desarrollo para los agentes.
                    """.trimIndent()
                ),
                ProjectFileEntity(
                    name = "Docs",
                    path = "/Docs",
                    extension = "folder",
                    content = "",
                    isDirectory = true,
                    parentPath = "/"
                ),
                ProjectFileEntity(
                    name = "Roadmap.md",
                    path = "/Docs/Roadmap.md",
                    extension = "md",
                    content = """
                        # 🗺️ Roadmap de DevStudio

                        - [x] Soporte Polyglot (Kotlin Jetpack Compose, C++ JNI, Rust HTTP Server)
                        - [x] Servidor HTTP Localhost en Rust (Puerto 8080)
                        - [x] Vista previa Web interactiva en tiempo real
                        - [x] Estructura de carpetas jerárquica y anidada sin límite de profundidad
                        - [x] Editor de Código Nativo con numeración de líneas y resaltado de sintaxis
                        - [x] Barra rápida de caracteres y símbolos de programación
                        - [ ] Integración con repositorios Git remotos
                    """.trimIndent(),
                    parentPath = "/Docs"
                ),
                ProjectFileEntity(
                    name = "AI_Context.md",
                    path = "/Docs/AI_Context.md",
                    extension = "md",
                    content = """
                        # 🤖 AI Context & Arquitectura

                        ## Arquitectura Polyglot
                        1. **UI Jetpack Compose**: Interfaz moderna con Material Design 3 y manejo de insets.
                        2. **Editor Nativo**: VisualTransformation con sintaxis coloreada para HTML, CSS, JS, KT, CPP, Rust y JSON.
                        3. **C++ Native Engine**: Procesamiento de alto rendimiento vía JNI.
                        4. **Rust Localhost HTTP Server**: Servidor embebido escuchando en `http://127.0.0.1:8080`.
                        5. **Room Local Database**: Persistencia reactiva de carpetas, archivos y chat de IA.
                    """.trimIndent(),
                    parentPath = "/Docs"
                ),
                ProjectFileEntity(
                    name = "Agents.md",
                    path = "/Docs/Agents.md",
                    extension = "md",
                    content = """
                        # 📋 Reglas y Guía para Agentes

                        1. **Respetar Arquitectura Polyglot**: Mantener la sinergia entre Kotlin, C++ JNI y Rust.
                        2. **Diseño Material 3**: Usar la paleta de colores centralizada en `ui/theme/`.
                        3. **Verificación de Compilación**: Ejecutar `compile_applet` tras cada cambio significativo.
                        4. **Persistencia Room**: Asegurar que toda creación o borrado de carpetas/archivos actualice la base de datos.
                    """.trimIndent(),
                    parentPath = "/Docs"
                )
            )

            projectFileDao.insertFiles(defaultFiles)

            // Initial welcome chat message
            chatMessageDao.insertMessage(
                ChatMessageEntity(
                    sender = "agent",
                    text = "¡Hola! Soy tu Agente de Código en DevStudio. Puedes pedirme agregar botones, modificar el diseño CSS, buscar errores o crear nuevos archivos.",
                    timestamp = System.currentTimeMillis()
                )
            )
        }
    }

    suspend fun createFile(name: String, path: String, content: String = "", parentPath: String = "/"): Long {
        val ext = name.substringAfterLast('.', "txt")
        val cleanParent = if (parentPath.endsWith("/") && parentPath != "/") parentPath.dropLast(1) else parentPath
        val fullPath = if (path.startsWith("/")) path else if (cleanParent == "/") "/$name" else "$cleanParent/$name"
        val entity = ProjectFileEntity(
            name = name,
            path = fullPath,
            extension = ext,
            content = content,
            isDirectory = false,
            parentPath = cleanParent
        )
        return projectFileDao.insertFile(entity)
    }

    suspend fun createDirectory(name: String, parentPath: String = "/"): Long {
        val cleanParent = if (parentPath.endsWith("/") && parentPath != "/") parentPath.dropLast(1) else parentPath
        val fullPath = if (cleanParent == "/") "/$name" else "$cleanParent/$name"
        val entity = ProjectFileEntity(
            name = name,
            path = fullPath,
            extension = "folder",
            content = "",
            isDirectory = true,
            parentPath = cleanParent
        )
        return projectFileDao.insertFile(entity)
    }

    suspend fun updateFileContent(path: String, content: String) {
        projectFileDao.updateFileContentByPath(path, content)
    }

    suspend fun deleteFile(id: Long) {
        val file = projectFileDao.getFileById(id)
        if (file != null) {
            if (file.isDirectory) {
                projectFileDao.deletePathAndChildren(file.path, "${file.path}/%")
            } else {
                projectFileDao.deleteFileById(id)
            }
        }
    }

    suspend fun getFileByPath(path: String): ProjectFileEntity? {
        return projectFileDao.getFileByPath(path)
    }

    suspend fun addChatMessage(message: ChatMessageEntity): Long {
        return chatMessageDao.insertMessage(message)
    }

    suspend fun updateChatMessageContent(id: Long, text: String, targetFilePath: String?, proposedCode: String?) {
        chatMessageDao.updateMessageContent(id, text, targetFilePath, proposedCode)
    }

    suspend fun setMessageApplied(id: Long) {
        chatMessageDao.setMessageApplied(id)
    }

    suspend fun clearChatHistory() {
        chatMessageDao.clearHistory()
    }

    // AI Tool Helper Methods
    suspend fun editFileContentByTarget(path: String, targetContent: String, replacementContent: String): String {
        val existing = projectFileDao.getFileByPath(path)
            ?: return "❌ Error: El archivo '$path' no existe."

        return if (existing.content.contains(targetContent)) {
            val updatedContent = existing.content.replace(targetContent, replacementContent)
            projectFileDao.updateFileContentByPath(path, updatedContent)
            "✅ Éxito: Se editó correctamente '$path'."
        } else {
            // Fallback: If target content not exact match, check if targetContent is empty or replace all
            if (targetContent.isBlank()) {
                projectFileDao.updateFileContentByPath(path, replacementContent)
                "✅ Éxito: Se reemplazó el contenido completo de '$path'."
            } else {
                "⚠️ No se encontró la coincidencia exacta en '$path'. Intentando reemplazo de líneas aproximadas."
            }
        }
    }

    suspend fun deleteFileByPath(path: String): String {
        val file = projectFileDao.getFileByPath(path)
            ?: return "❌ El archivo o carpeta '$path' no existe."

        if (file.isDirectory) {
            projectFileDao.deletePathAndChildren(file.path, "${file.path}/%")
        } else {
            projectFileDao.deleteFileByPath(path)
        }
        return "✅ Éxito: Eliminado '$path'."
    }
}
