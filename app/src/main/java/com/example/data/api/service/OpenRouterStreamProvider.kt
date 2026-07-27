package com.example.data.api.service

import com.example.data.api.StreamResult
import com.example.data.api.ToolSchemaBuilder
import com.example.data.api.service.AiContentParser.optCleanString
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject

class OpenRouterStreamProvider(
    private val client: OkHttpClient
) {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun streamOpenRouter(
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
                emit(AiContentParser.parseStreamedContent("❌ $errMessage", defaultFilePath))
                return@flow
            }

            val responseBody = response.body ?: return@flow

            val pendingToolCalls = mutableMapOf<Int, ToolCallAccumulator>()

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
                                    val index = tc.optInt("index", i)
                                    val acc = pendingToolCalls.getOrPut(index) { ToolCallAccumulator() }

                                    val functionObj = tc.optJSONObject("function")
                                    if (functionObj != null) {
                                        if (functionObj.has("name")) {
                                            acc.name.append(functionObj.optString("name", ""))
                                        }
                                        if (functionObj.has("arguments")) {
                                            acc.arguments.append(functionObj.optString("arguments", ""))
                                        }
                                    }

                                    val fnName = acc.name.toString().trim()
                                    val argsStr = acc.arguments.toString().trim()
                                    val parsedArgs = try { JSONObject(argsStr) } catch (e: Exception) { null }

                                    if (fnName.isNotBlank() && parsedArgs != null && !acc.executed) {
                                        acc.executed = true
                                        val toolResult = onExecuteTool(fnName, parsedArgs)
                                        accumulated += "\n[TOOL_EXEC:$fnName:$toolResult]\n"
                                        emit(AiContentParser.parseStreamedContent(accumulated, defaultFilePath))
                                    }
                                }
                            }

                            val chunkText = delta?.optCleanString("content", "") ?: ""
                            if (chunkText.isNotEmpty()) {
                                accumulated += chunkText
                                accumulated = AiContentParser.checkAndExecuteEmbeddedToolCalls(accumulated, executedTools, onExecuteTool)
                                emit(AiContentParser.parseStreamedContent(accumulated, defaultFilePath))
                            }
                        }
                    } catch (e: Exception) {
                        // Skip malformed chunks
                    }
                }
            }

            // Execute any remaining tool call that wasn't triggered during stream chunks
            pendingToolCalls.values.forEach { acc ->
                if (!acc.executed) {
                    val fnName = acc.name.toString().trim()
                    val argsStr = acc.arguments.toString().trim()
                    if (fnName.isNotBlank()) {
                        acc.executed = true
                        val parsedArgs = try { JSONObject(argsStr) } catch (e: Exception) { JSONObject() }
                        val toolResult = onExecuteTool(fnName, parsedArgs)
                        accumulated += "\n[TOOL_EXEC:$fnName:$toolResult]\n"
                        emit(AiContentParser.parseStreamedContent(accumulated, defaultFilePath))
                    }
                }
            }

            if (accumulated.isBlank()) {
                emit(AiContentParser.parseStreamedContent("No se recibió contenido de OpenRouter.", defaultFilePath))
            }

        } catch (e: Exception) {
            emit(AiContentParser.parseStreamedContent("❌ Excepción en streaming OpenRouter: ${e.localizedMessage}", defaultFilePath))
        }
    }
}

private class ToolCallAccumulator {
    val name = StringBuilder()
    val arguments = StringBuilder()
    var executed = false
}
