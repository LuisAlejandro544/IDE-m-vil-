package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.db.ProjectEntity
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.EditorBackground
import com.example.ui.theme.EditorBorder
import com.example.ui.theme.EditorPanelHeader
import com.example.ui.theme.EditorSurface
import com.example.ui.theme.LineNumberColor
import com.example.ui.theme.SoftGreen
import com.example.ui.theme.SoftRed
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WorkspaceScreen(
    projects: List<ProjectEntity>,
    onOpenProject: (Long) -> Unit,
    onCreateProject: (name: String, description: String, framework: String) -> Unit,
    onDeleteProject: (Long) -> Unit,
    onOpenSettings: () -> Unit
) {
    var searchQuery by remember { mutableStateOf("") }
    var selectedFrameworkFilter by remember { mutableStateOf("Todos") }
    var showNewProjectDialog by remember { mutableStateOf(false) }

    val frameworkFilters = listOf("Todos", "HTML5 / JS / CSS", "Kotlin + Compose", "Rust HTTP Server", "C++ JNI", "Node.js REST API")

    val filteredProjects = projects.filter { project ->
        val matchesSearch = project.name.contains(searchQuery, ignoreCase = true) ||
                project.description.contains(searchQuery, ignoreCase = true)
        val matchesFramework = selectedFrameworkFilter == "Todos" || project.framework.contains(selectedFrameworkFilter, ignoreCase = true)
        matchesSearch && matchesFramework
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.navigationBars),
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(EditorSurface)
                    .statusBarsPadding()
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(RoundedCornerShape(10.dp))
                                .background(AccentBlue),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Code,
                                contentDescription = "Logo",
                                tint = Color.White,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = "Espacio de Trabajo",
                                fontSize = 18.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "DevStudio Multi-Framework IDE",
                                fontSize = 12.sp,
                                color = LineNumberColor
                            )
                        }
                    }

                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "Configuración IA",
                            tint = LineNumberColor
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Search Input Field
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Buscar proyecto...", fontSize = 13.sp, color = LineNumberColor) },
                    leadingIcon = {
                        Icon(imageVector = Icons.Default.Search, contentDescription = "Buscar", tint = LineNumberColor)
                    },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = EditorPanelHeader,
                        unfocusedContainerColor = EditorPanelHeader,
                        focusedBorderColor = AccentBlue,
                        unfocusedBorderColor = EditorBorder,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    singleLine = true
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Framework Filter Chips
                LazyRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(frameworkFilters) { filter ->
                        val isSelected = filter == selectedFrameworkFilter
                        FilterChip(
                            selected = isSelected,
                            onClick = { selectedFrameworkFilter = filter },
                            label = { Text(filter, fontSize = 12.sp) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = AccentBlue,
                                selectedLabelColor = Color.White,
                                containerColor = EditorPanelHeader,
                                labelColor = LineNumberColor
                            ),
                            border = FilterChipDefaults.filterChipBorder(
                                enabled = true,
                                selected = isSelected,
                                borderColor = EditorBorder,
                                selectedBorderColor = AccentBlue
                            )
                        )
                    }
                }
            }
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showNewProjectDialog = true },
                containerColor = AccentBlue,
                contentColor = Color.White,
                shape = CircleShape
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "Nuevo Proyecto")
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Nuevo Proyecto", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(EditorBackground)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            if (filteredProjects.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Text("📁", fontSize = 48.sp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "No se encontraron proyectos",
                            fontSize = 16.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = LineNumberColor
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Crea un nuevo proyecto con el botón '+ Nuevo Proyecto'",
                            fontSize = 13.sp,
                            color = LineNumberColor
                        )
                    }
                }
            } else {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(filteredProjects) { project ->
                        ProjectCardItem(
                            project = project,
                            onOpenProject = { onOpenProject(project.id) },
                            onDeleteProject = { onDeleteProject(project.id) }
                        )
                    }
                }
            }
        }
    }

    if (showNewProjectDialog) {
        NewProjectModalDialog(
            onDismiss = { showNewProjectDialog = false },
            onCreate = { name, desc, framework ->
                onCreateProject(name, desc, framework)
                showNewProjectDialog = false
            }
        )
    }
}

@Composable
fun ProjectCardItem(
    project: ProjectEntity,
    onOpenProject: () -> Unit,
    onDeleteProject: () -> Unit
) {
    val dateStr = remember(project.updatedAt) {
        val sdf = SimpleDateFormat("dd MMM, HH:mm", Locale.getDefault())
        sdf.format(Date(project.updatedAt))
    }

    val badgeColor = when {
        project.framework.contains("Compose", true) -> SoftGreen
        project.framework.contains("Rust", true) -> Color(0xFFE5533D)
        project.framework.contains("C++", true) -> Color(0xFF00599C)
        project.framework.contains("Node", true) -> Color(0xFF68A063)
        else -> AccentBlue
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onOpenProject() }
            .border(1.dp, EditorBorder, RoundedCornerShape(14.dp)),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = EditorSurface)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = project.iconEmoji, fontSize = 28.sp)
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Text(
                            text = project.name,
                            fontSize = 16.sp,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Modificado: $dateStr",
                            fontSize = 11.sp,
                            color = LineNumberColor
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(20.dp))
                        .background(badgeColor.copy(alpha = 0.15f))
                        .border(1.dp, badgeColor.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = project.framework,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = badgeColor
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = project.description,
                fontSize = 13.sp,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(14.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDeleteProject,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = "Eliminar Proyecto",
                        tint = SoftRed,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Button(
                    onClick = onOpenProject,
                    colors = ButtonDefaults.buttonColors(containerColor = AccentBlue),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderOpen,
                        contentDescription = "Abrir",
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Abrir Proyecto", fontSize = 13.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

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
