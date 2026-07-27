package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.api.AiProvider
import com.example.data.api.ChatMode
import com.example.data.db.ChatMessageEntity
import com.example.data.db.ProjectFileEntity
import com.example.ui.state.IdeUiState
import com.example.ui.state.IdeViewMode
import com.example.ui.theme.AccentBlue
import com.example.ui.theme.EditorBackground
import com.example.ui.theme.EditorBorder
import kotlinx.coroutines.launch

@Composable
fun DevStudioIdeScreen(
    uiState: IdeUiState,
    onCloseProject: () -> Unit,
    onSelectFile: (String) -> Unit,
    onCloseTab: (String) -> Unit,
    onUpdateContent: (String) -> Unit,
    onSaveFile: () -> Unit,
    onDeleteFile: (ProjectFileEntity) -> Unit,
    onSetViewMode: (IdeViewMode) -> Unit,
    onToggleChat: () -> Unit,
    onShowNewFileDialog: (Boolean) -> Unit,
    onCreateFile: (name: String, parentPath: String) -> Unit,
    onCreateFolder: (name: String, parentPath: String) -> Unit,
    onRunLinter: () -> Unit,
    onClearDiagnosticLogs: () -> Unit,
    onSendDiagnosticsToAi: () -> Unit,
    onSendAgentPrompt: (String) -> Unit,
    onApplyProposedCode: (ChatMessageEntity) -> Unit,
    onInsertSymbol: (String) -> Unit,
    onSelectProvider: (AiProvider) -> Unit,
    onSelectChatMode: (ChatMode) -> Unit = {},
    onClearChat: () -> Unit = {},
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
                    projectName = uiState.currentProject?.name ?: "Proyecto",
                    activeFilePath = uiState.activeFilePath,
                    hasUnsavedChanges = uiState.hasUnsavedChanges,
                    viewMode = uiState.viewMode,
                    errorCount = uiState.diagnosticLogs.count { it.level == "ERROR" },
                    onBackToWorkspace = onCloseProject,
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
                    if (uiState.openTabs.isNotEmpty() && uiState.viewMode != IdeViewMode.PREVIEW && uiState.viewMode != IdeViewMode.DIAGNOSTICS) {
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
                            IdeViewMode.DIAGNOSTICS -> {
                                DiagnosticConsoleView(
                                    logs = uiState.diagnosticLogs,
                                    onRunLinter = onRunLinter,
                                    onClearLogs = onClearDiagnosticLogs,
                                    onSendDiagnosticsToAi = onSendDiagnosticsToAi
                                )
                            }
                        }
                    }

                    // Mobile Quick Symbol Toolbar
                    if (uiState.viewMode == IdeViewMode.EDITOR || uiState.viewMode == IdeViewMode.SPLIT) {
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
                        selectedChatMode = uiState.selectedChatMode,
                        openRouterApiKey = uiState.openRouterApiKey,
                        onSelectProvider = onSelectProvider,
                        onSelectChatMode = onSelectChatMode,
                        onOpenSettings = onOpenSettings,
                        onSendPrompt = onSendAgentPrompt,
                        onApplyProposedCode = onApplyProposedCode,
                        onClearChat = onClearChat,
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
}
