package com.example.ui.components.chat

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountTree
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.BugReport
import androidx.compose.material.icons.filled.Build
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.EditorBackground
import com.example.ui.theme.EditorPanelHeader
import com.example.ui.theme.LineNumberColor
import com.example.ui.theme.SoftGreen
import com.example.ui.theme.SoftRed

data class ToolPillData(
    val toolName: String,
    val summary: String
)

@Composable
fun ToolPillItem(
    pill: ToolPillData,
    modifier: Modifier = Modifier
) {
    var isExpanded by remember { mutableStateOf(false) }

    val (icon, badgeColor) = when (pill.toolName.lowercase()) {
        "get_project_structure" -> Icons.Default.AccountTree to AccentBlue
        "read_file" -> Icons.Default.Description to Color(0xFF8B5CF6)
        "edit_file" -> Icons.Default.Edit to SoftGreen
        "create_file" -> Icons.Default.AddCircleOutline to SoftGreen
        "delete_file" -> Icons.Default.DeleteOutline to SoftRed
        "get_diagnostics" -> Icons.Default.BugReport to Color(0xFFF59E0B)
        else -> Icons.Default.Build to AccentBlue
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(EditorBackground)
            .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(12.dp))
            .clickable { isExpanded = !isExpanded }
            .padding(horizontal = 10.dp, vertical = 7.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .clip(CircleShape)
                    .background(badgeColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = pill.toolName,
                    tint = badgeColor,
                    modifier = Modifier.size(13.dp)
                )
            }

            Text(
                text = pill.toolName,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = badgeColor,
                fontFamily = FontFamily.Monospace
            )

            if (pill.summary.isNotBlank()) {
                Text(
                    text = "•",
                    fontSize = 11.sp,
                    color = LineNumberColor
                )
                val cleanSummary = pill.summary.take(45).replace("\n", " ")
                Text(
                    text = cleanSummary + if (pill.summary.length > 45) "..." else "",
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }

        AnimatedVisibility(
            visible = isExpanded && pill.summary.isNotBlank(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Column {
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = pill.summary,
                    fontSize = 11.sp,
                    color = LineNumberColor,
                    fontFamily = FontFamily.Monospace,
                    lineHeight = 15.sp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(6.dp))
                        .background(EditorPanelHeader)
                        .padding(8.dp)
                )
            }
        }
    }
}
