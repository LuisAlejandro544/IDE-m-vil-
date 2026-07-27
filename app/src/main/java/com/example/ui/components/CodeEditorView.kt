package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.components.markdown.MarkdownRenderer
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.EditorBackground
import com.example.ui.theme.EditorBorder
import com.example.ui.theme.EditorPanelHeader
import com.example.ui.theme.LineNumberColor
import com.example.ui.theme.SoftGreen

@Composable
fun CodeEditorView(
    filePath: String?,
    content: String,
    extension: String,
    onContentChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (filePath == null) {
        Box(
            modifier = modifier
                .fillMaxSize()
                .background(EditorBackground),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "Selecciona o crea un archivo en el gestor para comenzar a programar.",
                color = LineNumberColor,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(24.dp)
            )
        }
        return
    }

    var textFieldValue by remember(filePath) {
        mutableStateOf(TextFieldValue(text = content))
    }

    // Keep internal state synced if content changes externally (e.g. from AI Agent)
    LaunchedEffect(content) {
        if (textFieldValue.text != content) {
            textFieldValue = textFieldValue.copy(text = content)
        }
    }

    var isRenderedMode by remember(filePath) {
        mutableStateOf(extension.equals("md", ignoreCase = true) || extension.equals("markdown", ignoreCase = true))
    }
    val isMarkdownFile = extension.equals("md", ignoreCase = true) || extension.equals("markdown", ignoreCase = true)

    val verticalScrollState = rememberScrollState()
    val horizontalScrollState = rememberScrollState()

    val lines = textFieldValue.text.split("\n")
    val lineCount = maxOf(lines.size, 1)

    val syntaxTransformation = remember(extension) {
        VisualTransformation { text ->
            val highlighted = SyntaxHighlighter.highlightCode(text.text, extension)
            TransformedText(highlighted, OffsetMapping.Identity)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(EditorBackground)
    ) {
        // Markdown Toggle Header Bar (if file is Markdown or user wants to render)
        if (isMarkdownFile) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EditorPanelHeader)
                    .padding(horizontal = 12.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "📄 Documento Markdown (.md)",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = AccentBlue
                )

                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    FilterChip(
                        selected = !isRenderedMode,
                        onClick = { isRenderedMode = false },
                        label = { Text("✏️ Código Fuente", fontSize = 11.sp) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = AccentBlue,
                            selectedLabelColor = Color.White,
                            containerColor = EditorBackground,
                            labelColor = LineNumberColor
                        )
                    )

                    FilterChip(
                        selected = isRenderedMode,
                        onClick = { isRenderedMode = true },
                        label = { Text("👁️ Renderizar .md", fontSize = 11.sp, fontWeight = FontWeight.Bold) },
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = SoftGreen,
                            selectedLabelColor = Color.White,
                            containerColor = EditorBackground,
                            labelColor = LineNumberColor
                        )
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(EditorBorder)
            )
        }

        // Main Editor Workspace (Rendered Markdown OR Code Editor)
        if (isMarkdownFile && isRenderedMode) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .verticalScroll(verticalScrollState)
                    .padding(16.dp)
            ) {
                com.example.ui.components.markdown.MarkdownRenderer(
                    markdownText = textFieldValue.text,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        } else {
            Row(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                // Line Numbers Column
                Column(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(48.dp)
                        .background(EditorPanelHeader)
                        .verticalScroll(verticalScrollState)
                        .padding(vertical = 12.dp, horizontal = 4.dp),
                    horizontalAlignment = Alignment.End
                ) {
                    for (i in 1..lineCount) {
                        Text(
                            text = "$i",
                            color = LineNumberColor,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 20.sp,
                            textAlign = TextAlign.End,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                // Vertical Border Divider
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(1.dp)
                        .background(EditorBorder)
                )

                // Text Input Code Area
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight()
                        .verticalScroll(verticalScrollState)
                        .horizontalScroll(horizontalScrollState)
                        .padding(horizontal = 12.dp, vertical = 12.dp)
                ) {
                    BasicTextField(
                        value = textFieldValue,
                        onValueChange = { newValue ->
                            textFieldValue = newValue
                            onContentChange(newValue.text)
                        },
                        textStyle = TextStyle(
                            color = MaterialTheme.colorScheme.onSurface,
                            fontSize = 13.sp,
                            fontFamily = FontFamily.Monospace,
                            lineHeight = 20.sp
                        ),
                        cursorBrush = SolidColor(MaterialTheme.colorScheme.primary),
                        visualTransformation = syntaxTransformation,
                        keyboardOptions = KeyboardOptions.Default,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }

        // Bottom Editor Info Status Bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(EditorPanelHeader)
                .padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚡ Editor Nativo DevStudio",
                color = LineNumberColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Líneas: $lineCount",
                color = LineNumberColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Caracteres: ${textFieldValue.text.length}",
                color = LineNumberColor,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = extension.uppercase(),
                color = MaterialTheme.colorScheme.primary,
                fontSize = 11.sp,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
