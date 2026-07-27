# Estructura del Proyecto DevStudio

A continuación se detalla la arquitectura de directorios del proyecto DevStudio:

```
/ (Raíz del proyecto)
├── README.md                      # Descripción e instrucciones generales del proyecto
├── STRUCTURE.md                   # Mapeo de la estructura de archivos del repositorio
├── AGENTS.md                      # Resumen de reglas para el agente de desarrollo
├── Docs/                          # Documentación técnica y guías para desarrolladores e IA
│   ├── Roadmap.md                 # Hoja de ruta y características implementadas
│   ├── AI_Context.md              # Contexto de arquitectura y reglas para Agentes IA
│   └── Agents.md                  # Reglas y convenciones para desarrolladores
├── app/                           # Módulo principal de Android
│   ├── build.gradle.kts           # Configuración de Gradle del módulo app
│   └── src/main/
│       ├── AndroidManifest.xml    # Manifiesto de Android (Permisos INTERNET y Cleartext)
│       ├── cpp/                   # Código Fuente C++ Nativo
│       │   ├── CMakeLists.txt     # Configuración de compilación CMake
│       │   └── devstudio_cpp.cpp  # Implementación de funciones nativas JNI C++
│       ├── rust/                  # Código Fuente Rust Nativo
│       │   └── devstudio_server/
│       │       ├── Cargo.toml     # Dependencias de Rust
│       │       └── src/lib.rs     # Servidor HTTP Localhost JNI en Rust
│       ├── java/com/example/
│       │   ├── MainActivity.kt    # Actividad principal y Scaffold Compose
│       │   ├── data/              # Capa de Datos (Room DB, DAOs, Entidades, Repositorio)
│       │   │   ├── db/            # AppDatabase, ProjectFileEntity (isDir, parentPath), ChatMessageEntity
│       │   │   └── repository/    # IdeRepository
│       │   ├── native/            # Enlaces JNI / Interop Nativo
│       │   │   ├── CppEngine.kt   # Interfaz Kotlin para el motor C++
│       │   │   └── RustHttpServer.kt # Interfaz Kotlin para el Servidor HTTP Rust
│       │   └── ui/                # Componentes de Interfaz con Jetpack Compose
│       │       ├── IdeViewModel.kt # ViewModel principal del IDE
│       │       ├── components/    # Componentes modulares
│       │       │   ├── CodeEditorView.kt   # Editor de código nativo con scroll y numeración
│       │       │   ├── SyntaxHighlighter.kt# Motor de coloreado de sintaxis
│       │       │   ├── FileManagerDrawer.kt# Árbol de archivos/carpetas jerárquico y anidado
│       │       │   ├── NewFileDialog.kt    # Diálogo para crear archivos y carpetas
│       │       │   ├── LivePreview.kt      # Vista previa WebView con servidor Rust
│       │       │   ├── AgentChatSheet.kt   # Interfaz del Asistente de IA
│       │       │   └── QuickSymbolBar.kt   # Barra rápida de caracteres de programación
│       │       └── theme/         # Sistema de diseño M3 y paleta de colores Editor
│       └── res/                   # Recursos visuales, valores y cadenas XML
```
