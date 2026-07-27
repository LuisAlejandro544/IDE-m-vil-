package com.example.data.api.service

import com.example.data.api.StreamResult
import org.json.JSONObject

object AiContentParser {

    fun JSONObject.optCleanString(key: String, defaultValue: String = ""): String {
        if (this.isNull(key)) return defaultValue
        val value = this.optString(key, defaultValue)
        return if (value == "null") defaultValue else value
    }

    suspend fun checkAndExecuteEmbeddedToolCalls(
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
                try {
                    val jsonObj = JSONObject(jsonStr)
                    val fnName = jsonObj.optCleanString("name")
                    val fnArgs = jsonObj.optJSONObject("args") ?: JSONObject()

                    if (fnName.isNotBlank()) {
                        executedTools.add(toolKey)
                        val result = onExecuteTool(fnName, fnArgs)
                        updatedText += "\n[TOOL_EXEC:$fnName:$result]\n"
                    }
                } catch (e: Exception) {
                    // Ignore incomplete JSON during streaming
                }
            }
        }
        return updatedText
    }

    fun parseStreamedContent(accumulatedText: String, defaultFilePath: String?): StreamResult {
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

        var explanation = accumulatedText.replace("nullnull", "")
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
