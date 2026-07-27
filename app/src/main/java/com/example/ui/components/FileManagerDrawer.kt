package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowRight
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ProjectFileEntity
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.EditorBorder
import com.example.ui.theme.EditorPanelHeader
import com.example.ui.theme.EditorSurface
import com.example.ui.theme.LineNumberColor
import com.example.ui.theme.SoftGreen
import com.example.ui.theme.SoftRed

data class DisplayFileItem(
    val entity: ProjectFileEntity,
    val depth: Int
)

fun buildFlatTree(
    allFiles: List<ProjectFileEntity>,
    expandedFolders: Set<String>,
    parentPath: String = "/",
    depth: Int = 0
): List<DisplayFileItem> {
    val result = mutableListOf<DisplayFileItem>()
    val cleanParent = if (parentPath.endsWith("/") && parentPath != "/") parentPath.dropLast(1) else parentPath

    val children = allFiles.filter { file ->
        val p = if (file.parentPath.endsWith("/") && file.parentPath != "/") file.parentPath.dropLast(1) else file.parentPath
        p == cleanParent
    }.sortedWith(compareByDescending<ProjectFileEntity> { it.isDirectory }.thenBy { it.name.lowercase() })

    for (child in children) {
        result.add(DisplayFileItem(child, depth))
        if (child.isDirectory && expandedFolders.contains(child.path)) {
            result.addAll(buildFlatTree(allFiles, expandedFolders, child.path, depth + 1))
        }
    }

    // Safety fallback: if there are files whose parentPath doesn't match any directory, show them at depth 0
    if (depth == 0) {
        val listedPaths = result.map { it.entity.path }.toSet()
        val orphans = allFiles.filter { !listedPaths.contains(it.path) }
        for (orphan in orphans) {
            result.add(DisplayFileItem(orphan, 0))
        }
    }

    return result
}

@Composable
fun FileManagerDrawer(
    files: List<ProjectFileEntity>,
    activeFilePath: String?,
    onFileSelect: (String) -> Unit,
    onFileDelete: (ProjectFileEntity) -> Unit,
    onNewFileClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Keep set of expanded folder paths (default expand root folders like /Docs)
    var expandedFolders by remember {
        mutableStateOf(setOf("/", "/Docs", "/src"))
    }

    val displayList = remember(files, expandedFolders) {
        buildFlatTree(files, expandedFolders)
    }

    Column(
        modifier = modifier
            .fillMaxHeight()
            .width(280.dp)
            .background(EditorSurface)
            .statusBarsPadding()
            .navigationBarsPadding()
            .padding(16.dp)
    ) {
        // Drawer Header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.Folder,
                    contentDescription = "Explorador de archivos",
                    tint = AccentBlue,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Explorador IDE",
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // New Item Button (creates files or folders)
        Button(
            onClick = onNewFileClick,
            modifier = Modifier.fillMaxWidth(),
            colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
            shape = RoundedCornerShape(8.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Add,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(text = "Nuevo Archivo / Carpeta", fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Hierarchical File Tree List
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            items(displayList, key = { it.entity.id }) { item ->
                val file = item.entity
                val isSelected = file.path == activeFilePath && !file.isDirectory
                val isExpanded = expandedFolders.contains(file.path)
                val indentation = (item.depth * 14).dp

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = indentation)
                        .clip(RoundedCornerShape(6.dp))
                        .background(if (isSelected) EditorPanelHeader else Color.Transparent)
                        .clickable {
                            if (file.isDirectory) {
                                expandedFolders = if (isExpanded) {
                                    expandedFolders - file.path
                                } else {
                                    expandedFolders + file.path
                                }
                            } else {
                                onFileSelect(file.path)
                            }
                        }
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
                    if (files.size > 1) {
                        IconButton(
                            onClick = { onFileDelete(file) },
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
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp)
                .background(EditorBorder)
                .height(1.dp)
        )

        Spacer(modifier = Modifier.height(8.dp))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val cppStatus = remember { com.example.native.CppEngine.getEngineStatus() }
            val isRustRunning = com.example.native.RustHttpServer.isServerRunning()

            Text(
                text = "⚡ $cppStatus",
                color = LineNumberColor,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = "🦀 Rust Server: ${if (isRustRunning) "http://127.0.0.1:8080" else "En Espera"}",
                color = if (isRustRunning) SoftGreen else LineNumberColor,
                fontSize = 10.sp
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "DevStudio v1.0 • Entorno Polyglot Kotlin/C++/Rust",
                color = LineNumberColor,
                fontSize = 10.sp
            )
        }
    }
}
