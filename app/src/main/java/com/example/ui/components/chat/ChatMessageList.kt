package com.example.ui.components.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.AiProvider
import com.example.data.db.ChatMessageEntity
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.EditorBackground
import com.example.ui.theme.EditorBorder
import com.example.ui.theme.EditorPanelHeader
import com.example.ui.theme.LineNumberColor
import com.example.ui.theme.SoftGreen

@Composable
fun ChatMessageList(
    messages: List<ChatMessageEntity>,
    isAiLoading: Boolean,
    selectedProvider: AiProvider,
    onApplyProposedCode: (ChatMessageEntity) -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
        contentPadding = PaddingValues(vertical = 8.dp)
    ) {
        items(messages, key = { it.id }) { msg ->
            val isUser = msg.sender == "user"
            val parsed = if (isUser) ParsedMessageContent(msg.text, emptyList()) else parseChatMessageContent(msg.text)

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

                    if (parsed.cleanText.isNotBlank()) {
                        com.example.ui.components.markdown.MarkdownRenderer(
                            markdownText = parsed.cleanText,
                            textColor = if (isUser) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Render Tool Pills (Pastillas)
                    if (parsed.toolPills.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                            parsed.toolPills.forEach { pill ->
                                ToolPillItem(pill = pill)
                            }
                        }
                    }

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
}

private fun String?.isNull_or_blank_or_null(): Boolean {
    return this == null || this.isBlank() || this == "null"
}

private data class ParsedMessageContent(
    val cleanText: String,
    val toolPills: List<ToolPillData>
)

private fun parseChatMessageContent(rawText: String): ParsedMessageContent {
    var text = rawText
    val pills = mutableListOf<ToolPillData>()

    // 1. Structured tag format: [TOOL_EXEC:toolName:summary]
    val tagRegex = Regex("""\[TOOL_EXEC:([^:]+):([\s\S]*?)\]""")
    tagRegex.findAll(text).forEach { match ->
        val name = match.groupValues.getOrNull(1)?.trim() ?: ""
        val summary = match.groupValues.getOrNull(2)?.trim() ?: ""
        if (name.isNotEmpty()) {
            pills.add(ToolPillData(name, summary))
        }
    }
    text = tagRegex.replace(text, "")

    // 2. Strip raw tool_call blocks from main visible chat text
    val embeddedToolRegex = Regex("""```tool_call[\s\S]*?(?:```|$)""", RegexOption.IGNORE_CASE)
    text = embeddedToolRegex.replace(text, "")

    // 3. Legacy / markdown stream format: 🛠️ **...**: `toolName`...\n> summary
    val legacyRegex = Regex("""(?:🛠️\s*)?\*\*(?:Ejecutando herramienta|Herramienta|Executing tool)[^*]*\*\*:?\s*`([^`]+)`[^\n]*\n?>?\s*([^\n]+)?""", RegexOption.IGNORE_CASE)
    legacyRegex.findAll(text).forEach { match ->
        val name = match.groupValues.getOrNull(1)?.trim() ?: ""
        val summary = match.groupValues.getOrNull(2)?.trim() ?: ""
        if (name.isNotEmpty() && pills.none { it.toolName == name }) {
            pills.add(ToolPillData(name, summary))
        }
    }
    text = legacyRegex.replace(text, "")

    // 3. Clean up nullnull, stray null, and excessive line breaks
    text = text.replace("nullnull", "")
        .replace(Regex("""^\s*null\s*$""", RegexOption.MULTILINE), "")
        .replace(Regex("""\n{3,}"""), "\n\n")
        .trim()

    return ParsedMessageContent(
        cleanText = text,
        toolPills = pills
    )
}
