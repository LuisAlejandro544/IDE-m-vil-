package com.example.data.api

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

enum class ChatMode(
    val id: String,
    val displayName: String,
    val iconEmoji: String,
    val description: String
) {
    PLANNING(
        id = "planning",
        displayName = "Chat",
        iconEmoji = "💬",
        description = "Planifica, analiza la estructura del proyecto y da ideas sin hacer cambios en archivos."
    ),
    STEP_BY_STEP(
        id = "step_by_step",
        displayName = "Paso a Paso",
        iconEmoji = "🐾",
        description = "Muestra qué Agente hará cada tarea y solicita confirmación con botón 'Aceptar'."
    ),
    FULL_AUTONOMOUS(
        id = "full_autonomous",
        displayName = "Código Completo",
        iconEmoji = "🚀",
        description = "El Agente Director y Sub-Agentes aplican los cambios automáticamente sin confirmación."
    )
}

data class StreamResult(
    val fullText: String,
    val explanation: String,
    val targetFilePath: String?,
    val proposedCode: String?
)

