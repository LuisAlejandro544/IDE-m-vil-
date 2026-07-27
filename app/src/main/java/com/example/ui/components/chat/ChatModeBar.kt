package com.example.ui.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.AiProvider
import com.example.data.api.ChatMode
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.EditorBackground
import com.example.ui.theme.EditorBorder
import com.example.ui.theme.EditorPanelHeader
import com.example.ui.theme.LineNumberColor

@Composable
fun ChatModeBar(
    selectedChatMode: ChatMode,
    selectedProvider: AiProvider,
    openRouterApiKey: String,
    onSelectChatMode: (ChatMode) -> Unit,
    onSelectProvider: (AiProvider) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.fillMaxWidth()) {
        // Mode Selector Bar
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
    }
}
