# 🤖 Manual de Contexto y Arquitectura para Agentes IA

Este documento sirve como manual de incorporación para cualquier Agente de Inteligencia Artificial que trabaje o contribuya a este repositorio.

---

## 💡 Principios de Arquitectura

1. **Estructura Polyglot**:
   - **Kotlin + Jetpack Compose**: Toda la interfaz gráfica de usuario y la lógica de estado (ViewModel/StateFlow) residen en Kotlin.
   - **C++ Native Core**: El directorio `app/src/main/cpp/` contiene librerías compartidas C++ invocadas mediante JNI (`CppEngine.kt`).
   - **Rust Embedded HTTP Server**: El directorio `app/src/main/rust/devstudio_server/` aloja el servidor de pruebas HTTP en Rust invocado desde `RustHttpServer.kt`.

2. **Herramientas de Agente IA en Tiempo Real (Tool Calling) & Modos de Chat**:
   - **Agente Director & 4 Sub-Agentes Especializados**:
     - 👔 **Agente Director**: Coordina el flujo general, atiende al usuario y dirige el trabajo.
     - 🏗️ **Agente Arquitecto**: Diseña la jerarquía de archivos y módulos.
     - 🎨 **Agente Frontend & UI**: Encargado de HTML, CSS, M3 y diseño.
     - ⚡ **Agente Lógica & Backend**: Desarrolla JavaScript, APIs y Servidor.
     - 🛡️ **Agente QA & Linter**: Valida sintaxis, diagnósticos y errores.
   - **Tres Modos de Interacción Seleccionables (`ChatMode`)**:
     1. 💬 **Chat (Planificación)**: Para estructurar el proyecto, analizar código y dar ideas sin realizar cambios en archivos.
     2. 🐾 **Código Paso a Paso**: La IA propone cambios detallados especificando el sub-agente asignado y requiriendo confirmación del usuario mediante un botón 'Aceptar / Aplicar Cambios'.
     3. 🚀 **Código Completo (Autónomo)**: La IA ejecuta cambios de forma automática en tiempo real directamente sobre los archivos utilizando las herramientas nativas.
   - **Herramientas Disponibles**:
     - `get_project_structure()`: Obtiene el árbol completo de directorios y archivos.
     - `read_file(path)`: Lee el contenido de cualquier archivo en el proyecto.
     - `edit_file(path, target_content, replacement_content)`: Modifica líneas exactas de código en caliente.
     - `create_file(path, content)`: Crea nuevos archivos y carpetas.
     - `delete_file(path)`: Elimina elementos del workspace.
     - `get_diagnostics()`: Consulta los registros de la Consola de Diagnóstico en Vivo y errores del Linter.

3. **Sistema de Skills Contextuales Integradas en Archivos `.md` (`/skills/`)**:
   - Las habilidades se almacenan en archivos `.md` separados que funcionan por detrás y no ensucian el gestor de archivos del proyecto del usuario:
     - **Guía de Uso de Herramientas (`tool_usage_guide.md`)**: Instruye al modelo sobre cómo usar sus 5 herramientas. **Prohibición estricta**: No se permite reescribir un archivo entero usando `edit_file`.
     - **UI/UX Design (`design_ui_ux.md`)**: Estética Material 3, tipografía con jerarquía, paleta cromática equilibrada e interacciones fluidas.
     - **Responsive Layout (`responsive_layout.md`)**: Estrategia Mobile-First, Flexbox/Grid, clamp() y prevención de desbordamientos.
     - **Clean Logic (`clean_logic.md`)**: Funciones puras, JS ES6+, manejo de errores con try-catch y validación de datos.

4. **Editor de Código Nativo y Coloreado de Sintaxis**:
   - `CodeEditorView.kt` utiliza un `BasicTextField` con un transformador visual (`SyntaxHighlighter.kt`) y un panel lateral con numeración de líneas sincronizado mediante scroll.
   - `SyntaxHighlighter.kt` realiza coloreado dinámico para HTML, CSS, JavaScript, JSON, Markdown, Kotlin, C++ y Rust.

5. **Estructura Jerárquica de Carpetas**:
   - La entidad `ProjectFileEntity` gestiona la propiedad `isDirectory: Boolean` y la ruta contenedora `parentPath: String`.
   - La función recursiva `buildFlatTree` en `FileManagerDrawer.kt` renderiza el árbol de directorios con sangría de profundidad (`depth`) y estado de desplegado (`expandedFolders`).

6. **Servidor HTTP Localhost y Vista Previa Multipágina**:
   - El servidor de vista previa corre en `http://127.0.0.1:8080` utilizando `RustHttpServer.kt`.
   - `LivePreviewView.kt` implementa un navegador Web completo con soporte para múltiples páginas HTML (`index.html`, `about.html`, etc.), botones de navegación (Atrás, Adelante, Recargar), barra de dirección URL en tiempo real y chips selector de archivos HTML.
   - `android:usesCleartextTraffic="true"` debe permanecer activo en `AndroidManifest.xml` para permitir conexiones HTTP locales sin TLS en el WebView.

7. **Persistencia**:
   - Todos los datos de archivos y carpetas del usuario se gestionan con **Room Database** (`AppDatabase.kt`).
   - Evitar acumulaciones de archivos obsoletos .md dentro del workspace del usuario.

---

## ⚠️ Reglas Importantes para la Modificación de Código

- **Conservar Signaturas JNI**: Si modificas funciones nativas en `devstudio_cpp.cpp` o `lib.rs`, actualiza de manera idéntica los métodos `external fun` en `CppEngine.kt` y `RustHttpServer.kt`.
- **Compatibilidad con Compose**: Mantén la reactividad utilizando `StateFlow` y `collectAsStateWithLifecycle()`.
- **Manejo de Errores Robustos**: El servidor HTTP nativo debe contar con fallback para servir los archivos del proyecto incluso si las librerías dinámicas `.so` están en proceso de recompilación.
