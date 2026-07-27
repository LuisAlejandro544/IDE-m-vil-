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
import org.json.JSONArray
import org.json.JSONObject
import java.util.concurrent.TimeUnit

enum class AiProvider(
    val id: String,
    val displayName: String,
    val modelName: String,
    val providerBadge: String
) {
    GEMINI(
        id = "gemini",
        displayName = "Google Gemini",
        modelName = "gemini-3.5-flash",
        providerBadge = "Gemini 3.5 Flash + Tools"
    ),
    OPENROUTER(
        id = "openrouter",
        displayName = "OpenRouter AI",
        modelName = "inclusionai/ling-3.0-flash:free",
        providerBadge = "Ling 3.0 Flash + Tools"
    )
}

data class StreamResult(
    val fullText: String,
    val explanation: String,
    val targetFilePath: String?,
    val proposedCode: String?
)

class AiAgentService {

    private val client = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun streamUserPrompt(
        provider: AiProvider,
        userPrompt: String,
        currentFileContent: String?,
        currentFilePath: String?,
        allFilesSummary: String,
        openRouterApiKey: String,
        customGeminiApiKey: String,
        onExecuteTool: suspend (toolName: String, args: JSONObject) -> String
    ): Flow<StreamResult> = flow {
        val systemInstruction = """
            Eres un Agente de Inteligencia Artificial Avanzado para el IDE móvil DevStudio.
            Tienes acceso a HERRAMIENTAS en tiempo real para modificar y consultar la estructura del proyecto directamente.

            HERRAMIENTAS DISPONIBLES:
            1. get_project_structure() -> Devuelve el árbol completo de archivos y carpetas del proyecto.
            2. read_file(path) -> Lee el contenido completo de un archivo (ej: /index.html).
            3. edit_file(path, target_content, replacement_content) -> Reemplaza líneas o bloques exactos de código en un archivo.
            4. create_file(path, content) -> Crea un nuevo archivo con el contenido proporcionado.
            5. delete_file(path) -> Elimina un archivo o carpeta.

            INSTRUCCIONES DE USO DE HERRAMIENTAS:
            - Si el usuario te pide crear, editar o eliminar un archivo, INVOCA directamente la herramienta correspondiente.
            - Puedes invocar herramientas escribiendo en tu respuesta el siguiente bloque JSON:
            ```tool_call
            {"name": "nombre_herramienta", "args": {"path": "/index.html", ...}}
            ```
            - También puedes responder con explicaciones claras y código propuesto en bloques de código markdown cuando sea oportuno.
            - Explicación breve en español sobre tus acciones.

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

    private fun buildGeminiToolsJson(): JSONArray {
        return JSONArray().apply {
            put(JSONObject().apply {
                put("functionDeclarations", JSONArray().apply {
                    put(JSONObject().apply {
                        put("name", "get_project_structure")
                        put("description", "Obtiene el árbol completo de carpetas y archivos en el proyecto")
                    })
                    put(JSONObject().apply {
                        put("name", "read_file")
                        put("description", "Lee el contenido de un archivo del proyecto")
                        put("parameters", JSONObject().apply {
                            put("type", "OBJECT")
                            put("properties", JSONObject().apply {
                                put("path", JSONObject().put("type", "STRING").put("description", "Ruta del archivo, ej: /index.html"))
                            })
                            put("required", JSONArray().put("path"))
                        })
                    })
                    put(JSONObject().apply {
                        put("name", "edit_file")
                        put("description", "Edita un archivo reemplazando target_content por replacement_content")
                        put("parameters", JSONObject().apply {
                            put("type", "OBJECT")
                            put("properties", JSONObject().apply {
                                put("path", JSONObject().put("type", "STRING").put("description", "Ruta del archivo"))
                                put("target_content", JSONObject().put("type", "STRING").put("description", "Texto exacto a reemplazar"))
                                put("replacement_content", JSONObject().put("type", "STRING").put("description", "Nuevo texto reemplazo"))
                            })
                            put("required", JSONArray().put("path").put("target_content").put("replacement_content"))
                        })
                    })
                    put(JSONObject().apply {
                        put("name", "create_file")
                        put("description", "Crea un nuevo archivo con el contenido dado")
                        put("parameters", JSONObject().apply {
                            put("type", "OBJECT")
                            put("properties", JSONObject().apply {
                                put("path", JSONObject().put("type", "STRING").put("description", "Ruta del nuevo archivo"))
                                put("content", JSONObject().put("type", "STRING").put("description", "Contenido inicial del archivo"))
                            })
                            put("required", JSONArray().put("path").put("content"))
                        })
                    })
                    put(JSONObject().apply {
                        put("name", "delete_file")
                        put("description", "Elimina un archivo o carpeta")
                        put("parameters", JSONObject().apply {
                            put("type", "OBJECT")
                            put("properties", JSONObject().apply {
                                put("path", JSONObject().put("type", "STRING").put("description", "Ruta a eliminar"))
                            })
                            put("required", JSONArray().put("path"))
                        })
                    })
                })
            })
        }
    }

    private fun buildOpenRouterToolsJson(): JSONArray {
        return JSONArray().apply {
            put(JSONObject().apply {
                put("type", "function")
                put("function", JSONObject().apply {
                    put("name", "get_project_structure")
                    put("description", "Obtiene la lista de todos los archivos y carpetas del proyecto")
                })
            })
            put(JSONObject().apply {
                put("type", "function")
                put("function", JSONObject().apply {
                    put("name", "read_file")
                    put("description", "Lee el contenido de un archivo del proyecto")
                    put("parameters", JSONObject().apply {
                        put("type", "object")
                        put("properties", JSONObject().apply {
                            put("path", JSONObject().put("type", "string"))
                        })
                        put("required", JSONArray().put("path"))
                    })
                })
            })
            put(JSONObject().apply {
                put("type", "function")
                put("function", JSONObject().apply {
                    put("name", "edit_file")
                    put("description", "Edita un archivo reemplazando target_content por replacement_content")
                    put("parameters", JSONObject().apply {
                        put("type", "object")
                        put("properties", JSONObject().apply {
                            put("path", JSONObject().put("type", "string"))
                            put("target_content", JSONObject().put("type", "string"))
                            put("replacement_content", JSONObject().put("type", "string"))
                        })
                        put("required", JSONArray().put("path").put("target_content").put("replacement_content"))
                    })
                })
            })
            put(JSONObject().apply {
                put("type", "function")
                put("function", JSONObject().apply {
                    put("name", "create_file")
                    put("description", "Crea un nuevo archivo con contenido")
                    put("parameters", JSONObject().apply {
                        put("type", "object")
                        put("properties", JSONObject().apply {
                            put("path", JSONObject().put("type", "string"))
                            put("content", JSONObject().put("type", "string"))
                        })
                        put("required", JSONArray().put("path").put("content"))
                    })
                })
            })
            put(JSONObject().apply {
                put("type", "function")
                put("function", JSONObject().apply {
                    put("name", "delete_file")
                    put("description", "Elimina un archivo o carpeta")
                    put("parameters", JSONObject().apply {
                        put("type", "object")
                        put("properties", JSONObject().apply {
                            put("path", JSONObject().put("type", "string"))
                        })
                        put("required", JSONArray().put("path"))
                    })
                })
            })
        }
    }

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
                    put("parts", JSONArray().put(JSONObject().put("text", systemInstruction)))
                })
                put("contents", JSONArray().put(
                    JSONObject().apply {
                        put("parts", JSONArray().put(JSONObject().put("text", promptText)))
                    }
                ))
                put("tools", buildGeminiToolsJson())
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

                                    // Check Function Call in Gemini API candidate
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
                        // Ignore unparseable partial array boundary lines
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
                put("messages", JSONArray().apply {
                    put(JSONObject().apply {
                        put("role", "system")
                        put("content", systemInstruction)
                    })
                    put(JSONObject().apply {
                        put("role", "user")
                        put("content", promptText)
                    })
                })
                put("tools", buildOpenRouterToolsJson())
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

                            // Check Tool Calls in OpenRouter JSON delta
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
                        // Skip malformed chunk
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
