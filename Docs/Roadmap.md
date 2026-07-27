# 🗺️ Roadmap de DevStudio IDE

Este documento define la hoja de ruta y planificación de características para DevStudio.

---

## 📌 Fase 1: Arquitectura Polyglot & Núcleo Nativo (COMPLETADO ✅)
- [x] Interfaz de usuario adaptativa con Jetpack Compose y tema oscuro optimizado.
- [x] Editor con barra rápida de símbolos programáticos.
- [x] Persistencia de archivos y chat mediante Room DB SQLite.
- [x] Módulo Nativo C++ integrado vía JNI.
- [x] Servidor HTTP Localhost embebido en Rust sirviendo en `127.0.0.1:8080`.
- [x] Integración de WebView con soporte para abrir en navegador externo del dispositivo.

---

## 📌 Fase 2: Gestor de Archivos Jerárquico y Editor Nativo (COMPLETADO ✅)
- [x] **Estructura de Carpetas Jerárquica y Anidada**: Creación de carpetas, subcarpetas y gestión de ubicación padre (`parentPath`).
- [x] **Eliminación en Cascada**: Eliminación de directorios y todos sus archivos/carpetas descendientes.
- [x] **Editor de Código Nativo**: Componente Jetpack Compose fluido con números de línea y scroll bidireccional.
- [x] **Resaltado de Sintaxis Coloreado**: Soporte para HTML, CSS, JavaScript, Markdown, JSON, Kotlin, C++ y Rust.

---

## 📌 Fase 3: Asistente IA con Herramientas & Skills en Tiempo Real (COMPLETADO ✅)
- [x] **Llamada a Herramientas (Tool Calling)**: Integración de herramientas en tiempo real para Gemini y OpenRouter.
- [x] **Operaciones del Agente**: `get_project_structure`, `read_file`, `edit_file`, `create_file` y `delete_file`.
- [x] **Inyección de Skills de IA (`AiSkills.kt`)**: Habilidades automáticas de Diseños UI/UX, Adaptabilidad Responsive Mobile-First y Lógica Robusta / Clean Code.
- [x] **Sincronización en Tiempo Real**: Reflejo inmediato de cambios de código en la vista del editor activo y consola de herramientas del chat.

---

## 📌 Fase 4: Control de Versiones Git Integrado (EN PROCESO ⏳)
- [ ] Soporte para operaciones Git locales (Commit, Status, Branch).
- [ ] Integración con GitHub (Clonar repositorios y realizar Push/Pull).
