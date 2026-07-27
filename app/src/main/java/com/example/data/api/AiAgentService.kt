package com.example.data.api

import com.example.BuildConfig
import com.example.data.api.service.AiContentParser
import com.example.data.api.service.GeminiStreamProvider
import com.example.data.api.service.OpenRouterStreamProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.OkHttpClient
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiAgentService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val geminiProvider = GeminiStreamProvider(client)
    private val openRouterProvider = OpenRouterStreamProvider(client)

    fun streamUserPrompt(
        provider: AiProvider,
        chatMode: ChatMode = ChatMode.STEP_BY_STEP,
        userPrompt: String,
        currentFileContent: String?,
        currentFilePath: String?,
        allFilesSummary: String,
        openRouterApiKey: String,
        customGeminiApiKey: String,
        onExecuteTool: suspend (toolName: String, args: JSONObject) -> String
    ): Flow<StreamResult> = flow {
        val modeInstruction = when (chatMode) {
            ChatMode.PLANNING -> """
                ⚠️ MODO ACTIVO: 💬 CHAT Y PLANIFICACIÓN DE ARQUITECTURA.
                - Tu objetivo es orientar al usuario y diseñar un PLAN TÉCNICO EXCLUSIVAMENTE en formato de archivo Markdown (.md).
                - Causa un impacto estructurando tu propuesta exactamente como un archivo Markdown listo para guardar (ej: `### ARCHIVO: /PLAN.md` o `### ARCHIVO: /Docs/PLAN.md`).
                - Incluye las siguientes secciones en el archivo .md:
                  1. # Plan de Desarrollo de la Aplicación
                  2. ## 🏗️ Arquitectura y Stack
                  3. ## 📁 Estructura de Archivos
                  4. ## ⚙️ Modelo de Datos y Lógica
                  5. ## 📋 Lista de Tareas (- [ ] Tarea 1, - [ ] Tarea 2...)
                  6. ## 🚀 Siguientes Pasos
                - Usa marcas de formato Markdown explícitas (`# Encabezados`, `**texto en negrita**`, `*cursiva*`, `- [ ] casillas de verificación`, ````bloques de código````).
                - No ejecutes directamente herramientas de edición de archivos de código fuente, pero propón el archivo `/PLAN.md` o `/Docs/PLAN.md` para que el usuario pueda revisarlo, renderizarlo y aplicarlo en su proyecto.
            """.trimIndent()

            ChatMode.STEP_BY_STEP -> """
                ⚠️ MODO ACTIVO: 🐾 CÓDIGO PASO A PASO (Supervisado por el Agente Director).
                - Eres el AGENTE DIRECTOR coordinando 4 Sub-Agentes especializados:
                  1. 🏗️ Agente Arquitecto (Estructura y Módulos)
                  2. 🎨 Agente Frontend & UI (HTML, CSS, Estética M3)
                  3. ⚡ Agente Lógica & Backend (JavaScript, APIS, Server)
                  4. 🛡️ Agente QA & Linter (Validaciones y Diagnósticos)
                - Explica qué Sub-Agente ejecuta cada tarea.
                - Presenta la propuesta de código indicando claramente la ruta del archivo (`### ARCHIVO: /ruta/archivo.ext`) para que el usuario lo revise y confirme presionando el botón 'Aceptar / Aplicar Cambios'.
                - Si requiere invocar herramientas automáticas, solicita confirmación.
            """.trimIndent()

            ChatMode.FULL_AUTONOMOUS -> """
                ⚠️ MODO ACTIVO: 🚀 CÓDIGO COMPLETO (AUTÓNOMO).
                - Eres el AGENTE DIRECTOR y junto con tus 4 Sub-Agentes (🏗️ Arquitecto, 🎨 Frontend, ⚡ Lógica, 🛡️ QA) tienes autorización completa para ejecutar cambios directamente.
                - INVOCA INMEDIATAMENTE las herramientas necesarias (`edit_file`, `create_file`, `delete_file`, etc.) mediante bloques `tool_call` para aplicar todos los cambios requeridos en el proyecto en tiempo real.
            """.trimIndent()
        }

        val systemInstruction = """
            Eres el AGENTE DIRECTOR DE CÓDIGO IA para el IDE móvil DevStudio.
            Coordinas las tareas del proyecto apoyándote en 4 Sub-Agentes especializados detras de escena.

            $modeInstruction

            HERRAMIENTAS DISPONIBLES:
            1. get_project_structure() -> Devuelve el árbol completo de archivos y carpetas del proyecto.
            2. read_file(path) -> Lee el contenido completo de un archivo (ej: /index.html).
            3. edit_file(path, target_content, replacement_content) -> Reemplaza líneas o bloques exactos de código en un archivo.
            4. create_file(path, content) -> Crea un nuevo archivo con el contenido proporcionado.
            5. delete_file(path) -> Elimina un archivo o carpeta.
            6. get_diagnostics() -> Consulta los registros de la Consola de Diagnóstico en Vivo y errores del Linter.

            INSTRUCCIONES DE USO DE HERRAMIENTAS:
            - Puedes invocar herramientas escribiendo en tu respuesta el siguiente bloque JSON:
            ```tool_call
            {"name": "nombre_herramienta", "args": {"path": "/index.html", ...}}
            ```
            - Explicación clara en español sobre tus acciones y las de tus Sub-Agentes.

            ${AiSkills.getAllSkillsSystemPrompt()}
        """.trimIndent()

        val promptText = """
            Estructura actual del proyecto:
            $allFilesSummary

            Archivo activo actualmente: ${currentFilePath ?: "Ninguno"}
            Contenido del archivo activo:
            ${currentFileContent ?: "Sin contenido"}

            Petición del usuario: $userPrompt
        """.trimIndent()

        when (provider) {
            AiProvider.GEMINI -> {
                val apiKey = customGeminiApiKey.ifBlank {
                    try { BuildConfig.GEMINI_API_KEY } catch (e: Exception) { "" }
                }

                if (apiKey.isBlank() || apiKey == "MY_GEMINI_API_KEY") {
                    emit(
                        AiContentParser.parseStreamedContent(
                            "⚠️ **Atención**: No se encontró la API Key de Gemini. Por favor ingresa una API Key válida en la configuración ⚙️.",
                            currentFilePath
                        )
                    )
                    return@flow
                }

                geminiProvider.streamGemini(apiKey, systemInstruction, promptText, currentFilePath, onExecuteTool).collect { result ->
                    emit(result)
                }
            }

            AiProvider.OPENROUTER -> {
                if (openRouterApiKey.isBlank()) {
                    emit(
                        AiContentParser.parseStreamedContent(
                            "⚠️ **API Key de OpenRouter Requerida**: Para usar el modelo `inclusionai/ling-3.0-flash:free` debes ingresar tu API Key de OpenRouter.\n\nHaz clic en el ícono de configuración ⚙️ para ingresar tu API Key.",
                            currentFilePath
                        )
                    )
                    return@flow
                }

                openRouterProvider.streamOpenRouter(openRouterApiKey, systemInstruction, promptText, currentFilePath, onExecuteTool).collect { result ->
                    emit(result)
                }
            }
        }
    }.flowOn(Dispatchers.IO)
}
