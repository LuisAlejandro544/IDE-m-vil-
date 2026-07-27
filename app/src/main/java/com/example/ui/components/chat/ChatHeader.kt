package com.example.ui.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.example.ui.theme.EditorPanelHeader
import com.example.ui.theme.LineNumberColor
import com.example.ui.theme.SoftGreen
import com.example.ui.theme.SoftRed

@Composable
fun ChatHeader(
    isAiLoading: Boolean,
    selectedChatMode: ChatMode,
    selectedProvider: AiProvider,
    onOpenSettings: () -> Unit,
    onClearChat: () -> Unit,
    onCloseChat: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
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

        IconButton(onClick = onClearChat) {
            Icon(
                imageVector = Icons.Default.DeleteOutline,
                contentDescription = "Limpiar Chat y Empezar de Nuevo",
                tint = SoftRed
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
}
