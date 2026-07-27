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

class GeminiStreamProvider(
    private val client: OkHttpClient
) {
    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    fun streamGemini(
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
                    AiContentParser.parseStreamedContent(
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
                                        val fnName = funcCall.optCleanString("name")
                                        val fnArgs = funcCall.optJSONObject("args") ?: JSONObject()
                                        val toolKey = "$fnName:$fnArgs"

                                        if (fnName.isNotBlank() && !executedTools.contains(toolKey)) {
                                            executedTools.add(toolKey)
                                            val toolResult = onExecuteTool(fnName, fnArgs)
                                            accumulated += "\n[TOOL_EXEC:$fnName:$toolResult]\n"
                                            emit(AiContentParser.parseStreamedContent(accumulated, defaultFilePath))
                                        }
                                    }

                                    val textChunk = part.optCleanString("text", "")
                                    if (textChunk.isNotEmpty()) {
                                        accumulated += textChunk
                                        accumulated = AiContentParser.checkAndExecuteEmbeddedToolCalls(accumulated, executedTools, onExecuteTool)
                                        emit(AiContentParser.parseStreamedContent(accumulated, defaultFilePath))
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
                emit(AiContentParser.parseStreamedContent("No se recibió respuesta de Gemini.", defaultFilePath))
            }

        } catch (e: Exception) {
            emit(AiContentParser.parseStreamedContent("❌ Excepción en streaming Gemini: ${e.localizedMessage}", defaultFilePath))
        }
    }
}
