package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.data.api.AiProvider
import com.example.data.api.ChatMode
import com.example.data.db.ChatMessageEntity
import com.example.ui.components.chat.ChatHeader
import com.example.ui.components.chat.ChatInputArea
import com.example.ui.components.chat.ChatMessageList
import com.example.ui.components.chat.ChatModeBar
import com.example.ui.theme.EditorBorder
import com.example.ui.theme.EditorSurface

@Composable
fun AiAgentChatSheet(
    messages: List<ChatMessageEntity>,
    isAiLoading: Boolean,
    selectedProvider: AiProvider,
    selectedChatMode: ChatMode = ChatMode.STEP_BY_STEP,
    openRouterApiKey: String,
    onSelectProvider: (AiProvider) -> Unit,
    onSelectChatMode: (ChatMode) -> Unit = {},
    onOpenSettings: () -> Unit,
    onSendPrompt: (String) -> Unit,
    onApplyProposedCode: (ChatMessageEntity) -> Unit,
    onClearChat: () -> Unit = {},
    onCloseChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxHeight(0.85f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(EditorSurface)
            .border(1.dp, EditorBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        ChatHeader(
            isAiLoading = isAiLoading,
            selectedChatMode = selectedChatMode,
            selectedProvider = selectedProvider,
            onOpenSettings = onOpenSettings,
            onClearChat = onClearChat,
            onCloseChat = onCloseChat
        )

        ChatModeBar(
            selectedChatMode = selectedChatMode,
            selectedProvider = selectedProvider,
            openRouterApiKey = openRouterApiKey,
            onSelectChatMode = onSelectChatMode,
            onSelectProvider = onSelectProvider,
            onOpenSettings = onOpenSettings
        )

        ChatMessageList(
            messages = messages,
            isAiLoading = isAiLoading,
            selectedProvider = selectedProvider,
            onApplyProposedCode = onApplyProposedCode,
            modifier = Modifier.weight(1f)
        )

        ChatInputArea(
            onSendPrompt = onSendPrompt
        )
    }
}
