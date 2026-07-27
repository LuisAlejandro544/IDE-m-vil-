package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.AiAgentService
import com.example.data.api.AiProvider
import com.example.data.db.AppDatabase
import com.example.data.db.ChatMessageEntity
import com.example.data.db.DiagnosticLogEntity
import com.example.data.db.ProjectEntity
import com.example.data.db.ProjectFileEntity
import com.example.data.repository.IdeRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class IdeViewMode {
    EDITOR,
    PREVIEW,
    SPLIT,
    DIAGNOSTICS
}

private data class ProjectState(
    val currentProjectId: Long?,
    val currentProject: ProjectEntity?,
    val projects: List<ProjectEntity>
)

private data class EditorState(
    val files: List<ProjectFileEntity>,
    val openTabs: List<String>,
    val activeFilePath: String?,
    val editorContent: String,
    val savedContent: String
)

private data class UiControlState(
    val viewMode: IdeViewMode,
    val isChatOpen: Boolean,
    val isAiLoading: Boolean,
    val showAiSettingsDialog: Boolean,
    val showNewFileDialog: Boolean,
    val toastMessage: String?
)

private data class AiAndDiagnosticsState(
    val chatMessages: List<ChatMessageEntity>,
    val diagnosticLogs: List<DiagnosticLogEntity>,
    val selectedAiProvider: AiProvider,
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
    val openRouterApiKey: String = "",
    val customGeminiApiKey: String = "",
    val showAiSettingsDialog: Boolean = false,
    val showNewFileDialog: Boolean = false,
    val pendingActionMessage: String? = null
)

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

    private val prefs = application.getSharedPreferences("devstudio_ai_prefs", Context.MODE_PRIVATE)

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
        _openRouterApiKey,
        _customGeminiApiKey
    ) { msgs, logs, provider, openRouterKey, geminiKey ->
        AiAndDiagnosticsState(msgs, logs, provider, openRouterKey, geminiKey)
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
        val savedProviderName = prefs.getString("ai_provider", AiProvider.GEMINI.name) ?: AiProvider.GEMINI.name
        _selectedAiProvider.value = try { AiProvider.valueOf(savedProviderName) } catch (e: Exception) { AiProvider.GEMINI }
        _openRouterApiKey.value = prefs.getString("openrouter_api_key", "") ?: ""
        _customGeminiApiKey.value = prefs.getString("gemini_api_key", "") ?: ""

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
        prefs.edit()
            .putString("ai_provider", _selectedAiProvider.value.name)
            .putString("openrouter_api_key", _openRouterApiKey.value)
            .putString("gemini_api_key", _customGeminiApiKey.value)
            .apply()
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
        val projectId = _currentProjectId.value ?: return
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
            repository.addChatMessage(
                projectId = projectId,
                message = ChatMessageEntity(
                    projectId = projectId,
                    sender = "user",
                    text = userPrompt,
                    timestamp = System.currentTimeMillis()
                )
            )

            val agentMsgId = repository.addChatMessage(
                projectId = projectId,
                message = ChatMessageEntity(
                    projectId = projectId,
                    sender = "agent",
                    text = "Analizando código y diagnóstico...",
                    timestamp = System.currentTimeMillis()
                )
            )

            _isAiLoading.value = true

            val activePath = _activeFilePath.value
            val activeContent = _editorContent.value
            val filesList = uiState.value.files
            val filesSummary = filesList.joinToString("\n") { f ->
                "- ${f.path} (${f.content.length} caracteres)"
            }

            val currentProvider = _selectedAiProvider.value
            val openRouterKey = _openRouterApiKey.value
            val geminiKey = _customGeminiApiKey.value

            aiAgentService.streamUserPrompt(
                provider = currentProvider,
                userPrompt = userPrompt,
                currentFileContent = activeContent,
                currentFilePath = activePath,
                allFilesSummary = filesSummary,
                openRouterApiKey = openRouterKey,
                customGeminiApiKey = geminiKey,
                onExecuteTool = { toolName, args ->
                    executeAgentTool(projectId, toolName, args)
                }
            ).collect { streamResult ->
                repository.updateChatMessageContent(
                    id = agentMsgId,
                    text = streamResult.explanation,
                    targetFilePath = streamResult.targetFilePath,
                    proposedCode = streamResult.proposedCode
                )
            }

            _isAiLoading.value = false
        }
    }

    private suspend fun executeAgentTool(projectId: Long, toolName: String, args: org.json.JSONObject): String {
        return when (toolName) {
            "get_project_structure" -> {
                val filesList = uiState.value.files
                if (filesList.isEmpty()) "Proyecto vacío"
                else filesList.joinToString("\n") { f ->
                    if (f.isDirectory) "📁 [DIR] ${f.path}" else "📄 ${f.path} (${f.content.length} chars)"
                }
            }

            "read_file" -> {
                val path = args.optString("path")
                if (path.isBlank()) "❌ Ruta de archivo requerida."
                else {
                    val f = repository.getFileByPath(projectId, path)
                    if (f == null) "❌ Archivo '$path' no encontrado."
                    else "📄 Contenido de '$path':\n```\n${f.content}\n```"
                }
            }

            "edit_file" -> {
                val path = args.optString("path")
                val targetContent = args.optString("target_content")
                val replacementContent = args.optString("replacement_content")

                if (path.isBlank()) "❌ Ruta de archivo requerida."
                else {
                    val res = repository.editFileContentByTarget(projectId, path, targetContent, replacementContent)
                    if (_activeFilePath.value == path) {
                        val updated = repository.getFileByPath(projectId, path)
                        if (updated != null) {
                            _editorContent.value = updated.content
                            _savedContent.value = updated.content
                        }
                    }
                    res
                }
            }

            "create_file" -> {
                val path = args.optString("path")
                val content = args.optString("content")
                if (path.isBlank()) "❌ Ruta de archivo requerida."
                else {
                    val cleanPath = if (path.startsWith("/")) path else "/$path"
                    val fileName = cleanPath.substringAfterLast('/')
                    val parent = if (cleanPath.count { it == '/' } > 1) cleanPath.substringBeforeLast('/') else "/"
                    repository.createFile(projectId, fileName, cleanPath, content, parentPath = parent)
                    selectFile(cleanPath)
                    "✅ Éxito: Archivo '$cleanPath' creado y abierto."
                }
            }

            "delete_file" -> {
                val path = args.optString("path")
                if (path.isBlank()) "❌ Ruta requerida."
                else {
                    val res = repository.deleteFileByPath(projectId, path)
                    closeTab(path)
                    res
                }
            }

            "get_diagnostics" -> {
                val logs = repository.runLinterAnalysis(projectId)
                if (logs.isEmpty()) "🟢 Consola de Diagnóstico: Sin errores reportados."
                else {
                    val sb = StringBuilder("🔍 Reporte de Consola de Diagnóstico & Linter:\n")
                    logs.forEach { log ->
                        sb.append("- [${log.level}][${log.source}] ${log.filePath ?: ""}:${log.lineNumber ?: 0} -> ${log.message}\n")
                    }
                    sb.toString()
                }
            }

            else -> "⚠️ Herramienta '$toolName' no reconocida."
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

    fun clearToast() {
        _toastMessage.value = null
    }

    fun insertSymbolIntoEditor(symbol: String) {
        val currentText = _editorContent.value
        _editorContent.value = currentText + symbol
    }
}
