# 🛠️ Skill: Tool Usage Guide & Constraints (Guía de Uso de Herramientas e Instrucciones)

## Propósito
Esta habilidad enseña al Agente de IA a utilizar sus herramientas interactivas de manera precisa, segura y eficiente en DevStudio IDE.

---

## 🛠️ Herramientas Disponibles y Uso Exacto

### 1. `get_project_structure()`
- **Descripción**: Obtiene la estructura jerárquica completa de archivos y directorios del proyecto.
- **Cuándo usar**: Úsalo al inicio de una tarea compleja para conocer la ubicación exacta de los archivos antes de realizar lecturas o ediciones.

### 2. `read_file(path: String)`
- **Descripción**: Lee el contenido completo de un archivo específico del proyecto (ej. `/index.html`, `/style.css`).
- **Cuándo usar**: Invócala para inspeccionar el código existente antes de proponer cambios con `edit_file`.

### 3. `edit_file(path: String, target_content: String, replacement_content: String)`
- **Descripción**: Realiza ediciones quirúrgicas y exactas reemplazando un bloque específico de código por uno nuevo.
- **REGLAS Y PROHIBICIONES STRICTAS DE `edit_file`**:
  - ⛔ **PROHIBIDO REESCRIBIR EL ARCHIVO ENTERO**: NUNCA envíes el código completo del archivo en `target_content`.
  - 🎯 **REEMPLAZO DE LÍNEAS EXACTAS**: `target_content` debe contener ÚNICAMENTE el fragmento, línea o bloque específico que se desea cambiar.
  - 🔍 **COINCIDENCIA EXACTA**: El texto en `target_content` debe coincidir exactamente (incluyendo espacios y saltos de línea) con el código en el archivo.

### 4. `create_file(path: String, content: String)`
- **Descripción**: Crea un nuevo archivo en el workspace con su contenido inicial.
- **Cuándo usar**: Úsala exclusivamente para crear un archivo nuevo o carpetas necesarias que no existan previamente.

### 5. `delete_file(path: String)`
- **Descripción**: Elimina un archivo o directorio completo del workspace.
- **Cuándo usar**: Solo cuando el usuario solicite explícitamente eliminar un elemento.

### 6. `get_diagnostics()`
- **Descripción**: Consulta los registros activos de la Consola de Diagnóstico / Linter en Vivo para el proyecto actual.
- **Cuándo usar**: Úsalo cuando ocurra un error de sintaxis, comportamiento inesperado o el usuario te pida depurar/resolver fallos en el código. Devuelve advertencias, errores de sintaxis y registros del sistema.

---

## ⚠️ Reglas Generales de Conducta de Herramientas
1. **Paso Previo de Inspección**: Siempre consulta o lee el archivo antes de modificarlo si no estás seguro de su contenido.
2. **Ediciones Limpias**: Utiliza `edit_file` para modificar solo las partes relevantes del código.
3. **Respuesta Transparente**: Explica brevemente en español qué herramienta ejecutaste y el resultado obtenido.
