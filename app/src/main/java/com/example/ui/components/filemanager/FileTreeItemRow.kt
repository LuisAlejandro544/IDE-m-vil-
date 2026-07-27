package com.example.ui.components.filemanager

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Css
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Html
import androidx.compose.material.icons.filled.Javascript
import androidx.compose.material.icons.filled.Terminal
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ProjectFileEntity
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.EditorPanelHeader
import com.example.ui.theme.LineNumberColor
import com.example.ui.theme.SoftRed

@Composable
fun FileTreeItemRow(
    item: DisplayFileItem,
    activeFilePath: String?,
    isExpanded: Boolean,
    canDelete: Boolean,
    onItemClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val file = item.entity
    val isSelected = file.path == activeFilePath && !file.isDirectory
    val indentation = (item.depth * 14).dp

    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(start = indentation)
            .clip(RoundedCornerShape(6.dp))
            .background(if (isSelected) EditorPanelHeader else Color.Transparent)
            .clickable { onItemClick() }
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (file.isDirectory) {
            Icon(
                imageVector = if (isExpanded) Icons.Default.ArrowDropDown else Icons.AutoMirrored.Filled.ArrowRight,
                contentDescription = if (isExpanded) "Contraer" else "Expandir",
                tint = LineNumberColor,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Icon(
                imageVector = if (isExpanded) Icons.Default.FolderOpen else Icons.Default.Folder,
                contentDescription = "Carpeta",
                tint = Color(0xFFEAB308), // Warm Amber Folder Icon
                modifier = Modifier.size(18.dp)
            )
        } else {
            Spacer(modifier = Modifier.width(22.dp))
            val (icon, iconTint) = when (file.extension.lowercase()) {
                "html", "htm" -> Icons.Default.Html to Color(0xFFE34F26)
                "css" -> Icons.Default.Css to Color(0xFF1572B6)
                "js" -> Icons.Default.Javascript to Color(0xFFF7DF1E)
                "md" -> Icons.Default.Description to Color(0xFF38BDF8)
                "json" -> Icons.Default.Code to Color(0xFFF97316)
                "kt", "java" -> Icons.Default.Code to Color(0xFFA855F7)
                "cpp", "c", "h" -> Icons.Default.Terminal to Color(0xFF06B6D4)
                "rs" -> Icons.Default.Terminal to Color(0xFFF43F5E)
                else -> Icons.Default.Code to LineNumberColor
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = file.name,
            color = if (isSelected) AccentBlue else if (file.isDirectory) Color(0xFFE2E8F0) else MaterialTheme.colorScheme.onSurface,
            fontWeight = if (isSelected || file.isDirectory) FontWeight.Bold else FontWeight.Normal,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f)
        )

        // Show delete button
        if (canDelete) {
            IconButton(
                onClick = onDeleteClick,
                modifier = Modifier.size(22.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Eliminar",
                    tint = SoftRed,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}
