package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.AiAgentService
import com.example.data.api.AiProvider
import com.example.data.api.ChatMode
import com.example.data.db.AppDatabase
import com.example.data.db.ChatMessageEntity
import com.example.data.db.ProjectEntity
import com.example.data.db.ProjectFileEntity
import com.example.data.repository.IdeRepository
import com.example.ui.delegate.AgentToolExecutor
import com.example.ui.handler.AgentPromptRunner
import com.example.ui.handler.AiPreferencesManager
import com.example.ui.state.AiAndDiagnosticsState
import com.example.ui.state.EditorState
import com.example.ui.state.IdeUiState
import com.example.ui.state.IdeViewMode
import com.example.ui.state.ProjectState
import com.example.ui.state.UiControlState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(ExperimentalCoroutinesApi::class)
class IdeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = IdeRepository(
        projectDao = db.projectDao(),
        projectFileDao = db.projectFileDao(),
        chatMessageDao = db.chatMessageDao(),
        diagnosticLogDao = db.diagnosticLogDao()
    )
    private val aiAgentService = AiAgentService()
    private val toolExecutor = AgentToolExecutor(repository)
    private val agentPromptRunner = AgentPromptRunner(repository, aiAgentService, toolExecutor)
    private val prefsManager = AiPreferencesManager(application)

    private val _currentProjectId = MutableStateFlow<Long?>(null)
    private val _currentProject = MutableStateFlow<ProjectEntity?>(null)

    private val _openTabs = MutableStateFlow<List<String>>(emptyList())
    private val _activeFilePath = MutableStateFlow<String?>(null)
    private val _editorContent = MutableStateFlow<String>("")
    private val _savedContent = MutableStateFlow<String>("")
    private val _viewMode = MutableStateFlow(IdeViewMode.EDITOR)
    private val _isChatOpen = MutableStateFlow(false)
    private val _isAiLoading = MutableStateFlow(false)

    private val _selectedAiProvider = MutableStateFlow(AiProvider.GEMINI)
    private val _selectedChatMode = MutableStateFlow(ChatMode.STEP_BY_STEP)
    private val _openRouterApiKey = MutableStateFlow("")
    private val _customGeminiApiKey = MutableStateFlow("")
    private val _showAiSettingsDialog = MutableStateFlow(false)

    private val _showNewFileDialog = MutableStateFlow(false)
    private val _toastMessage = MutableStateFlow<String?>(null)

    // Dynamic flows based on current selected project
    private val activeFilesFlow = _currentProjectId.flatMapLatest { projectId ->
        if (projectId == null) flowOf(emptyList())
        else repository.getFilesForProject(projectId)
    }

    private val activeMessagesFlow = _currentProjectId.flatMapLatest { projectId ->
        if (projectId == null) flowOf(emptyList())
        else repository.getChatMessagesForProject(projectId)
    }

    private val activeLogsFlow = _currentProjectId.flatMapLatest { projectId ->
        if (projectId == null) flowOf(emptyList())
        else repository.getDiagnosticLogsForProject(projectId)
    }

    private val projectStateFlow = combine(
        _currentProjectId,
        _currentProject,
        repository.allProjects
    ) { projectId, currentProj, projectsList ->
        ProjectState(projectId, currentProj, projectsList)
    }

    private val editorStateFlow = combine(
        activeFilesFlow,
        _openTabs,
        _activeFilePath,
        _editorContent,
        _savedContent
    ) { filesList, tabs, activePath, editorText, savedText ->
        EditorState(filesList, tabs, activePath, editorText, savedText)
    }

    private val uiControlStateFlow = combine(
        combine(_viewMode, _isChatOpen, _isAiLoading) { vm, isChat, isLoading ->
            Triple(vm, isChat, isLoading)
        },
        combine(_showAiSettingsDialog, _showNewFileDialog, _toastMessage) { showSettings, showNewFile, toast ->
            Triple(showSettings, showNewFile, toast)
        }
    ) { (vm, isChat, isLoading), (showSettings, showNewFile, toast) ->
        UiControlState(vm, isChat, isLoading, showSettings, showNewFile, toast)
    }

    private val aiAndDiagnosticsStateFlow = combine(
        activeMessagesFlow,
        activeLogsFlow,
        _selectedAiProvider,
        _selectedChatMode,
        combine(_openRouterApiKey, _customGeminiApiKey) { openKey, geminiKey -> openKey to geminiKey }
    ) { msgs, logs, provider, mode, (openRouterKey, geminiKey) ->
        AiAndDiagnosticsState(msgs, logs, provider, mode, openRouterKey, geminiKey)
    }

    val uiState: StateFlow<IdeUiState> = combine(
        projectStateFlow,
        editorStateFlow,
        uiControlStateFlow,
        aiAndDiagnosticsStateFlow
    ) { proj, ed, ctrl, ai ->
        IdeUiState(
            currentProjectId = proj.currentProjectId,
            currentProject = proj.currentProject,
            projects = proj.projects,
            files = ed.files,
            openTabs = ed.openTabs,
            activeFilePath = ed.activeFilePath,
            activeFileContent = ed.editorContent,
            hasUnsavedChanges = ed.editorContent != ed.savedContent,
            viewMode = ctrl.viewMode,
            isChatOpen = ctrl.isChatOpen,
            chatMessages = ai.chatMessages,
            diagnosticLogs = ai.diagnosticLogs,
            isAiLoading = ctrl.isAiLoading,
            selectedAiProvider = ai.selectedAiProvider,
            selectedChatMode = ai.selectedChatMode,
            openRouterApiKey = ai.openRouterApiKey,
            customGeminiApiKey = ai.customGeminiApiKey,
            showAiSettingsDialog = ctrl.showAiSettingsDialog,
            showNewFileDialog = ctrl.showNewFileDialog,
            pendingActionMessage = ctrl.toastMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = IdeUiState()
    )

    init {
        _selectedAiProvider.value = prefsManager.loadSelectedProvider()
        _selectedChatMode.value = prefsManager.loadSelectedChatMode()
        _openRouterApiKey.value = prefsManager.loadOpenRouterApiKey()
        _customGeminiApiKey.value = prefsManager.loadCustomGeminiApiKey()

        viewModelScope.launch {
            repository.ensureDefaultProjectsExist()
        }
    }

    fun openProject(projectId: Long) {
        viewModelScope.launch {
            val proj = repository.getProjectById(projectId)
            if (proj != null) {
                _currentProjectId.value = projectId
                _currentProject.value = proj

                _openTabs.value = listOf("/index.html", "/src/main/java/MainActivity.kt", "/src/main.rs", "/server.js", "/cpp/engine.cpp")

                val primaryFile = repository.getFileByPath(projectId, "/index.html")
                    ?: repository.getFileByPath(projectId, "/src/main/java/MainActivity.kt")
                    ?: repository.getFileByPath(projectId, "/src/main.rs")
                    ?: repository.getFileByPath(projectId, "/server.js")

                if (primaryFile != null) {
                    selectFile(primaryFile.path)
                } else {
                    _activeFilePath.value = null
                    _editorContent.value = ""
                    _savedContent.value = ""
                }

                repository.runLinterAnalysis(projectId)
            }
        }
    }

    fun closeProject() {
        _currentProjectId.value = null
        _currentProject.value = null
        _activeFilePath.value = null
        _editorContent.value = ""
        _savedContent.value = ""
        _openTabs.value = emptyList()
    }

    fun createProject(name: String, description: String, framework: String) {
        viewModelScope.launch {
            val newId = repository.createProject(name, description, framework)
            _toastMessage.value = "✨ Proyecto '$name' creado con éxito."
            openProject(newId)
        }
    }

    fun deleteProject(projectId: Long) {
        viewModelScope.launch {
            repository.deleteProject(projectId)
            if (_currentProjectId.value == projectId) {
                closeProject()
            }
            _toastMessage.value = "Proyecto eliminado."
        }
    }

    fun selectAiProvider(provider: AiProvider) {
        _selectedAiProvider.value = provider
        saveAiPreferences()
    }

    fun selectChatMode(mode: ChatMode) {
        _selectedChatMode.value = mode
        saveAiPreferences()
    }

    fun saveAiSettings(provider: AiProvider, openRouterKey: String, geminiKey: String) {
        _selectedAiProvider.value = provider
        _openRouterApiKey.value = openRouterKey
        _customGeminiApiKey.value = geminiKey
        saveAiPreferences()
        _toastMessage.value = "Configuración de IA guardada (${provider.displayName})"
    }

    fun setShowAiSettingsDialog(show: Boolean) {
        _showAiSettingsDialog.value = show
    }

    private fun saveAiPreferences() {
        prefsManager.savePreferences(
            provider = _selectedAiProvider.value,
            chatMode = _selectedChatMode.value,
            openRouterApiKey = _openRouterApiKey.value,
            geminiApiKey = _customGeminiApiKey.value
        )
    }

    fun selectFile(path: String) {
        val projectId = _currentProjectId.value ?: return
        viewModelScope.launch {
            val file = repository.getFileByPath(projectId, path)
            if (file != null) {
                if (!_openTabs.value.contains(path)) {
                    _openTabs.value = _openTabs.value + path
                }
                _activeFilePath.value = path
                _editorContent.value = file.content
                _savedContent.value = file.content
            }
        }
    }

    fun updateEditorContent(newText: String) {
        _editorContent.value = newText
    }

    fun saveCurrentFile() {
        val projectId = _currentProjectId.value ?: return
        val currentPath = _activeFilePath.value ?: return
        val currentText = _editorContent.value
        viewModelScope.launch {
            repository.updateFileContent(projectId, currentPath, currentText)
            _savedContent.value = currentText
            _toastMessage.value = "Archivo guardado: $currentPath"
        }
    }

    fun closeTab(path: String) {
        val currentTabs = _openTabs.value.toMutableList()
        currentTabs.remove(path)
        _openTabs.value = currentTabs

        if (_activeFilePath.value == path) {
            val nextPath = currentTabs.lastOrNull()
            if (nextPath != null) {
                selectFile(nextPath)
            } else {
                _activeFilePath.value = null
                _editorContent.value = ""
                _savedContent.value = ""
            }
        }
    }

    fun createNewFile(fileName: String, initialContent: String = "", parentPath: String = "/") {
        val projectId = _currentProjectId.value ?: return
        if (fileName.isBlank()) return
        val cleanName = fileName.trim().removePrefix("/")
        val cleanParent = if (parentPath.endsWith("/") && parentPath != "/") parentPath.dropLast(1) else parentPath
        val fullPath = if (cleanParent == "/") "/$cleanName" else "$cleanParent/$cleanName"
        viewModelScope.launch {
            repository.createFile(projectId, cleanName, fullPath, initialContent, cleanParent)
            _showNewFileDialog.value = false
            selectFile(fullPath)
            _toastMessage.value = "Archivo creado: $fullPath"
        }
    }

    fun createNewFolder(folderName: String, parentPath: String = "/") {
        val projectId = _currentProjectId.value ?: return
        if (folderName.isBlank()) return
        val cleanName = folderName.trim().removePrefix("/")
        val cleanParent = if (parentPath.endsWith("/") && parentPath != "/") parentPath.dropLast(1) else parentPath
        val fullPath = if (cleanParent == "/") "/$cleanName" else "$cleanParent/$cleanName"
        viewModelScope.launch {
            repository.createDirectory(projectId, cleanName, cleanParent)
            _showNewFileDialog.value = false
            _toastMessage.value = "Carpeta creada: $fullPath"
        }
    }

    fun deleteFile(file: ProjectFileEntity) {
        val projectId = _currentProjectId.value ?: return
        viewModelScope.launch {
            repository.deleteFile(projectId, file.id)
            if (file.isDirectory) {
                val tabsToClose = _openTabs.value.filter { it == file.path || it.startsWith("${file.path}/") }
                tabsToClose.forEach { closeTab(it) }
                _toastMessage.value = "Carpeta eliminada: ${file.name}"
            } else {
                closeTab(file.path)
                _toastMessage.value = "Archivo eliminado: ${file.name}"
            }
        }
    }

    fun setViewMode(mode: IdeViewMode) {
        _viewMode.value = mode
    }

    fun toggleChat() {
        _isChatOpen.value = !_isChatOpen.value
    }

    fun setShowNewFileDialog(show: Boolean) {
        _showNewFileDialog.value = show
    }

    // --- Diagnostic Console & Linter ---

    fun runLinterAnalysis() {
        val projectId = _currentProjectId.value ?: return
        viewModelScope.launch {
            repository.runLinterAnalysis(projectId)
            _toastMessage.value = "🔍 Análisis de Linter completado"
        }
    }

    fun clearDiagnosticLogs() {
        val projectId = _currentProjectId.value ?: return
        viewModelScope.launch {
            repository.clearDiagnosticLogs(projectId)
            _toastMessage.value = "Consola de Diagnóstico limpia"
        }
    }

    fun sendDiagnosticsToAi() {
        val currentLogs = uiState.value.diagnosticLogs
        if (currentLogs.isEmpty()) {
            _toastMessage.value = "No hay registros para enviar a la IA."
            return
        }

        val errorsSummary = currentLogs.joinToString("\n") { log ->
            "-[${log.level}][${log.source}] ${log.filePath ?: ""}:${log.lineNumber ?: 0} -> ${log.message}"
        }

        val prompt = "Por favor revisa estos logs de la Consola de Diagnóstico y Linter de mi proyecto y ayúdame a corregirlos:\n```\n$errorsSummary\n```"
        _isChatOpen.value = true
        sendAgentPrompt(prompt)
    }

    // --- Agent Communication ---

    fun sendAgentPrompt(userPrompt: String) {
        val projectId = _currentProjectId.value ?: return
        if (userPrompt.isBlank()) return

        viewModelScope.launch {
            _isAiLoading.value = true

            agentPromptRunner.runPromptStream(
                projectId = projectId,
                userPrompt = userPrompt,
                activeFilePath = _activeFilePath.value,
                editorContent = _editorContent.value,
                filesList = uiState.value.files,
                currentProvider = _selectedAiProvider.value,
                currentChatMode = _selectedChatMode.value,
                openRouterKey = _openRouterApiKey.value,
                geminiKey = _customGeminiApiKey.value,
                onFileContentUpdated = { _, newContent ->
                    _editorContent.value = newContent
                    _savedContent.value = newContent
                },
                onFileSelected = { selectFile(it) },
                onTabClosed = { closeTab(it) },
                onAgentMessageUpdate = { msgId, text, targetPath, code ->
                    viewModelScope.launch {
                        repository.updateChatMessageContent(
                            id = msgId,
                            text = text,
                            targetFilePath = targetPath,
                            proposedCode = code
                        )
                    }
                },
                onAutoApplyProposedCode = { msgId, targetFile, code ->
                    val autoMsg = ChatMessageEntity(
                        id = msgId,
                        projectId = projectId,
                        sender = "agent",
                        text = "Autónomo",
                        targetFilePath = targetFile,
                        proposedCode = code
                    )
                    applyAgentProposedCode(autoMsg)
                }
            )

            _isAiLoading.value = false
        }
    }

    fun applyAgentProposedCode(chatMessage: ChatMessageEntity) {
        val projectId = _currentProjectId.value ?: return
        val path = chatMessage.targetFilePath ?: _activeFilePath.value ?: return
        val newCode = chatMessage.proposedCode ?: return

        viewModelScope.launch {
            val existing = repository.getFileByPath(projectId, path)
            if (existing != null) {
                repository.updateFileContent(projectId, path, newCode)
            } else {
                val fileName = path.substringAfterLast('/')
                repository.createFile(projectId, fileName, path, newCode)
            }

            repository.setMessageApplied(chatMessage.id)

            if (_activeFilePath.value == path) {
                _editorContent.value = newCode
                _savedContent.value = newCode
            } else {
                selectFile(path)
            }

            _toastMessage.value = "✨ Cambios de IA aplicados a $path"
        }
    }

    fun clearChatHistory() {
        val projectId = _currentProjectId.value ?: return
        viewModelScope.launch {
            repository.clearChatHistory(projectId)
            _toastMessage.value = "Chat limpiado. Empezando conversación de cero."
        }
    }

    fun clearToast() {
        _toastMessage.value = null
    }

    fun insertSymbolIntoEditor(symbol: String) {
        val currentText = _editorContent.value
        _editorContent.value = currentText + symbol
    }
}
