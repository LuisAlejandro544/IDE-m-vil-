package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.api.AiProvider
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.EditorBackground
import com.example.ui.theme.EditorBorder
import com.example.ui.theme.EditorPanelHeader
import com.example.ui.theme.EditorSurface
import com.example.ui.theme.LineNumberColor
import com.example.ui.theme.SoftGreen

@Composable
fun AiSettingsDialog(
    selectedProvider: AiProvider,
    openRouterApiKey: String,
    customGeminiApiKey: String,
    onDismiss: () -> Unit,
    onSaveSettings: (provider: AiProvider, openRouterKey: String, geminiKey: String) -> Unit
) {
    var provider by remember { mutableStateOf(selectedProvider) }
    var openRouterKey by remember { mutableStateOf(openRouterApiKey) }
    var geminiKey by remember { mutableStateOf(customGeminiApiKey) }
    var showOpenRouterKey by remember { mutableStateOf(false) }
    var showGeminiKey by remember { mutableStateOf(false) }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = EditorSurface),
            border = androidx.compose.foundation.BorderStroke(1.dp, EditorBorder)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(20.dp)
            ) {
                // Header
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.Key,
                        contentDescription = null,
                        tint = AccentBlue
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Configuración de IA",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Provider Selection
                Text(
                    text = "PROVEEDOR DE IA",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LineNumberColor,
                    letterSpacing = 0.5.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Gemini Radio Choice
                ProviderOptionRow(
                    title = "Google Gemini",
                    subtitle = "Modelo: gemini-3.5-flash",
                    isSelected = provider == AiProvider.GEMINI,
                    onClick = { provider = AiProvider.GEMINI }
                )

                Spacer(modifier = Modifier.height(8.dp))

                // OpenRouter Radio Choice
                ProviderOptionRow(
                    title = "OpenRouter AI",
                    subtitle = "Modelo: inclusionai/ling-3.0-flash:free",
                    isSelected = provider == AiProvider.OPENROUTER,
                    onClick = { provider = AiProvider.OPENROUTER }
                )

                Spacer(modifier = Modifier.height(16.dp))

                // OpenRouter API Key Input
                Text(
                    text = "API KEY DE OPENROUTER",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LineNumberColor,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = openRouterKey,
                    onValueChange = { openRouterKey = it },
                    placeholder = { Text("sk-or-v1-...", fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
                    singleLine = true,
                    visualTransformation = if (showOpenRouterKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showOpenRouterKey = !showOpenRouterKey }) {
                            Icon(
                                imageVector = if (showOpenRouterKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Mostrar/Ocultar clave",
                                tint = LineNumberColor
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = EditorBorder,
                        focusedContainerColor = EditorBackground,
                        unfocusedContainerColor = EditorBackground
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Gemini API Key Input (Optional)
                Text(
                    text = "API KEY DE GEMINI (OPCIONAL)",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = LineNumberColor,
                    letterSpacing = 0.5.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                OutlinedTextField(
                    value = geminiKey,
                    onValueChange = { geminiKey = it },
                    placeholder = { Text("AIzaSy... (o usa .env por defecto)", fontSize = 12.sp, fontFamily = FontFamily.Monospace) },
                    singleLine = true,
                    visualTransformation = if (showGeminiKey) VisualTransformation.None else PasswordVisualTransformation(),
                    trailingIcon = {
                        IconButton(onClick = { showGeminiKey = !showGeminiKey }) {
                            Icon(
                                imageVector = if (showGeminiKey) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                                contentDescription = "Mostrar/Ocultar clave",
                                tint = LineNumberColor
                            )
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = EditorBorder,
                        focusedContainerColor = EditorBackground,
                        unfocusedContainerColor = EditorBackground
                    )
                )

                Spacer(modifier = Modifier.height(20.dp))

                // Actions
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(onClick = onDismiss) {
                        Text("Cancelar", color = LineNumberColor)
                    }

                    Spacer(modifier = Modifier.weight(1f))

                    Button(
                        onClick = {
                            onSaveSettings(provider, openRouterKey.trim(), geminiKey.trim())
                            onDismiss()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                        shape = RoundedCornerShape(8.dp)
                    ) {
                        Icon(imageVector = Icons.Default.AutoAwesome, contentDescription = null)
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Guardar Cambios", fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun ProviderOptionRow(
    title: String,
    subtitle: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isSelected) EditorPanelHeader else EditorBackground)
            .border(
                1.dp,
                if (isSelected) AccentBlue else EditorBorder,
                RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (isSelected) AccentBlue else LineNumberColor
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtitle,
                fontSize = 11.sp,
                color = if (isSelected) SoftGreen else LineNumberColor,
                fontFamily = FontFamily.Monospace
            )
        }
    }
}
