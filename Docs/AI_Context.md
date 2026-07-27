# 🤖 Manual de Contexto y Arquitectura para Agentes IA

Este documento sirve como manual de incorporación para cualquier Agente de Inteligencia Artificial que trabaje o contribuya a este repositorio.

---

## 💡 Principios de Arquitectura

1. **Estructura Polyglot**:
   - **Kotlin + Jetpack Compose**: Toda la interfaz gráfica de usuario y la lógica de estado (ViewModel/StateFlow) residen en Kotlin.
   - **C++ Native Core**: El directorio `app/src/main/cpp/` contiene librerías compartidas C++ invocadas mediante JNI (`CppEngine.kt`).
   - **Rust Embedded HTTP Server**: El directorio `app/src/main/rust/devstudio_server/` aloja el servidor de pruebas HTTP en Rust invocado desde `RustHttpServer.kt`.

2. **Herramientas de Agente IA en Tiempo Real (Tool Calling)**:
   - Los modelos de IA (Google Gemini 3.5 Flash u OpenRouter Ling 3.0 Flash) tienen acceso a herramientas integradas en tiempo real:
     - `get_project_structure()`: Obtiene el árbol completo de directorios y archivos.
     - `read_file(path)`: Lee el contenido de cualquier archivo en el proyecto.
     - `edit_file(path, target_content, replacement_content)`: Modifica líneas exactas de código en caliente.
     - `create_file(path, content)`: Crea nuevos archivos y carpetas.
     - `delete_file(path)`: Elimina elementos del workspace.
   - Las ejecuciones de herramientas se reflejan e interconectan en tiempo real en la UI del chat y en el editor.

3. **Sistema de Skills Contextuales Integradas (`AiSkills.kt`)**:
   - Para evitar prompts extensos y repetitivos del usuario, el sistema inyecta automáticamente en el System Instruction del Agente 3 Skills esenciales:
     - **UI/UX Design**: Estética Material 3, tipografía con jerarquía, paleta cromática equilibrada e interacciones fluidas.
     - **Responsive Layout**: Estrategia Mobile-First, Flexbox/Grid, clamp() y prevención de desbordamientos.
     - **Clean Logic**: Funciones puras, JS ES6+, manejo de errores con try-catch y validación de datos.

4. **Editor de Código Nativo y Coloreado de Sintaxis**:
   - `CodeEditorView.kt` utiliza un `BasicTextField` con un transformador visual (`SyntaxHighlighter.kt`) y un panel lateral con numeración de líneas sincronizado mediante scroll.
   - `SyntaxHighlighter.kt` realiza coloreado dinámico para HTML, CSS, JavaScript, JSON, Markdown, Kotlin, C++ y Rust.

5. **Estructura Jerárquica de Carpetas**:
   - La entidad `ProjectFileEntity` gestiona la propiedad `isDirectory: Boolean` y la ruta contenedora `parentPath: String`.
   - La función recursiva `buildFlatTree` en `FileManagerDrawer.kt` renderiza el árbol de directorios con sangría de profundidad (`depth`) y estado de desplegado (`expandedFolders`).

6. **Servidor HTTP Localhost y Vista Previa**:
   - El servidor de vista previa corre en `http://127.0.0.1:8080`.
   - `android:usesCleartextTraffic="true"` debe permanecer activo en `AndroidManifest.xml` para permitir conexiones HTTP locales sin TLS en el WebView.

7. **Persistencia**:
   - Todos los datos de archivos y carpetas del usuario se gestionan con **Room Database** (`AppDatabase.kt`).
   - Evitar acumulaciones de archivos obsoletos .md dentro del workspace del usuario.

---

## ⚠️ Reglas Importantes para la Modificación de Código

- **Conservar Signaturas JNI**: Si modificas funciones nativas en `devstudio_cpp.cpp` o `lib.rs`, actualiza de manera idéntica los métodos `external fun` en `CppEngine.kt` y `RustHttpServer.kt`.
- **Compatibilidad con Compose**: Mantén la reactividad utilizando `StateFlow` y `collectAsStateWithLifecycle()`.
- **Manejo de Errores Robustos**: El servidor HTTP nativo debe contar con fallback para servir los archivos del proyecto incluso si las librerías dinámicas `.so` están en proceso de recompilación.
