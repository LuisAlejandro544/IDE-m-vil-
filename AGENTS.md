# AGENTS.md - Reglas del Proyecto DevStudio

Consulte la documentación detallada en `Docs/Agents.md` y `Docs/AI_Context.md`.

## Resumen de Reglas:
1. **Arquitectura Polyglot**: Kotlin + C++ JNI + Rust Localhost HTTP Server.
2. **Jerarquía y Persistencia**: `parentPath` e `isDirectory` en `ProjectFileEntity` con Room DB.
3. **Llamada a Herramientas de IA (Tool Calling)**: Soporte de herramientas en tiempo real (`get_project_structure`, `read_file`, `edit_file`, `create_file`, `delete_file`) ejecutables mediante llamadas a funciones de Gemini u OpenRouter.
4. **Sistema de Skills Integradas**: Inyección de habilidades contextuales (`AiSkills.kt`): Diseños M3 / Estética, Adaptabilidad Responsive (Mobile-First) y Lógica Robusta / Clean Code.
5. **Edición Nativa**: `CodeEditorView.kt` con `SyntaxHighlighter.kt`.
6. **Paleta M3**: Seguir temas y colores en `ui/theme/`.
7. **Compilación**: Validar con `compile_applet` tras cualquier cambio.
