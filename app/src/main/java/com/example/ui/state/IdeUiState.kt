package com.example.ui.state

import com.example.data.api.AiProvider
import com.example.data.api.ChatMode
import com.example.data.db.ChatMessageEntity
import com.example.data.db.DiagnosticLogEntity
import com.example.data.db.ProjectEntity
import com.example.data.db.ProjectFileEntity

enum class IdeViewMode {
    EDITOR,
    PREVIEW,
    SPLIT,
    DIAGNOSTICS
}

internal data class ProjectState(
    val currentProjectId: Long?,
    val currentProject: ProjectEntity?,
    val projects: List<ProjectEntity>
)

internal data class EditorState(
    val files: List<ProjectFileEntity>,
    val openTabs: List<String>,
    val activeFilePath: String?,
    val editorContent: String,
    val savedContent: String
)

internal data class UiControlState(
    val viewMode: IdeViewMode,
    val isChatOpen: Boolean,
    val isAiLoading: Boolean,
    val showAiSettingsDialog: Boolean,
    val showNewFileDialog: Boolean,
    val toastMessage: String?
)

internal data class AiAndDiagnosticsState(
    val chatMessages: List<ChatMessageEntity>,
    val diagnosticLogs: List<DiagnosticLogEntity>,
    val selectedAiProvider: AiProvider,
    val selectedChatMode: ChatMode,
    val openRouterApiKey: String,
    val customGeminiApiKey: String
)

data class IdeUiState(
    val currentProjectId: Long? = null,
    val currentProject: ProjectEntity? = null,
    val projects: List<ProjectEntity> = emptyList(),
    val files: List<ProjectFileEntity> = emptyList(),
    val openTabs: List<String> = emptyList(),
    val activeFilePath: String? = null,
    val activeFileContent: String = "",
    val hasUnsavedChanges: Boolean = false,
    val viewMode: IdeViewMode = IdeViewMode.EDITOR,
    val isChatOpen: Boolean = false,
    val chatMessages: List<ChatMessageEntity> = emptyList(),
    val diagnosticLogs: List<DiagnosticLogEntity> = emptyList(),
    val isAiLoading: Boolean = false,
    val selectedAiProvider: AiProvider = AiProvider.GEMINI,
    val selectedChatMode: ChatMode = ChatMode.STEP_BY_STEP,
    val openRouterApiKey: String = "",
    val customGeminiApiKey: String = "",
    val showAiSettingsDialog: Boolean = false,
    val showNewFileDialog: Boolean = false,
    val pendingActionMessage: String? = null
)
