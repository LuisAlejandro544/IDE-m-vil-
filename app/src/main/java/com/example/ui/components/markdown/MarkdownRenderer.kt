package com.example.ui.components.markdown

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.EditorBackground
import com.example.ui.theme.EditorBorder
import com.example.ui.theme.EditorPanelHeader
import com.example.ui.theme.LineNumberColor
import com.example.ui.theme.SoftGreen
import com.example.ui.theme.SoftRed

@Composable
fun MarkdownRenderer(
    markdownText: String,
    modifier: Modifier = Modifier,
    textColor: Color = MaterialTheme.colorScheme.onSurface
) {
    val blocks = remember(markdownText) { parseMarkdownBlocks(markdownText) }

    SelectionContainer(modifier = modifier) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            blocks.forEach { block ->
                when (block) {
                    is MarkdownBlock.Heading -> {
                        val (size, weight, color) = when (block.level) {
                            1 -> Triple(20.sp, FontWeight.Bold, AccentBlue)
                            2 -> Triple(17.sp, FontWeight.Bold, MaterialTheme.colorScheme.primary)
                            3 -> Triple(15.sp, FontWeight.SemiBold, MaterialTheme.colorScheme.onSurface)
                            else -> Triple(14.sp, FontWeight.Medium, MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        Column(modifier = Modifier.fillMaxWidth().padding(top = 6.dp, bottom = 2.dp)) {
                            Text(
                                text = parseInlineMarkdown(block.text),
                                fontSize = size,
                                fontWeight = weight,
                                color = color,
                                lineHeight = (size.value * 1.3f).sp
                            )
                            if (block.level <= 2) {
                                Spacer(modifier = Modifier.height(4.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(EditorBorder)
                                )
                            }
                        }
                    }

                    is MarkdownBlock.Paragraph -> {
                        Text(
                            text = parseInlineMarkdown(block.text),
                            fontSize = 13.sp,
                            color = textColor,
                            lineHeight = 19.sp
                        )
                    }

                    is MarkdownBlock.ListItem -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(start = (block.depth * 12).dp),
                            verticalAlignment = Alignment.Top
                        ) {
                            if (block.isTask) {
                                Icon(
                                    imageVector = if (block.isChecked) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                                    contentDescription = null,
                                    tint = if (block.isChecked) SoftGreen else LineNumberColor,
                                    modifier = Modifier
                                        .size(16.dp)
                                        .padding(top = 2.dp)
                                )
                            } else {
                                Box(
                                    modifier = Modifier
                                        .padding(top = 7.dp, end = 8.dp)
                                        .size(6.dp)
                                        .clip(CircleShape)
                                        .background(AccentBlue)
                                )
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = parseInlineMarkdown(block.text),
                                fontSize = 13.sp,
                                color = textColor,
                                lineHeight = 19.sp,
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }

                    is MarkdownBlock.CodeBlock -> {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(EditorBackground)
                                .border(1.dp, EditorBorder, RoundedCornerShape(8.dp))
                        ) {
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .background(EditorPanelHeader)
                                    .padding(horizontal = 10.dp, vertical = 4.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Icon(
                                        imageVector = Icons.Default.Code,
                                        contentDescription = null,
                                        tint = AccentBlue,
                                        modifier = Modifier.size(14.dp)
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Text(
                                        text = block.language.ifBlank { "código" }.uppercase(),
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.Bold,
                                        color = LineNumberColor,
                                        fontFamily = FontFamily.Monospace
                                    )
                                }
                            }
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .horizontalScroll(rememberScrollState())
                                    .padding(10.dp)
                            ) {
                                Text(
                                    text = block.code,
                                    fontSize = 12.sp,
                                    color = Color(0xFFE2E8F0),
                                    fontFamily = FontFamily.Monospace,
                                    lineHeight = 18.sp
                                )
                            }
                        }
                    }

                    is MarkdownBlock.BlockQuote -> {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(4.dp))
                                .background(EditorPanelHeader.copy(alpha = 0.5f))
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .width(3.dp)
                                    .height(20.dp)
                                    .background(AccentBlue)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = parseInlineMarkdown(block.text),
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    is MarkdownBlock.HorizontalRule -> {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 6.dp)
                                .height(1.dp)
                                .background(EditorBorder)
                        )
                    }
                }
            }
        }
    }
}

private sealed class MarkdownBlock {
    data class Heading(val level: Int, val text: String) : MarkdownBlock()
    data class Paragraph(val text: String) : MarkdownBlock()
    data class ListItem(val text: String, val depth: Int = 0, val isTask: Boolean = false, val isChecked: Boolean = false) : MarkdownBlock()
    data class CodeBlock(val language: String, val code: String) : MarkdownBlock()
    data class BlockQuote(val text: String) : MarkdownBlock()
    object HorizontalRule : MarkdownBlock()
}

private fun parseMarkdownBlocks(rawText: String): List<MarkdownBlock> {
    val result = mutableListOf<MarkdownBlock>()
    val lines = rawText.split("\n")
    var inCodeBlock = false
    var currentCodeLang = ""
    val currentCodeLines = mutableListOf<String>()

    for (line in lines) {
        val trimmed = line.trim()

        if (trimmed.startsWith("```")) {
            if (inCodeBlock) {
                result.add(MarkdownBlock.CodeBlock(currentCodeLang, currentCodeLines.joinToString("\n")))
                currentCodeLines.clear()
                inCodeBlock = false
            } else {
                inCodeBlock = true
                currentCodeLang = trimmed.removePrefix("```").trim()
            }
            continue
        }

        if (inCodeBlock) {
            currentCodeLines.add(line)
            continue
        }

        if (trimmed.isBlank()) continue

        when {
            trimmed.startsWith("# ") -> result.add(MarkdownBlock.Heading(1, trimmed.removePrefix("# ").trim()))
            trimmed.startsWith("## ") -> result.add(MarkdownBlock.Heading(2, trimmed.removePrefix("## ").trim()))
            trimmed.startsWith("### ") -> result.add(MarkdownBlock.Heading(3, trimmed.removePrefix("### ").trim()))
            trimmed.startsWith("#### ") -> result.add(MarkdownBlock.Heading(4, trimmed.removePrefix("#### ").trim()))

            trimmed == "---" || trimmed == "***" || trimmed == "___" -> result.add(MarkdownBlock.HorizontalRule)

            trimmed.startsWith("> ") -> result.add(MarkdownBlock.BlockQuote(trimmed.removePrefix("> ").trim()))

            trimmed.startsWith("- [ ] ") || trimmed.startsWith("* [ ] ") -> {
                val text = trimmed.substring(6).trim()
                result.add(MarkdownBlock.ListItem(text = text, isTask = true, isChecked = false))
            }

            trimmed.startsWith("- [x] ") || trimmed.startsWith("* [x] ") || trimmed.startsWith("- [X] ") -> {
                val text = trimmed.substring(6).trim()
                result.add(MarkdownBlock.ListItem(text = text, isTask = true, isChecked = true))
            }

            trimmed.startsWith("- ") || trimmed.startsWith("* ") || trimmed.startsWith("+ ") -> {
                val text = trimmed.substring(2).trim()
                result.add(MarkdownBlock.ListItem(text = text, isTask = false))
            }

            Regex("""^\d+\.\s+""").containsMatchIn(trimmed) -> {
                val text = trimmed.replace(Regex("""^\d+\.\s+"""), "").trim()
                result.add(MarkdownBlock.ListItem(text = text, isTask = false))
            }

            else -> result.add(MarkdownBlock.Paragraph(line))
        }
    }

    if (inCodeBlock) {
        result.add(MarkdownBlock.CodeBlock(currentCodeLang, currentCodeLines.joinToString("\n")))
    }

    return result
}

fun parseInlineMarkdown(text: String): AnnotatedString {
    return buildAnnotatedString {
        var i = 0
        val len = text.length

        while (i < len) {
            // Bold **text** or __text__
            if (i + 1 < len && (text.substring(i, i + 2) == "**" || text.substring(i, i + 2) == "__")) {
                val end = text.indexOf(text.substring(i, i + 2), i + 2)
                if (end != -1) {
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(text.substring(i + 2, end))
                    }
                    i = end + 2
                    continue
                }
            }

            // Italic *text* or _text_
            if (text[i] == '*' || text[i] == '_') {
                val end = text.indexOf(text[i], i + 1)
                if (end != -1 && end > i + 1) {
                    withStyle(SpanStyle(fontStyle = FontStyle.Italic)) {
                        append(text.substring(i + 1, end))
                    }
                    i = end + 1
                    continue
                }
            }

            // Inline Code `text`
            if (text[i] == '`') {
                val end = text.indexOf('`', i + 1)
                if (end != -1 && end > i + 1) {
                    withStyle(
                        SpanStyle(
                            fontFamily = FontFamily.Monospace,
                            background = EditorPanelHeader,
                            color = AccentBlue,
                            fontSize = 12.sp
                        )
                    ) {
                        append(" ${text.substring(i + 1, end)} ")
                    }
                    i = end + 1
                    continue
                }
            }

            append(text[i])
            i++
        }
    }
}
