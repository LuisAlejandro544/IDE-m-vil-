package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.IdeViewModel
import com.example.ui.components.AiSettingsDialog
import com.example.ui.components.DevStudioIdeScreen
import com.example.ui.components.WorkspaceScreen
import com.example.ui.theme.DevStudioTheme

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

                if (uiState.currentProjectId == null) {
                    // Workspace Selection Screen
                    WorkspaceScreen(
                        projects = uiState.projects,
                        onOpenProject = { projectId -> viewModel.openProject(projectId) },
                        onCreateProject = { name, desc, framework -> viewModel.createProject(name, desc, framework) },
                        onDeleteProject = { projectId -> viewModel.deleteProject(projectId) },
                        onOpenSettings = { viewModel.setShowAiSettingsDialog(true) }
                    )
                } else {
                    // Main IDE Screen for Active Project
                    DevStudioIdeScreen(
                        uiState = uiState,
                        onCloseProject = { viewModel.closeProject() },
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
                        onRunLinter = { viewModel.runLinterAnalysis() },
                        onClearDiagnosticLogs = { viewModel.clearDiagnosticLogs() },
                        onSendDiagnosticsToAi = { viewModel.sendDiagnosticsToAi() },
                        onSendAgentPrompt = { viewModel.sendAgentPrompt(it) },
                        onApplyProposedCode = { viewModel.applyAgentProposedCode(it) },
                        onInsertSymbol = { viewModel.insertSymbolIntoEditor(it) },
                        onSelectProvider = { viewModel.selectAiProvider(it) },
                        onSelectChatMode = { viewModel.selectChatMode(it) },
                        onClearChat = { viewModel.clearChatHistory() },
                        onOpenSettings = { viewModel.setShowAiSettingsDialog(true) },
                        onSaveAiSettings = { provider, openRouterKey, geminiKey -> viewModel.saveAiSettings(provider, openRouterKey, geminiKey) },
                        onDismissAiSettings = { viewModel.setShowAiSettingsDialog(false) }
                    )
                }

                if (uiState.showAiSettingsDialog) {
                    AiSettingsDialog(
                        selectedProvider = uiState.selectedAiProvider,
                        openRouterApiKey = uiState.openRouterApiKey,
                        customGeminiApiKey = uiState.customGeminiApiKey,
                        onDismiss = { viewModel.setShowAiSettingsDialog(false) },
                        onSaveSettings = { provider, openRouterKey, geminiKey -> viewModel.saveAiSettings(provider, openRouterKey, geminiKey) }
                    )
                }
            }
        }
    }
}
