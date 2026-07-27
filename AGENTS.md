# AGENTS.md - Reglas del Proyecto DevStudio

Consulte la documentación detallada en `Docs/Agents.md` y `Docs/AI_Context.md`.

## Resumen de Reglas:
1. Mantener la arquitectura Polyglot (Kotlin + C++ JNI + Rust Localhost HTTP Server).
2. Respetar la Estructura Jerárquica de Carpetas y la persitencia con `parentPath` y `isDirectory` en `ProjectFileEntity`.
3. Mantener la UI de edicion de código nativa (`CodeEditorView.kt` con `SyntaxHighlighter.kt`).
4. Asegurar que los cambios en UI sigan la paleta M3 en `ui/theme/`.
5. Validar con `compile_applet` tras realizar modificaciones.
