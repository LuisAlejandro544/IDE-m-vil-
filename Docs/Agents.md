# 📋 Guía y Convenciones para Agentes de Código

Este documento especifica las pautas de estilo de código, flujos de trabajo y estándares para cualquier Agente de IA o desarrollador.

---

## 🎨 Estilo de Código y UI

- **Material Design 3**: Utiliza exclusivamente componentes M3 (`androidx.compose.material3`).
- **Paleta Oscura Cómoda**: La interfaz utiliza colores oscuros suaves (`#121318`, `#1A1C23`, `#2D303E`) evitando el contraste excesivo o brillante para cuidar la vista del usuario en sesiones prolongadas.
- **Editor de Código Nativo**: Mantener la implementación de `CodeEditorView.kt` y `SyntaxHighlighter.kt` en Jetpack Compose nativo.
- **Iconos**: Utilizar `Icons.Default` o `Icons.AutoMirrored` para elementos de navegación.

---

## 🧠 Sistema de Skills de IA Integradas (`AiSkills.kt`)

El motor de inteligencia artificial de DevStudio incorpora 3 Skills nativas por detrás en la llamada a la API:
1. **🎨 Diseños (UI/UX)**: Estética moderna, sombras sutiles, bordes suaves y Material Design 3.
2. **📱 Responsive**: Maquetación adaptativa Mobile-First con Flexbox, CSS Grid y sin scroll horizontal indeseado.
3. **⚡ Lógica (Clean Code)**: Código JavaScript ES6+ limpio, manejo de errores robusto, funciones modulares y validación.

---

## 🛠️ Ejecución de Herramientas IA (Tool Calling)

- El agente de IA cuenta con herramientas ejecutables en tiempo real (`get_project_structure`, `read_file`, `edit_file`, `create_file`, `delete_file`).
- Al invocar `edit_file`, las modificaciones se aplican directamente al repositorio de Room DB y se reflejan al instante en la pestaña activa del editor.

---

## 📁 Manejo de Archivos y Carpetas

- **Jerarquía y Anidación**: Toda adición de carpetas o archivos debe registrar `parentPath` en `ProjectFileEntity`.
- **Eliminación Segura**: Al borrar un directorio, se deben eliminar tanto el registro de la carpeta como sus hijos mediante `deletePathAndChildren` en `ProjectFileDao`.

---

## 🔧 Compilación y Verificación

- Antes de finalizar cualquier turno o entregar cambios, ejecuta la verificación con `compile_applet`.
- No alteres las versiones principales de Gradle ni plugins sin necesidad estricta.
