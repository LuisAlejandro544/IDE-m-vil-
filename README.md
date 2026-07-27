# DevStudio IDE - Mobile Polyglot Development Environment

**DevStudio** es un IDE móvil avanzado para Android desarrollado en Kotlin y Jetpack Compose, diseñado para ofrecer una experiencia de desarrollo completa directamente en dispositivos móviles.

---

## 🌟 Características Principales

- **Gestor de Archivos con Estructura Jerárquica y Anidada**: Soporte para creación de carpetas, subcarpetas e inserción de archivos en cualquier nivel o en la raíz.
- **Editor de Código Nativo**: Editor ultra fluido desarrollado en Jetpack Compose con numeración de líneas, desplazamiento bidireccional y resaltado de sintaxis (HTML, CSS, JavaScript, Markdown, JSON, Kotlin, C++, Rust).
- **Servidor HTTP Localhost Integrado en Rust**: Servidor incrustado de alto rendimiento para servir la vista previa web en `http://127.0.0.1:8080`.
- **Módulo Nativo C++**: Núcleo C++ nativo listo para procesamiento intensivo a través de JNI.
- **Asistente de Código IA Integrado**: Chat interactivo con motor Gemini para generación, análisis y aplicación automática de cambios de código.
- **Vista Previa en Vivo**: Renderizado Web en tiempo real con alternancia de modos teléfono/tablet y botón para abrir en el navegador del dispositivo.
- **Barra de Símbolos Rápidos**: Teclado auxiliar optimizado para programación táctil (`<`, `>`, `{`, `}`, `(`, `)`, `;`, `=`, `/`, `TAB`).
- **Persistencia Local con Room**: Base de datos SQLite reactiva con soporte jerárquico (`parentPath`) para almacenamiento seguro de carpetas y archivos.

---

## 🛠️ Stack Tecnológico

- **Frontend / UI**: Kotlin, Jetpack Compose, Material Design 3.
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
