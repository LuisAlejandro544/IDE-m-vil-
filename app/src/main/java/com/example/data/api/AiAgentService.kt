package com.example.data.api

import com.example.BuildConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.util.concurrent.TimeUnit

class AiAgentService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

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
                ⚠️ MODO ACTIVO: 💬 CHAT Y PLANIFICACIÓN.
                - Tu objetivo es orientar al usuario, explicar la arquitectura del proyecto, dar ideas y analizar la estructura.
                - PROHIBIDO realizar cambios en el código o invocar herramientas de modificación de archivos (`edit_file`, `create_file`, `delete_file`).
                - Puedes consultar la estructura con `get_project_structure()` o `read_file()`.
                - Si el usuario solicita modificar archivos, explica el plan de acción y sugiérele cambiar al modo 'Paso a paso' o 'Código completo'.
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
                        parseStreamedContent(
                            "⚠️ **Atención**: No se encontró la API Key de Gemini. Por favor ingresa una API Key válida en la configuración ⚙️.",
                            currentFilePath
                        )
                    )
                    return@flow
                }

                streamGemini(apiKey, systemInstruction, promptText, currentFilePath, onExecuteTool).collect { result ->
                    emit(result)
                }
            }

            AiProvider.OPENROUTER -> {
                if (openRouterApiKey.isBlank()) {
                    emit(
                        parseStreamedContent(
                            "⚠️ **API Key de OpenRouter Requerida**: Para usar el modelo `inclusionai/ling-3.0-flash:free` debes ingresar tu API Key de OpenRouter.\n\nHaz clic en el ícono de configuración ⚙️ para ingresar tu API Key.",
                            currentFilePath
                        )
                    )
                    return@flow
                }

                streamOpenRouter(openRouterApiKey, systemInstruction, promptText, currentFilePath, onExecuteTool).collect { result ->
                    emit(result)
                }
            }
        }
    }.flowOn(Dispatchers.IO)

    private fun streamGemini(
        apiKey: String,
        systemInstruction: String,
        promptText: String,
        defaultFilePath: String?,
        onExecuteTool: suspend (toolName: String, args: JSONObject) -> String
    ): Flow<StreamResult> = flow {
        var accumulated = ""
        val executedTools = mutableSetOf<String>()

        try {
            val url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:streamGenerateContent?key=$apiKey"

            val requestJson = JSONObject().apply {
                put("systemInstruction", JSONObject().apply {
                    put("parts", org.json.JSONArray().put(JSONObject().put("text", systemInstruction)))
                })
                put("contents", org.json.JSONArray().put(
                    JSONObject().apply {
                        put("parts", org.json.JSONArray().put(JSONObject().put("text", promptText)))
                    }
                ))
                put("tools", ToolSchemaBuilder.buildGeminiToolsJson())
                put("generationConfig", JSONObject().apply {
                    put("temperature", 0.3)
                })
            }

            val httpRequest = Request.Builder()
                .url(url)
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(httpRequest).execute()

            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                emit(
                    parseStreamedContent(
                        "❌ Error en la API de Gemini (${response.code}): ${response.message}\n$errBody",
                        defaultFilePath
                    )
                )
                return@flow
            }

            val responseBody = response.body ?: return@flow

            responseBody.byteStream().bufferedReader().use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val rawLine = line?.trim() ?: continue
                    if (rawLine.isEmpty()) continue

                    var cleaned = rawLine
                    if (cleaned.startsWith("[")) cleaned = cleaned.substring(1).trim()
                    if (cleaned.startsWith(",")) cleaned = cleaned.substring(1).trim()
                    if (cleaned.endsWith("]")) cleaned = cleaned.substring(0, cleaned.length - 1).trim()

                    if (cleaned.isBlank() || cleaned == "]" || cleaned == "[") continue

                    try {
                        val json = JSONObject(cleaned)
                        val candidates = json.optJSONArray("candidates")
                        if (candidates != null && candidates.length() > 0) {
                            val candidate = candidates.getJSONObject(0)
                            val content = candidate.optJSONObject("content")
                            val parts = content?.optJSONArray("parts")

                            if (parts != null && parts.length() > 0) {
                                for (i in 0 until parts.length()) {
                                    val part = parts.getJSONObject(i)

                                    val funcCall = part.optJSONObject("functionCall")
                                    if (funcCall != null) {
                                        val fnName = funcCall.optString("name")
                                        val fnArgs = funcCall.optJSONObject("args") ?: JSONObject()
                                        val toolKey = "$fnName:$fnArgs"

                                        if (!executedTools.contains(toolKey)) {
                                            executedTools.add(toolKey)
                                            accumulated += "\n🛠️ **Ejecutando herramienta (Gemini)**: `$fnName`...\n"
                                            emit(parseStreamedContent(accumulated, defaultFilePath))

                                            val toolResult = onExecuteTool(fnName, fnArgs)
                                            accumulated += "> $toolResult\n\n"
                                            emit(parseStreamedContent(accumulated, defaultFilePath))
                                        }
                                    }

                                    val textChunk = part.optString("text", "")
                                    if (textChunk.isNotEmpty()) {
                                        accumulated += textChunk
                                        accumulated = checkAndExecuteEmbeddedToolCalls(accumulated, executedTools, onExecuteTool)
                                        emit(parseStreamedContent(accumulated, defaultFilePath))
                                    }
                                }
                            }
                        }
                    } catch (e: Exception) {
                        // Skip malformed streaming chunks
                    }
                }
            }

            if (accumulated.isBlank()) {
                emit(parseStreamedContent("No se recibió respuesta de Gemini.", defaultFilePath))
            }

        } catch (e: Exception) {
            emit(parseStreamedContent("❌ Excepción en streaming Gemini: ${e.localizedMessage}", defaultFilePath))
        }
    }

    private fun streamOpenRouter(
        apiKey: String,
        systemInstruction: String,
        promptText: String,
        defaultFilePath: String?,
        onExecuteTool: suspend (toolName: String, args: JSONObject) -> String
    ): Flow<StreamResult> = flow {
        var accumulated = ""
        val executedTools = mutableSetOf<String>()

        try {
            val url = "https://openrouter.ai/api/v1/chat/completions"

            val requestJson = JSONObject().apply {
                put("model", "inclusionai/ling-3.0-flash:free")
                put("messages", org.json.JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemInstruction)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", promptText)
                    })
                })
                put("tools", ToolSchemaBuilder.buildOpenRouterToolsJson())
                put("tool_choice", "auto")
                put("stream", true)
                put("temperature", 0.3)
            }

            val httpRequest = Request.Builder()
                .url(url)
                .addHeader("Authorization", "Bearer $apiKey")
                .addHeader("HTTP-Referer", "https://devstudio.app")
                .addHeader("X-Title", "DevStudio Mobile IDE")
                .post(requestJson.toString().toRequestBody(jsonMediaType))
                .build()

            val response = client.newCall(httpRequest).execute()

            if (!response.isSuccessful) {
                val errBody = response.body?.string() ?: ""
                val errMessage = when (response.code) {
                    401 -> "API Key de OpenRouter no válida (401 Unauthorized)."
                    402 -> "Límite de créditos alcanzado en OpenRouter (402 Payment Required)."
                    429 -> "Límite de peticiones alcanzado (429 Rate Limit). Por favor espera un momento."
                    else -> "Error en OpenRouter (${response.code}): $errBody"
                }
                emit(parseStreamedContent("❌ $errMessage", defaultFilePath))
                return@flow
            }

            val responseBody = response.body ?: return@flow

            responseBody.byteStream().bufferedReader().use { reader ->
                var line: String?
                while (reader.readLine().also { line = it } != null) {
                    val rawLine = line?.trim() ?: continue
                    if (!rawLine.startsWith("data:")) continue

                    val data = rawLine.removePrefix("data:").trim()
                    if (data == "[DONE]") break

                    try {
                        val json = JSONObject(data)
                        val choices = json.optJSONArray("choices")
                        if (choices != null && choices.length() > 0) {
                            val firstChoice = choices.getJSONObject(0)
                            val delta = firstChoice.optJSONObject("delta")

                            val toolCalls = delta?.optJSONArray("tool_calls")
                            if (toolCalls != null && toolCalls.length() > 0) {
                                for (i in 0 until toolCalls.length()) {
                                    val tc = toolCalls.getJSONObject(i)
                                    val functionObj = tc.optJSONObject("function")
                                    val fnName = functionObj?.optString("name", "") ?: ""
                                    val fnArgsStr = functionObj?.optString("arguments", "") ?: "{}"

                                    if (fnName.isNotEmpty()) {
                                        val toolKey = "$fnName:$fnArgsStr"
                                        if (!executedTools.contains(toolKey)) {
                                            executedTools.add(toolKey)
                                            val fnArgs = try { JSONObject(fnArgsStr) } catch (e: Exception) { JSONObject() }

                                            accumulated += "\n🛠️ **Ejecutando herramienta (OpenRouter)**: `$fnName`...\n"
                                            emit(parseStreamedContent(accumulated, defaultFilePath))

                                            val toolResult = onExecuteTool(fnName, fnArgs)
                                            accumulated += "> $toolResult\n\n"
                                            emit(parseStreamedContent(accumulated, defaultFilePath))
                                        }
                                    }
                                }
                            }

                            val chunkText = delta?.optString("content", "") ?: ""
                            if (chunkText.isNotEmpty()) {
                                accumulated += chunkText
                                accumulated = checkAndExecuteEmbeddedToolCalls(accumulated, executedTools, onExecuteTool)
                                emit(parseStreamedContent(accumulated, defaultFilePath))
                            }
                        }
                    } catch (e: Exception) {
                        // Skip malformed chunks
                    }
                }
            }

            if (accumulated.isBlank()) {
                emit(parseStreamedContent("No se recibió contenido de OpenRouter.", defaultFilePath))
            }

        } catch (e: Exception) {
            emit(parseStreamedContent("❌ Excepción en streaming OpenRouter: ${e.localizedMessage}", defaultFilePath))
        }
    }

    private suspend fun checkAndExecuteEmbeddedToolCalls(
        text: String,
        executedTools: MutableSet<String>,
        onExecuteTool: suspend (toolName: String, args: JSONObject) -> String
    ): String {
        var updatedText = text
        val toolCallRegex = Regex("""```tool_call\s*\n?(\{[\s\S]*?\})\n?```""", RegexOption.IGNORE_CASE)

        toolCallRegex.findAll(text).forEach { match ->
            val jsonStr = match.groupValues.getOrNull(1) ?: return@forEach
            val toolKey = "embedded:$jsonStr"

            if (!executedTools.contains(toolKey)) {
                executedTools.add(toolKey)
                try {
                    val jsonObj = JSONObject(jsonStr)
                    val fnName = jsonObj.optString("name")
                    val fnArgs = jsonObj.optJSONObject("args") ?: JSONObject()

                    val result = onExecuteTool(fnName, fnArgs)
                    updatedText += "\n\n🛠️ **Herramienta invocada**: `$fnName`\n> $result\n\n"
                } catch (e: Exception) {
                    updatedText += "\n⚠️ Error al interpretar herramienta: ${e.localizedMessage}\n"
                }
            }
        }
        return updatedText
    }

    private fun parseStreamedContent(accumulatedText: String, defaultFilePath: String?): StreamResult {
        if (accumulatedText.isBlank()) {
            return StreamResult("", "", null, null)
        }

        val filePathRegex = Regex("""(?:Ruta del archivo|### ARCHIVO|Archivo|File):\s*([^\s\n]+)""", RegexOption.IGNORE_CASE)
        val filePathMatch = filePathRegex.find(accumulatedText)
        val extractedPath = filePathMatch?.groupValues?.getOrNull(1)?.trim()?.removeSurrounding("`")

        val codeBlockRegex = Regex("""```(?:html|css|js|javascript|json|markdown|cpp|rust|kotlin)?\s*\n?([\s\S]*?)(?:```|$)""", RegexOption.IGNORE_CASE)
        val codeMatch = codeBlockRegex.find(accumulatedText)
        val proposedCode = codeMatch?.groupValues?.getOrNull(1)?.takeIf { it.isNotBlank() }

        val targetFilePath = when {
            extractedPath != null -> extractedPath
            proposedCode != null -> defaultFilePath ?: "/index.html"
            else -> null
        }

        var explanation = accumulatedText
        if (codeMatch != null && proposedCode != null) {
            val codeStartIndex = codeMatch.range.first
            val textBeforeCode = accumulatedText.substring(0, codeStartIndex).trim()
            explanation = if (textBeforeCode.isNotBlank()) {
                textBeforeCode.replace(filePathRegex, "").trim()
            } else {
                "He preparado el código propuesto para ${targetFilePath ?: "el archivo"}."
            }
        } else if (filePathMatch != null) {
            explanation = accumulatedText.replace(filePathRegex, "").trim()
        }

        return StreamResult(
            fullText = accumulatedText,
            explanation = explanation.ifBlank { accumulatedText },
            targetFilePath = targetFilePath,
            proposedCode = proposedCode
        )
    }
}
