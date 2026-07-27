# Estructura del Proyecto DevStudio

A continuación se detalla la arquitectura de directorios del proyecto DevStudio:

```
/ (Raíz del proyecto)
├── README.md                      # Descripción e instrucciones generales del proyecto
├── STRUCTURE.md                   # Mapeo de la estructura de archivos del repositorio
├── AGENTS.md                      # Resumen de reglas para el agente de desarrollo
├── skills/                        # Archivos .md de Habilidades de IA (Cargados por detrás)
│   ├── tool_usage_guide.md        # Guía de uso de herramientas y prohibición de reescritura total
│   ├── design_ui_ux.md            # Guía de diseño estético y Material Design 3
│   ├── responsive_layout.md       # Guía de maquetación adaptativa Mobile-First y Flexbox/Grid
│   └── clean_logic.md             # Guía de lógica limpia, ES6+ y manejo de errores
├── Docs/                          # Documentación técnica y guías para desarrolladores e IA
│   ├── Roadmap.md                 # Hoja de ruta y características implementadas
│   ├── AI_Context.md              # Contexto de arquitectura y reglas para Agentes IA
│   └── Agents.md                  # Reglas y convenciones para desarrolladores
├── app/                           # Módulo principal de Android
│   ├── build.gradle.kts           # Configuración de Gradle del módulo app
│   └── src/main/
│       ├── assets/skills/         # Archivos .md de Skills empaquetados para runtime en Android
│       │   ├── tool_usage_guide.md
│       │   ├── design_ui_ux.md
│       │   ├── responsive_layout.md
│       │   └── clean_logic.md
│       ├── AndroidManifest.xml    # Manifiesto de Android (Permisos INTERNET y Cleartext)
│       ├── cpp/                   # Código Fuente C++ Nativo
│       │   ├── CMakeLists.txt     # Configuración de compilación CMake
│       │   └── devstudio_cpp.cpp  # Implementación de funciones nativas JNI C++
│       ├── rust/                  # Código Fuente Rust Nativo
│       │   └── devstudio_server/
│       │       ├── Cargo.toml     # Dependencias de Rust
│       │       └── src/lib.rs     # Servidor HTTP Localhost JNI en Rust
│       ├── java/com/example/
│       │   ├── MainActivity.kt    # Punto de entrada Activity liviano y temas
│       │   ├── data/              # Capa de Datos (Room DB, DAOs, Entidades, Repositorio, API)
│       │   │   ├── api/           # API de Inteligencia Artificial y Skills
│       │   │   │   ├── AiAgentService.kt   # Servicio de streaming para Gemini y OpenRouter
│       │   │   │   ├── AiAgentModels.kt   # Modelos y Enums (AiProvider, StreamResult)
│       │   │   │   ├── ToolSchemaBuilder.kt# Generador de declaraciones de Function Calling JSON
│       │   │   │   └── AiSkills.kt        # Carga e inyección de habilidades .md
│       │   │   ├── db/            # Room Database (AppDatabase, DAOs y Entidades)
│       │   │   └── repository/    # Repositorio modular
│       │   │       ├── IdeRepository.kt   # Orquestador principal de repositorio
│       │   │       └── delegate/          # Delegados especializados de datos
│       │   │           ├── ProjectTemplateDelegate.kt# Plantillas de proyectos (Web, Compose, Rust, C++)
│       │   │           ├── LinterEngineDelegate.kt   # Análisis de linter y consola diagnóstica
│       │   │           ├── FileOperationsDelegate.kt # Operaciones CRUD y edición por coincidencias
│       │   │           └── ChatOperationsDelegate.kt # Historial de mensajes de chat
│       │   ├── native/            # Enlaces JNI / Interop Nativo
│       │   │   ├── CppEngine.kt   # Interfaz Kotlin para el motor C++
│       │   │   └── RustHttpServer.kt # Interfaz Kotlin para el Servidor HTTP Rust
│       │   └── ui/                # Componentes de Interfaz con Jetpack Compose
│       │       ├── IdeViewModel.kt # ViewModel principal modularizado
│       │       ├── state/         # Estados de UI desacoplados
│       │       │   └── IdeUiState.kt # Modelos de estado (IdeUiState, IdeViewMode, etc.)
│       │       ├── delegate/      # Delegados de lógica de UI
│       │       │   └── AgentToolExecutor.kt # Ejecutor de herramientas en tiempo real para el agente IA
│       │       ├── components/    # Componentes modulares Compose
│       │       │   ├── DevStudioIdeScreen.kt# Pantalla principal del IDE
│       │       │   ├── IdeTopAppBar.kt     # Barra de herramientas superior
│       │       │   ├── OpenTabsRow.kt      # Barra de pestañas abiertas
│       │       │   ├── CodeEditorView.kt   # Editor de código nativo con scroll y numeración
│       │       │   ├── SyntaxHighlighter.kt# Motor de coloreado de sintaxis
│       │       │   ├── FileManagerDrawer.kt# Árbol de archivos/carpetas jerárquico
│       │       │   ├── NewFileDialog.kt    # Diálogo para crear archivos y carpetas
│       │       │   ├── LivePreviewView.kt  # Vista previa interactiva con servidor HTTP
│       │       │   ├── DiagnosticConsoleView.kt # Consola de diagnósticos y registros del linter
│       │       │   ├── WorkspaceScreen.kt  # Pantalla de selección y creación de proyectos
│       │       │   ├── AiAgentChatSheet.kt # Interfaz del Asistente de IA con streaming
│       │       │   ├── AiSettingsDialog.kt # Diálogo de configuración de API Keys
│       │       │   └── QuickSymbolBar.kt   # Barra rápida de símbolos para teclado móvil
│       │       └── theme/         # Sistema de diseño M3 y paleta de colores Editor
│       └── res/                   # Recursos visuales, valores y cadenas XML
```
