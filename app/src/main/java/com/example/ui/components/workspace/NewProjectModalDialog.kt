package com.example.ui.components.workspace

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.EditorBorder
import com.example.ui.theme.EditorPanelHeader
import com.example.ui.theme.EditorSurface
import com.example.ui.theme.LineNumberColor

@Composable
fun NewProjectModalDialog(
    onDismiss: () -> Unit,
    onCreate: (name: String, description: String, framework: String) -> Unit
) {
    var projectName by remember { mutableStateOf("") }
    var projectDesc by remember { mutableStateOf("") }
    var selectedFramework by remember { mutableStateOf("HTML5 / JS / CSS") }

    val frameworks = listOf(
        "HTML5 / JS / CSS" to "🌐 Aplicación Web interactiva (HTML, CSS, JavaScript)",
        "Kotlin + Compose" to "📱 App Nativa Android con Jetpack Compose",
        "Rust HTTP Server" to "🦀 Servidor HTTP de alto rendimiento en Rust",
        "C++ JNI Engine" to "⚡ Módulo nativo C++ optimizado vía JNI",
        "Node.js REST API" to "💚 Backend REST API en JavaScript / Node"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Crear Nuevo Proyecto", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSurface)
        },
        text = {
            Column(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = projectName,
                    onValueChange = { projectName = it },
                    label = { Text("Nombre del proyecto") },
                    placeholder = { Text("Ej: Mi App Genial") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                OutlinedTextField(
                    value = projectDesc,
                    onValueChange = { projectDesc = it },
                    label = { Text("Descripción") },
                    placeholder = { Text("Breve explicación del objetivo") },
                    modifier = Modifier.fillMaxWidth(),
                    maxLines = 2
                )

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Seleccionar Framework / Plantilla:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = LineNumberColor
                )

                Spacer(modifier = Modifier.height(6.dp))

                frameworks.forEach { (frameworkName, frameworkDesc) ->
                    val isSelected = selectedFramework == frameworkName
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 3.dp)
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) AccentBlue.copy(alpha = 0.2f) else EditorPanelHeader)
                            .border(1.dp, if (isSelected) AccentBlue else EditorBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedFramework = frameworkName }
                            .padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = frameworkName,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isSelected) AccentBlue else MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = frameworkDesc,
                                fontSize = 11.sp,
                                color = LineNumberColor
                            )
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (projectName.isNotBlank()) {
                        onCreate(projectName, projectDesc.ifBlank { "Proyecto $selectedFramework" }, selectedFramework)
                    }
                },
                enabled = projectName.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = AccentBlue)
            ) {
                Text("Crear Proyecto")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = LineNumberColor)
            }
        },
        containerColor = EditorSurface
    )
}
