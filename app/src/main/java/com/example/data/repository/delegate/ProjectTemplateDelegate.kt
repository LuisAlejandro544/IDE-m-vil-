package com.example.data.repository.delegate

import com.example.data.db.ProjectFileDao
import com.example.data.db.ProjectFileEntity

class ProjectTemplateDelegate(
    private val projectFileDao: ProjectFileDao
) {

    suspend fun createWebProjectTemplate(projectId: Long) {
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
                        <nav class="nav-bar">
                          <a href="index.html" class="nav-link active">Inicio</a>
                          <a href="about.html" class="nav-link">Acerca de</a>
                        </nav>

                        <div class="header">
                          <span class="badge">PROYECTO MULTIPÁGINA DEVSTUDIO</span>
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
                            <li>📁 Integrar gestor de archivos multipágina</li>
                            <li>👁️ Habilitar vista previa en vivo con servidor Rust</li>
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
                name = "about.html",
                path = "/about.html",
                extension = "html",
                content = """
                    <!DOCTYPE html>
                    <html lang="es">
                    <head>
                      <meta charset="UTF-8">
                      <meta name="viewport" content="width=device-width, initial-scale=1.0">
                      <title>Acerca de - DevStudio</title>
                      <link rel="stylesheet" href="style.css">
                    </head>
                    <body>
                      <div class="container">
                        <nav class="nav-bar">
                          <a href="index.html" class="nav-link">← Inicio</a>
                          <a href="about.html" class="nav-link active">Acerca de</a>
                        </nav>

                        <div class="header">
                          <span class="badge">SOPORTE MULTIPÁGINA</span>
                          <h1>Acerca del Proyecto</h1>
                          <p>Esta página demuestra la navegación entre múltiples archivos HTML en el servidor local DevStudio.</p>
                        </div>

                        <div class="card">
                          <h3>🚀 Características del IDE</h3>
                          <ul style="margin-left: 20px; line-height: 1.8;">
                            <li>Navegación nativa entre páginas HTML.</li>
                            <li>Servidor HTTP ultra-rápido en tiempo real.</li>
                            <li>Soporte para CSS, JavaScript y assets locales.</li>
                            <li>Generación de proyectos con Agente de IA.</li>
                          </ul>
                        </div>
                      </div>
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

                    .nav-bar {
                      display: flex;
                      gap: 12px;
                      background: var(--card-bg);
                      padding: 10px 16px;
                      border-radius: 8px;
                      border: 1px solid var(--border-color);
                    }

                    .nav-link {
                      color: var(--text-secondary);
                      text-decoration: none;
                      font-weight: 600;
                      font-size: 0.9rem;
                    }

                    .nav-link.active, .nav-link:hover {
                      color: var(--primary-blue);
                    }

                    .badge {
                      display: inline-block;
                      background: rgba(16, 185, 129, 0.15);
                      color: var(--accent-green);
                      padding: 4px 8px;
                      border-radius: 4px;
                      font-size: 0.75rem;
                      font-weight: 700;
                      margin-bottom: 8px;
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

    suspend fun createAndroidComposeTemplate(projectId: Long) {
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

    suspend fun createRustServerTemplate(projectId: Long) {
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

    suspend fun createCppNativeTemplate(projectId: Long) {
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

    suspend fun createNodeApiTemplate(projectId: Long) {
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
}
