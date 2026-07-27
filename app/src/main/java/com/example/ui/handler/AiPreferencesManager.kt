package com.example.ui.handler

import android.content.Context
import android.content.SharedPreferences
import com.example.data.api.AiProvider
import com.example.data.api.ChatMode

class AiPreferencesManager(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences("devstudio_ai_prefs", Context.MODE_PRIVATE)

    fun loadSelectedProvider(): AiProvider {
        val savedName = prefs.getString("ai_provider", AiProvider.GEMINI.name) ?: AiProvider.GEMINI.name
        return try { AiProvider.valueOf(savedName) } catch (e: Exception) { AiProvider.GEMINI }
    }

    fun loadSelectedChatMode(): ChatMode {
        val savedName = prefs.getString("chat_mode", ChatMode.STEP_BY_STEP.name) ?: ChatMode.STEP_BY_STEP.name
        return try { ChatMode.valueOf(savedName) } catch (e: Exception) { ChatMode.STEP_BY_STEP }
    }

    fun loadOpenRouterApiKey(): String {
        return prefs.getString("openrouter_api_key", "") ?: ""
    }

    fun loadCustomGeminiApiKey(): String {
        return prefs.getString("gemini_api_key", "") ?: ""
    }

    fun savePreferences(
        provider: AiProvider,
        chatMode: ChatMode,
        openRouterApiKey: String,
        geminiApiKey: String
    ) {
        prefs.edit()
            .putString("ai_provider", provider.name)
            .putString("chat_mode", chatMode.name)
            .putString("openrouter_api_key", openRouterApiKey)
            .putString("gemini_api_key", geminiApiKey)
            .apply()
    }
}
