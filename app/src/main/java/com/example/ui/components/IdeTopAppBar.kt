package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.state.IdeViewMode
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.EditorBackground
import com.example.ui.theme.EditorPanelHeader
import com.example.ui.theme.EditorSurface
import com.example.ui.theme.LineNumberColor
import com.example.ui.theme.SoftRed

@Composable
fun IdeTopAppBar(
    projectName: String,
    activeFilePath: String?,
    hasUnsavedChanges: Boolean,
    viewMode: IdeViewMode,
    errorCount: Int,
    onBackToWorkspace: () -> Unit,
    onOpenDrawer: () -> Unit,
    onSaveFile: () -> Unit,
    onSetViewMode: (IdeViewMode) -> Unit,
    onToggleChat: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(EditorSurface)
            .statusBarsPadding()
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onBackToWorkspace, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Volver al Espacio de Trabajo",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        IconButton(onClick = onOpenDrawer, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Abrir explorador",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // App & Project Title
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = projectName,
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            if (activeFilePath != null) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = activeFilePath,
                        fontSize = 10.sp,
                        color = LineNumberColor,
                        fontFamily = FontFamily.Monospace,
                        maxLines = 1
                    )
                    if (hasUnsavedChanges) {
                        Spacer(modifier = Modifier.width(4.dp))
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(SoftRed)
                        )
                    }
                }
            }
        }

        // Save Button
        IconButton(onClick = onSaveFile, modifier = Modifier.size(36.dp)) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = "Guardar",
                tint = if (hasUnsavedChanges) SoftRed else LineNumberColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // View Mode Switcher Toolbar
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(EditorPanelHeader)
                .padding(2.dp)
        ) {
            IconButton(
                onClick = { onSetViewMode(IdeViewMode.EDITOR) },
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (viewMode == IdeViewMode.EDITOR) AccentBlue else Color.Transparent)
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = "Editor",
                    tint = if (viewMode == IdeViewMode.EDITOR) EditorBackground else LineNumberColor,
                    modifier = Modifier.size(15.dp)
                )
            }

            IconButton(
                onClick = { onSetViewMode(IdeViewMode.PREVIEW) },
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (viewMode == IdeViewMode.PREVIEW) AccentBlue else Color.Transparent)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Vista Previa",
                    tint = if (viewMode == IdeViewMode.PREVIEW) EditorBackground else LineNumberColor,
                    modifier = Modifier.size(15.dp)
                )
            }

            IconButton(
                onClick = { onSetViewMode(IdeViewMode.SPLIT) },
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (viewMode == IdeViewMode.SPLIT) AccentBlue else Color.Transparent)
            ) {
                Icon(
                    imageVector = Icons.Default.Splitscreen,
                    contentDescription = "Pantalla Dividida",
                    tint = if (viewMode == IdeViewMode.SPLIT) EditorBackground else LineNumberColor,
                    modifier = Modifier.size(15.dp)
                )
            }

            IconButton(
                onClick = { onSetViewMode(IdeViewMode.DIAGNOSTICS) },
                modifier = Modifier
                    .size(30.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (viewMode == IdeViewMode.DIAGNOSTICS) AccentBlue else Color.Transparent)
            ) {
                Box {
                    Icon(
                        imageVector = Icons.Default.BugReport,
                        contentDescription = "Consola Diagnóstico / Linter",
                        tint = if (viewMode == IdeViewMode.DIAGNOSTICS) EditorBackground else if (errorCount > 0) SoftRed else LineNumberColor,
                        modifier = Modifier.size(15.dp)
                    )
                    if (errorCount > 0 && viewMode != IdeViewMode.DIAGNOSTICS) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(SoftRed)
                                .align(Alignment.TopEnd)
                        )
                    }
                }
            }
        }
    }
}
