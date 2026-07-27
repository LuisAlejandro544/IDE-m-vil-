package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Code
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Splitscreen
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.api.AiProvider
import com.example.ui.IdeUiState
import com.example.ui.IdeViewMode
import com.example.ui.IdeViewModel
import com.example.ui.components.AiAgentChatSheet
import com.example.ui.components.AiSettingsDialog
import com.example.ui.components.CodeEditorView
import com.example.ui.components.FileManagerDrawer
import com.example.ui.components.LivePreviewView
import com.example.ui.components.NewFileDialog
import com.example.ui.components.QuickSymbolBar
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.DevStudioTheme
import com.example.ui.theme.EditorBackground
import com.example.ui.theme.EditorBorder
import com.example.ui.theme.EditorPanelHeader
import com.example.ui.theme.EditorSurface
import com.example.ui.theme.LineNumberColor
import com.example.ui.theme.SoftGreen
import com.example.ui.theme.SoftRed
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: IdeViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            DevStudioTheme {
                val uiState by viewModel.uiState.collectAsStateWithLifecycle()

                LaunchedEffect(uiState.pendingActionMessage) {
                    uiState.pendingActionMessage?.let { message ->
                        Toast.makeText(this@MainActivity, message, Toast.LENGTH_SHORT).show()
                        viewModel.clearToast()
                    }
                }

                DevStudioIdeScreen(
                    uiState = uiState,
                    onSelectFile = { viewModel.selectFile(it) },
                    onCloseTab = { viewModel.closeTab(it) },
                    onUpdateContent = { viewModel.updateEditorContent(it) },
                    onSaveFile = { viewModel.saveCurrentFile() },
                    onDeleteFile = { viewModel.deleteFile(it) },
                    onSetViewMode = { viewModel.setViewMode(it) },
                    onToggleChat = { viewModel.toggleChat() },
                    onShowNewFileDialog = { viewModel.setShowNewFileDialog(it) },
                    onCreateFile = { name, parentPath -> viewModel.createNewFile(name, parentPath = parentPath) },
                    onCreateFolder = { name, parentPath -> viewModel.createNewFolder(name, parentPath = parentPath) },
                    onSendAgentPrompt = { viewModel.sendAgentPrompt(it) },
                    onApplyProposedCode = { viewModel.applyAgentProposedCode(it) },
                    onInsertSymbol = { viewModel.insertSymbolIntoEditor(it) },
                    onSelectProvider = { viewModel.selectAiProvider(it) },
                    onOpenSettings = { viewModel.setShowAiSettingsDialog(true) },
                    onSaveAiSettings = { provider, openRouterKey, geminiKey -> viewModel.saveAiSettings(provider, openRouterKey, geminiKey) },
                    onDismissAiSettings = { viewModel.setShowAiSettingsDialog(false) }
                )
            }
        }
    }
}

@Composable
fun DevStudioIdeScreen(
    uiState: IdeUiState,
    onSelectFile: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onUpdateContent: (String) -> Unit,
    onSaveFile: () -> Unit,
    onDeleteFile: (com.example.data.db.ProjectFileEntity) -> Unit,
    onSetViewMode: (IdeViewMode) -> Unit,
    onToggleChat: () -> Unit,
    onShowNewFileDialog: (Boolean) -> Unit,
    onCreateFile: (name: String, parentPath: String) -> Unit,
    onCreateFolder: (name: String, parentPath: String) -> Unit,
    onSendAgentPrompt: (String) -> Unit,
    onApplyProposedCode: (com.example.data.db.ChatMessageEntity) -> Unit,
    onInsertSymbol: (String) -> Unit,
    onSelectProvider: (AiProvider) -> Unit,
    onOpenSettings: () -> Unit,
    onSaveAiSettings: (AiProvider, String, String) -> Unit,
    onDismissAiSettings: () -> Unit
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            FileManagerDrawer(
                files = uiState.files,
                activeFilePath = uiState.activeFilePath,
                onFileSelect = { path ->
                    onSelectFile(path)
                    scope.launch { drawerState.close() }
                },
                onFileDelete = onDeleteFile,
                onNewFileClick = {
                    onShowNewFileDialog(true)
                    scope.launch { drawerState.close() }
                }
            )
        }
    ) {
        Scaffold(
            modifier = Modifier
                .fillMaxSize()
                .windowInsetsPadding(WindowInsets.navigationBars),
            topBar = {
                IdeTopAppBar(
                    activeFilePath = uiState.activeFilePath,
                    hasUnsavedChanges = uiState.hasUnsavedChanges,
                    viewMode = uiState.viewMode,
                    onOpenDrawer = { scope.launch { drawerState.open() } },
                    onSaveFile = onSaveFile,
                    onSetViewMode = onSetViewMode,
                    onToggleChat = onToggleChat
                )
            },
            floatingActionButton = {
                if (!uiState.isChatOpen) {
                    FloatingActionButton(
                        onClick = onToggleChat,
                        containerColor = AccentBlue,
                        contentColor = Color.White,
                        shape = CircleShape
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 12.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.AutoAwesome,
                                contentDescription = "Agente IA"
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Agente IA", fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        }
                    }
                }
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .background(EditorBackground)
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Open Tabs Bar
                    if (uiState.openTabs.isNotEmpty() && uiState.viewMode != IdeViewMode.PREVIEW) {
                        OpenTabsRow(
                            openTabs = uiState.openTabs,
                            activeFilePath = uiState.activeFilePath,
                            onSelectFile = onSelectFile,
                            onCloseTab = onCloseTab
                        )
                    }

                    // Main Content depending on View Mode
                    Box(modifier = Modifier.weight(1f)) {
                        when (uiState.viewMode) {
                            IdeViewMode.EDITOR -> {
                                val activeExt = uiState.activeFilePath?.substringAfterLast('.', "txt") ?: "txt"
                                CodeEditorView(
                                    filePath = uiState.activeFilePath,
                                    content = uiState.activeFileContent,
                                    extension = activeExt,
                                    onContentChange = onUpdateContent
                                )
                            }
                            IdeViewMode.PREVIEW -> {
                                LivePreviewView(files = uiState.files)
                            }
                            IdeViewMode.SPLIT -> {
                                Column(modifier = Modifier.fillMaxSize()) {
                                    val activeExt = uiState.activeFilePath?.substringAfterLast('.', "txt") ?: "txt"
                                    CodeEditorView(
                                        filePath = uiState.activeFilePath,
                                        content = uiState.activeFileContent,
                                        extension = activeExt,
                                        onContentChange = onUpdateContent,
                                        modifier = Modifier.weight(1f)
                                    )
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .height(2.dp)
                                            .background(EditorBorder)
                                    )
                                    LivePreviewView(
                                        files = uiState.files,
                                        modifier = Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    // Mobile Quick Symbol Toolbar
                    if (uiState.viewMode != IdeViewMode.PREVIEW) {
                        QuickSymbolBar(onSymbolClick = onInsertSymbol)
                    }
                }

                // AI Agent Chat Drawer / Sheet
                AnimatedVisibility(
                    visible = uiState.isChatOpen,
                    enter = slideInVertically(initialOffsetY = { fullHeight -> fullHeight }),
                    exit = slideOutVertically(targetOffsetY = { fullHeight -> fullHeight }),
                    modifier = Modifier.align(Alignment.BottomCenter)
                ) {
                    AiAgentChatSheet(
                        messages = uiState.chatMessages,
                        isAiLoading = uiState.isAiLoading,
                        selectedProvider = uiState.selectedAiProvider,
                        openRouterApiKey = uiState.openRouterApiKey,
                        onSelectProvider = onSelectProvider,
                        onOpenSettings = onOpenSettings,
                        onSendPrompt = onSendAgentPrompt,
                        onApplyProposedCode = onApplyProposedCode,
                        onCloseChat = onToggleChat
                    )
                }
            }
        }
    }

    if (uiState.showNewFileDialog) {
        NewFileDialog(
            existingFolders = uiState.files,
            onDismiss = { onShowNewFileDialog(false) },
            onCreateFile = onCreateFile,
            onCreateFolder = onCreateFolder
        )
    }

    if (uiState.showAiSettingsDialog) {
        AiSettingsDialog(
            selectedProvider = uiState.selectedAiProvider,
            openRouterApiKey = uiState.openRouterApiKey,
            customGeminiApiKey = uiState.customGeminiApiKey,
            onDismiss = onDismissAiSettings,
            onSaveSettings = onSaveAiSettings
        )
    }
}

@Composable
fun IdeTopAppBar(
    activeFilePath: String?,
    hasUnsavedChanges: Boolean,
    viewMode: IdeViewMode,
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
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onOpenDrawer) {
            Icon(
                imageVector = Icons.Default.Menu,
                contentDescription = "Abrir explorador",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Spacer(modifier = Modifier.width(4.dp))

        // App Title & Active File Badge
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "DevStudio",
                fontWeight = FontWeight.Bold,
                fontSize = 15.sp,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (activeFilePath != null) {
                Spacer(modifier = Modifier.width(8.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(EditorPanelHeader)
                        .padding(horizontal = 8.dp, vertical = 2.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = activeFilePath,
                            fontSize = 11.sp,
                            color = LineNumberColor,
                            fontFamily = FontFamily.Monospace
                        )
                        if (hasUnsavedChanges) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(SoftRed)
                            )
                        }
                    }
                }
            }
        }

        // Save Button
        IconButton(onClick = onSaveFile) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = "Guardar",
                tint = if (hasUnsavedChanges) SoftRed else LineNumberColor
            )
        }

        // View Mode Switcher
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(EditorPanelHeader)
                .padding(2.dp)
        ) {
            IconButton(
                onClick = { onSetViewMode(IdeViewMode.EDITOR) },
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (viewMode == IdeViewMode.EDITOR) AccentBlue else Color.Transparent)
            ) {
                Icon(
                    imageVector = Icons.Default.Code,
                    contentDescription = "Modo Editor",
                    tint = if (viewMode == IdeViewMode.EDITOR) EditorBackground else LineNumberColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = { onSetViewMode(IdeViewMode.PREVIEW) },
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (viewMode == IdeViewMode.PREVIEW) AccentBlue else Color.Transparent)
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Modo Vista Previa",
                    tint = if (viewMode == IdeViewMode.PREVIEW) EditorBackground else LineNumberColor,
                    modifier = Modifier.size(16.dp)
                )
            }

            IconButton(
                onClick = { onSetViewMode(IdeViewMode.SPLIT) },
                modifier = Modifier
                    .size(32.dp)
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (viewMode == IdeViewMode.SPLIT) AccentBlue else Color.Transparent)
            ) {
                Icon(
                    imageVector = Icons.Default.Splitscreen,
                    contentDescription = "Modo Pantalla Dividida",
                    tint = if (viewMode == IdeViewMode.SPLIT) EditorBackground else LineNumberColor,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
fun OpenTabsRow(
    openTabs: List<String>,
    activeFilePath: String?,
    onSelectFile: (String) -> Unit,
    onCloseTab: (String) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(EditorSurface)
            .border(1.dp, EditorBorder)
            .padding(vertical = 4.dp, horizontal = 8.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        items(openTabs) { tabPath ->
            val isSelected = tabPath == activeFilePath
            val fileName = tabPath.substringAfterLast('/')

            Row(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .background(if (isSelected) EditorPanelHeader else Color.Transparent)
                    .border(
                        1.dp,
                        if (isSelected) AccentBlue else EditorBorder,
                        RoundedCornerShape(6.dp)
                    )
                    .clickable { onSelectFile(tabPath) }
                    .padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = fileName,
                    fontSize = 12.sp,
                    fontFamily = FontFamily.Monospace,
                    color = if (isSelected) AccentBlue else LineNumberColor,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                )
                Spacer(modifier = Modifier.width(6.dp))
                IconButton(
                    onClick = { onCloseTab(tabPath) },
                    modifier = Modifier.size(16.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Cerrar pestaña",
                        tint = LineNumberColor,
                        modifier = Modifier.size(12.dp)
                    )
                }
            }
        }
    }
}
