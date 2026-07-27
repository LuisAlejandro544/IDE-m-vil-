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
                        # DevStudio IDE

                        Proyecto web interactivo y entorno de desarrollo listo para editar y probar en tu dispositivo.

                        ## Características
                        - 📁 Gestor de archivos en tiempo real
                        - ✏️ Editor de código con barra rápida de símbolos
                        - 👁️ Vista previa renderizada con servidor HTTP Rust (127.0.0.1:8080)
                        - ⚡ Núcleo nativo C++ y servidor HTTP Rust embebido
                        - 🤖 Asistente de Código IA integrado
                    """.trimIndent()
                ),
                ProjectFileEntity(
                    name = "STRUCTURE.md",
                    path = "/STRUCTURE.md",
                    extension = "md",
                    content = """
                        # Estructura de Proyecto

                        - `/index.html` - Página principal HTML
                        - `/style.css` - Estilos CSS de la interfaz
                        - `/script.js` - Lógica JavaScript interactiva
                        - `/Docs/Roadmap.md` - Plan de desarrollo futuro
                        - `/Docs/AI_Context.md` - Manual de contexto para la IA
                        - `/Docs/Agents.md` - Guía para agentes
                    """.trimIndent()
                ),
                ProjectFileEntity(
                    name = "Roadmap.md",
                    path = "/Docs/Roadmap.md",
                    extension = "md",
                    content = """
                        # 🗺️ Roadmap de DevStudio

                        - [x] Soporte Polyglot (Kotlin, C++, Rust)
                        - [x] Servidor HTTP Localhost en Rust (port 8080)
                        - [x] Vista previa Web interactiva con apertura externa
                        - [ ] Resaltado de sintaxis con colores dinámicos
                        - [ ] Integración con repositorio Git remoto
                    """.trimIndent()
                ),
                ProjectFileEntity(
                    name = "AI_Context.md",
                    path = "/Docs/AI_Context.md",
                    extension = "md",
                    content = """
                        # 🤖 AI Context Manual

                        Este proyecto combina Kotlin Compose (UI), C++ JNI (Procesamiento nativo) y Rust (Servidor HTTP Localhost).
                        Al realizar cambios, mantén la reactividad de Jetpack Compose y la compatibilidad de permisos de red local.
                    """.trimIndent()
                ),
                ProjectFileEntity(
                    name = "Agents.md",
                    path = "/Docs/Agents.md",
                    extension = "md",
                    content = """
                        # 📋 Guía para Agentes

                        - Mantén un código limpio y modular.
                        - Verifica la compilación con `compile_applet`.
                        - Ofrece soluciones directamente ejecutables.
                    """.trimIndent()
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

    suspend fun createFile(name: String, path: String, content: String = ""): Long {
        val ext = name.substringAfterLast('.', "txt")
        val entity = ProjectFileEntity(
            name = name,
            path = if (path.startsWith("/")) path else "/$path",
            extension = ext,
            content = content
        )
        return projectFileDao.insertFile(entity)
    }

    suspend fun updateFileContent(path: String, content: String) {
        projectFileDao.updateFileContentByPath(path, content)
    }

    suspend fun deleteFile(id: Long) {
        projectFileDao.deleteFileById(id)
    }

    suspend fun getFileByPath(path: String): ProjectFileEntity? {
        return projectFileDao.getFileByPath(path)
    }

    suspend fun addChatMessage(message: ChatMessageEntity): Long {
        return chatMessageDao.insertMessage(message)
    }

    suspend fun setMessageApplied(id: Long) {
        chatMessageDao.setMessageApplied(id)
    }

    suspend fun clearChatHistory() {
        chatMessageDao.clearHistory()
    }
}
