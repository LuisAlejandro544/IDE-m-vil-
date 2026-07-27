# 🗺️ Roadmap de DevStudio IDE

Este documento define la hoja de ruta y planificación de características para DevStudio.

---

## 📌 Fase 1: Arquitectura Polyglot & Núcleo Nativo (COMPLETADO ✅)
- [x] Interfaz de usuario adaptativa con Jetpack Compose y tema oscuro optimizado.
- [x] Editor con barra rápida de símbolos programáticos.
- [x] Persistencia de archivos y chat mediante Room DB SQLite.
- [x] Módulo Nativo C++ integrado vía JNI.
- [x] Servidor HTTP Localhost embebido en Rust sirviendo en `127.0.0.1:8080` con soporte para rutas y recursos dinámicos.
- [x] Integración de WebView con soporte multipágina (`index.html`, `about.html`), historial de navegación (Atrás/Adelante), barra de dirección URL y apertura en navegador externo del dispositivo.

---

## 📌 Fase 2: Gestor de Archivos Jerárquico y Editor Nativo (COMPLETADO ✅)
- [x] **Estructura de Carpetas Jerárquica y Anidada**: Creación de carpetas, subcarpetas y gestión de ubicación padre (`parentPath`).
- [x] **Eliminación en Cascada**: Eliminación de directorios y todos sus archivos/carpetas descendientes.
- [x] **Editor de Código Nativo**: Componente Jetpack Compose fluido con números de línea y scroll bidireccional.
- [x] **Resaltado de Sintaxis Coloreado**: Soporte para HTML, CSS, JavaScript, Markdown, JSON, Kotlin, C++ y Rust.

---

## 📌 Fase 3: Asistente IA con Herramientas, Multi-Agente Director & Skills `.md` (COMPLETADO ✅)
- [x] **Arquitectura Multi-Agente (Director + 4 Sub-Agentes)**:
  - Agente Director coordinando a los sub-agentes Arquitecto 🏗️, Frontend 🎨, Backend/Lógica ⚡ y QA 🛡️.
- [x] **Tres Modos de Chat Seleccionables (`ChatMode`)**:
  - 💬 **Chat (Planificación)**: Modo para dar ideas, estructurar proyectos y responder preguntas sin modificar código.
  - 🐾 **Código Paso a Paso**: Modo supervisado donde los sub-agentes proponen cambios que el usuario aprueba con un clic.
  - 🚀 **Código Completo (Autónomo)**: Modo 100% automático donde la IA ejecuta las herramientas e implementa todo el código directamente.
- [x] **Llamada a Herramientas (Tool Calling)**: Integración de herramientas en tiempo real para Gemini y OpenRouter.
- [x] **Operaciones del Agente**: `get_project_structure`, `read_file`, `edit_file`, `create_file`, `delete_file` y `get_diagnostics`.
- [x] **Sistema de Skills `.md` Detrás de Escena (`/skills/`)**:
  - `tool_usage_guide.md`: Guía de herramientas y prohibición estricta de reescribir un archivo completo en `edit_file`.
  - `design_ui_ux.md`: Diseños M3 y estética moderna.
  - `responsive_layout.md`: Adaptabilidad Mobile-First y Flexbox/Grid.
  - `clean_logic.md`: Lógica limpia, ES6+ y manejo de errores.
- [x] **Sincronización en Tiempo Real**: Reflejo inmediato de cambios de código en la vista del editor activo y consola de herramientas del chat.

---

## 📌 Fase 4: Control de Versiones Git Integrado (EN PROCESO ⏳)
- [ ] Soporte para operaciones Git locales (Commit, Status, Branch).
- [ ] Integración con GitHub (Clonar repositorios y realizar Push/Pull).
