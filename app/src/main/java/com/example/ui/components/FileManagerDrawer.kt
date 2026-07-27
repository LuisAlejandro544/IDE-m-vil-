package com.example.ui.components

import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ProjectFileEntity
import com.example.ui.components.filemanager.FileDrawerFooter
import com.example.ui.components.filemanager.FileTreeItemRow
import com.example.ui.components.filemanager.buildFlatTree
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.EditorBorder
import com.example.ui.theme.EditorSurface

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
                val isExpanded = expandedFolders.contains(file.path)

                FileTreeItemRow(
                    item = item,
                    activeFilePath = activeFilePath,
                    isExpanded = isExpanded,
                    canDelete = files.size > 1,
                    onItemClick = {
                        if (file.isDirectory) {
                            expandedFolders = if (isExpanded) {
                                expandedFolders - file.path
                            } else {
                                expandedFolders + file.path
                            }
                        } else {
                            onFileSelect(file.path)
                        }
                    },
                    onDeleteClick = { onFileDelete(file) }
                )
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

        FileDrawerFooter()
    }
}
