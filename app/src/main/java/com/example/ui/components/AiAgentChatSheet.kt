package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.AiProvider
import com.example.data.api.ChatMode
import com.example.data.db.ChatMessageEntity
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.EditorBackground
import com.example.ui.theme.EditorBorder
import com.example.ui.theme.EditorPanelHeader
import com.example.ui.theme.EditorSurface
import com.example.ui.theme.LineNumberColor
import com.example.ui.theme.SoftGreen

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
    onCloseChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    var inputText by remember { mutableStateOf("") }

    val quickPrompts = listOf(
        "🛠️ Muestra la estructura del proyecto",
        "✏️ Cambia el título de /index.html",
        "📄 Lee /style.css y sugiere mejoras",
        "➕ Crea un archivo app.js"
    )

    Column(
        modifier = modifier
            .fillMaxHeight(0.85f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
            .background(EditorSurface)
            .border(1.dp, EditorBorder, RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp))
    ) {
        // Chat Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(EditorPanelHeader)
                .padding(horizontal = 16.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(AccentBlue),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = EditorBackground,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Agente Director & Sub-Agentes",
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = if (isAiLoading) "⚡ Agentes trabajando..." else "${selectedChatMode.iconEmoji} Modo ${selectedChatMode.displayName} • ${selectedProvider.displayName}",
                    fontSize = 11.sp,
                    color = if (isAiLoading) AccentBlue else SoftGreen,
                    fontWeight = FontWeight.SemiBold
                )
            }

            IconButton(onClick = onOpenSettings) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = "Configurar API Keys",
                    tint = AccentBlue
                )
            }

            IconButton(onClick = onCloseChat) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Cerrar chat",
                    tint = LineNumberColor
                )
            }
        }

        // Mode Selector Bar (Chat vs Paso a Paso vs Código Completo)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(EditorBackground)
                .border(1.dp, EditorBorder)
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Modo:",
                fontSize = 11.sp,
                color = LineNumberColor,
                fontWeight = FontWeight.Bold
            )

            ChatMode.values().forEach { mode ->
                val isSelected = mode == selectedChatMode
                FilterChip(
                    selected = isSelected,
                    onClick = { onSelectChatMode(mode) },
                    label = {
                        Text(
                            text = "${mode.iconEmoji} ${mode.displayName}",
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = AccentBlue,
                        selectedLabelColor = EditorBackground,
                        containerColor = EditorPanelHeader,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    modifier = Modifier.height(28.dp)
                )
            }
        }

        // Active Mode Description Banner
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(AccentBlue.copy(alpha = 0.08f))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(
                text = "${selectedChatMode.iconEmoji} ${selectedChatMode.description}",
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
        }

        // Provider Selector Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(EditorPanelHeader.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Modelo:",
                fontSize = 10.sp,
                color = LineNumberColor,
                fontWeight = FontWeight.Bold
            )

            AiProvider.values().forEach { provider ->
                val isSelected = provider == selectedProvider
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(if (isSelected) AccentBlue else EditorBackground)
                        .border(1.dp, if (isSelected) AccentBlue else EditorBorder, RoundedCornerShape(4.dp))
                        .clickable { onSelectProvider(provider) }
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = provider.displayName,
                        fontSize = 10.sp,
                        color = if (isSelected) EditorBackground else MaterialTheme.colorScheme.onSurface,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                    )
                }
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Sub-Agentes: 🏗️ 🎨 ⚡ 🛡️",
                fontSize = 10.sp,
                color = AccentBlue,
                fontWeight = FontWeight.SemiBold
            )
        }

        // Active Skills Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(EditorPanelHeader.copy(alpha = 0.5f))
                .padding(horizontal = 12.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = "Skills:",
                fontSize = 10.sp,
                color = LineNumberColor,
                fontWeight = FontWeight.Bold
            )
            val skills = listOf("🛠️ Tool Usage .md", "🎨 UI/UX Design", "📱 Responsive Layout", "⚡ Clean Logic")
            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items(skills) { skill ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(AccentBlue.copy(alpha = 0.15f))
                            .border(1.dp, AccentBlue.copy(alpha = 0.3f), RoundedCornerShape(4.dp))
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = skill,
                            fontSize = 10.sp,
                            color = AccentBlue,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }

        // Warning banner if OpenRouter is selected and key is empty
        if (selectedProvider == AiProvider.OPENROUTER && openRouterApiKey.isBlank()) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.2f))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.Warning,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.error,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Requiere API Key de OpenRouter",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.weight(1f)
                )
                Button(
                    onClick = onOpenSettings,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 2.dp),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.height(26.dp)
                ) {
                    Icon(imageVector = Icons.Default.Key, contentDescription = null, modifier = Modifier.size(12.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Ingresar", fontSize = 10.sp)
                }
            }
        }

        // Quick Suggestion Chips
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(quickPrompts) { prompt ->
                SuggestionChip(
                    onClick = { inputText = prompt },
                    label = { Text(prompt, fontSize = 11.sp) },
                    colors = SuggestionChipDefaults.suggestionChipColors(
                        containerColor = EditorPanelHeader,
                        labelColor = MaterialTheme.colorScheme.onSurface
                    ),
                    border = SuggestionChipDefaults.suggestionChipBorder(
                        borderColor = EditorBorder,
                        enabled = true
                    )
                )
            }
        }

        // Message List
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(horizontal = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
            contentPadding = PaddingValues(vertical = 8.dp)
        ) {
            items(messages, key = { it.id }) { msg ->
                val isUser = msg.sender == "user"

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = if (isUser) Arrangement.End else Arrangement.Start
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth(0.88f)
                            .clip(
                                RoundedCornerShape(
                                    topStart = 12.dp,
                                    topEnd = 12.dp,
                                    bottomStart = if (isUser) 12.dp else 2.dp,
                                    bottomEnd = if (isUser) 2.dp else 12.dp
                                )
                            )
                            .background(if (isUser) AccentBlue else EditorPanelHeader)
                            .border(1.dp, if (isUser) AccentBlue else EditorBorder)
                            .padding(12.dp)
                    ) {
                        Text(
                            text = if (isUser) "Tú" else "Agente IA (${selectedProvider.displayName})",
                            fontWeight = FontWeight.Bold,
                            fontSize = 11.sp,
                            color = if (isUser) EditorBackground else AccentBlue
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = msg.text,
                            fontSize = 13.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 18.sp
                        )

                        // Proposed Code Box
                        if (!msg.proposedCode.isNull_or_blank_or_null()) {
                            Spacer(modifier = Modifier.height(10.dp))

                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(EditorBackground)
                                    .padding(8.dp)
                            ) {
                                Column {
                                    Text(
                                        text = "Código propuesto para ${msg.targetFilePath ?: "archivo"}:",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = SoftGreen,
                                        fontFamily = FontFamily.Monospace
                                    )
                                    Spacer(modifier = Modifier.height(4.dp))
                                    val code = msg.proposedCode ?: ""
                                    Text(
                                        text = code.take(300) + if (code.length > 300) "..." else "",
                                        fontSize = 11.sp,
                                        color = LineNumberColor,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(8.dp))

                            Button(
                                onClick = { onApplyProposedCode(msg) },
                                enabled = !msg.isApplied,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (msg.isApplied) EditorBorder else SoftGreen
                                ),
                                shape = RoundedCornerShape(6.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(
                                    imageVector = if (msg.isApplied) Icons.Default.Check else Icons.Default.AutoAwesome,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (msg.isApplied) "Cambios Aplicados" else "✨ Aplicar Cambios al Archivo",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            if (isAiLoading) {
                item {
                    Row(
                        modifier = Modifier.padding(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = AccentBlue,
                            strokeWidth = 2.dp
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Streaming en tiempo real (${selectedProvider.modelName})...",
                            fontSize = 12.sp,
                            color = LineNumberColor
                        )
                    }
                }
            }
        }

        // Input Field Area
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(EditorPanelHeader)
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            OutlinedTextField(
                value = inputText,
                onValueChange = { inputText = it },
                placeholder = { Text("Escribe instrucciones al agente...", fontSize = 13.sp) },
                modifier = Modifier.weight(1f),
                shape = RoundedCornerShape(20.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = AccentBlue,
                    unfocusedBorderColor = EditorBorder,
                    focusedContainerColor = EditorBackground,
                    unfocusedContainerColor = EditorBackground
                ),
                maxLines = 3
            )

            Spacer(modifier = Modifier.width(8.dp))

            IconButton(
                onClick = {
                    if (inputText.isNotBlank()) {
                        onSendPrompt(inputText)
                        inputText = ""
                    }
                },
                modifier = Modifier
                    .size(44.dp)
                    .clip(CircleShape)
                    .background(AccentBlue)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.Send,
                    contentDescription = "Enviar prompt",
                    tint = EditorBackground,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

private fun String?.isNull_or_blank_or_null(): Boolean {
    return this == null || this.isBlank() || this == "null"
}
