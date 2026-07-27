# DevStudio IDE - Mobile Polyglot Development Environment

**DevStudio** es un IDE móvil avanzado para Android desarrollado en Kotlin y Jetpack Compose, diseñado para ofrecer una experiencia de desarrollo completa directamente en dispositivos móviles.

---

## 🌟 Características Principales

- **Gestor de Archivos con Estructura Jerárquica y Anidada**: Soporte para creación de carpetas, subcarpetas e inserción de archivos en cualquier nivel o en la raíz.
- **Editor de Código Nativo**: Editor ultra fluido desarrollado en Jetpack Compose con numeración de líneas, desplazamiento bidireccional y resaltado de sintaxis (HTML, CSS, JavaScript, Markdown, JSON, Kotlin, C++, Rust).
- **Agente IA Director & Sub-Agentes con 3 Modos de Chat**:
  - 👔 **Agente Director**: Atiende al usuario y supervisa a 4 Sub-Agentes especializados (🏗️ Arquitecto, 🎨 Frontend, ⚡ Lógica/Backend, 🛡️ QA).
  - 💬 **Modo Chat (Planificación)**: Diseñado para idear la estructura del proyecto y resolver dudas sin modificar código.
  - 🐾 **Modo Paso a Paso**: Los sub-agentes proponen los cambios de código paso a paso requiriendo confirmación del usuario.
  - 🚀 **Modo Código Completo (Autónomo)**: Ejecución 100% automática de herramientas e implementación de código en tiempo real.
  - 🛠️ **Herramientas Disponibles**: Consultar estructura (`get_project_structure`), leer archivos (`read_file`), editar líneas exactas (`edit_file`), crear archivos (`create_file`), eliminar (`delete_file`) y consultar diagnósticos (`get_diagnostics`).
- **Sistema Integrado de Skills de IA en Archivos `.md` Detrás de Escena (`/skills/`)**: Inyección automática de habilidades especializadas desde archivos `.md` externos (sin saturar el espacio de trabajo del usuario):
  - 🛠️ **Tool Usage Guide (`tool_usage_guide.md`)**: Instrucciones precisas de uso de herramientas y prohibición estricta de reescritura total de archivos con `edit_file`.
  - 🎨 **UI/UX Design (`design_ui_ux.md`)**: Estética Material Design 3, paletas cromáticas elegantes y micro-interacciones.
  - 📱 **Responsive Layout (`responsive_layout.md`)**: Diseños fluidos Mobile-First, Flexbox/Grid, clamp() y media queries sin desbordamientos.
  - ⚡ **Clean Logic (`clean_logic.md`)**: Funciones puras, manejo de errores robusto, manipulación segura con ES6+ y validación de datos.
- **Vista Previa Web Multipágina y Servidor Rust**: Renderizado Web en tiempo real con servidor HTTP local Rust (`127.0.0.1:8080`), navegación multipágina (Atrás, Adelante, Recargar, Barra de dirección URL activa y Selector rápido de archivos HTML como `index.html`, `about.html`), alternancia de modos teléfono/tablet y acceso externo en el navegador del dispositivo.
- **Barra de Símbolos Rápidos**: Teclado auxiliar optimizado para programación táctil (`<`, `>`, `{`, `}`, `(`, `)`, `;`, `=`, `/`, `TAB`).
- **Persistencia Local con Room**: Base de datos SQLite reactiva con soporte jerárquico (`parentPath`) para almacenamiento seguro de carpetas y archivos.

---

## 🛠️ Stack Tecnológico

- **Frontend / UI**: Kotlin, Jetpack Compose, Material Design 3.
- **Agente IA**: Engine de streaming multimodal con Tool Calling + Inyección de Skills en `.md` (`AiSkills.kt` / `/skills/`) para Google Gemini API y OpenRouter API.
- **Editor de Código**: BasicTextField con VisualTransformation (Syntax Highlighter) + Scroll bidireccional y Line Numbers.
- **Nativo C++**: CMake, NDK, JNI (`libdevstudio_cpp.so`).
- **Nativo Rust**: Rust `cdylib`, JNI, `tiny_http` (`libdevstudio_server.so`).
- **Persistencia**: Android Room Database (Soporte jerárquico de directorios), Kotlin Coroutines, StateFlow.
- **Vista Previa**: Android WebView + Servidor HTTP Localhost Rust en `127.0.0.1:8080`.

---

## 🚀 Cómo Ejecutar el Proyecto

1. Abrir el proyecto en Android Studio o compilar mediante `./gradlew assembleDebug`.
2. Ejecutar la aplicación en un emulador o dispositivo real.
3. El servidor local en Rust iniciará automáticamente en `http://127.0.0.1:8080` al abrir la vista previa.
