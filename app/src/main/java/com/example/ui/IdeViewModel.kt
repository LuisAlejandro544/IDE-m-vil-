package com.example.ui

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.AiAgentService
import com.example.data.api.AiProvider
import com.example.data.db.AppDatabase
import com.example.data.db.ChatMessageEntity
import com.example.data.db.ProjectFileEntity
import com.example.data.repository.IdeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class IdeViewMode {
    EDITOR,
    PREVIEW,
    SPLIT
}

private data class EditorState(
    val files: List<ProjectFileEntity>,
    val openTabs: List<String>,
    val activeFilePath: String?,
    val editorContent: String,
    val savedContent: String
)

private data class ViewState(
    val viewMode: IdeViewMode,
    val isChatOpen: Boolean,
    val showNewFileDialog: Boolean,
    val toastMessage: String?
)

private data class AiState(
    val chatMessages: List<ChatMessageEntity>,
    val isAiLoading: Boolean,
    val selectedAiProvider: AiProvider,
    val openRouterApiKey: String,
    val customGeminiApiKey: String
)

data class IdeUiState(
    val files: List<ProjectFileEntity> = emptyList(),
    val openTabs: List<String> = emptyList(),
    val activeFilePath: String? = null,
    val activeFileContent: String = "",
    val hasUnsavedChanges: Boolean = false,
    val viewMode: IdeViewMode = IdeViewMode.EDITOR,
    val isChatOpen: Boolean = false,
    val chatMessages: List<ChatMessageEntity> = emptyList(),
    val isAiLoading: Boolean = false,
    val selectedAiProvider: AiProvider = AiProvider.GEMINI,
    val openRouterApiKey: String = "",
    val customGeminiApiKey: String = "",
    val showAiSettingsDialog: Boolean = false,
    val showNewFileDialog: Boolean = false,
    val pendingActionMessage: String? = null
)

class IdeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = IdeRepository(db.projectFileDao(), db.chatMessageDao())
    private val aiAgentService = AiAgentService()

    private val prefs = application.getSharedPreferences("devstudio_ai_prefs", Context.MODE_PRIVATE)

    private val _openTabs = MutableStateFlow<List<String>>(listOf("/index.html", "/style.css", "/script.js"))
    private val _activeFilePath = MutableStateFlow<String?>("/index.html")
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

    private val editorStateFlow = combine(
        repository.allFiles,
        _openTabs,
        _activeFilePath,
        _editorContent,
        _savedContent
    ) { files, tabs, activePath, editorText, savedText ->
        EditorState(files, tabs, activePath, editorText, savedText)
    }

    private val viewStateFlow = combine(
        _viewMode,
        _isChatOpen,
        _showNewFileDialog,
        _toastMessage
    ) { viewMode, isChatOpen, showNewFile, toast ->
        ViewState(viewMode, isChatOpen, showNewFile, toast)
    }

    private val aiStateFlow = combine(
        repository.chatMessages,
        _isAiLoading,
        _selectedAiProvider,
        _openRouterApiKey,
        _customGeminiApiKey
    ) { msgs, aiLoading, provider, openRouterKey, geminiKey ->
        AiState(msgs, aiLoading, provider, openRouterKey, geminiKey)
    }

    val uiState: StateFlow<IdeUiState> = combine(
        editorStateFlow,
        viewStateFlow,
        aiStateFlow,
        _showAiSettingsDialog
    ) { editor, view, ai, showSettings ->
        IdeUiState(
            files = editor.files,
            openTabs = editor.openTabs,
            activeFilePath = editor.activeFilePath,
            activeFileContent = editor.editorContent,
            hasUnsavedChanges = editor.editorContent != editor.savedContent,
            viewMode = view.viewMode,
            isChatOpen = view.isChatOpen,
            chatMessages = ai.chatMessages,
            isAiLoading = ai.isAiLoading,
            selectedAiProvider = ai.selectedAiProvider,
            openRouterApiKey = ai.openRouterApiKey,
            customGeminiApiKey = ai.customGeminiApiKey,
            showAiSettingsDialog = showSettings,
            showNewFileDialog = view.showNewFileDialog,
            pendingActionMessage = view.toastMessage
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = IdeUiState()
    )

    init {
        // Load saved AI Preferences
        val savedProviderName = prefs.getString("ai_provider", AiProvider.GEMINI.name) ?: AiProvider.GEMINI.name
        _selectedAiProvider.value = try { AiProvider.valueOf(savedProviderName) } catch (e: Exception) { AiProvider.GEMINI }
        _openRouterApiKey.value = prefs.getString("openrouter_api_key", "") ?: ""
        _customGeminiApiKey.value = prefs.getString("gemini_api_key", "") ?: ""

        viewModelScope.launch {
            repository.ensureDefaultFilesExist()
            selectFile("/index.html")
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
        viewModelScope.launch {
            val file = repository.getFileByPath(path)
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
        val currentPath = _activeFilePath.value ?: return
        val currentText = _editorContent.value
        viewModelScope.launch {
            repository.updateFileContent(currentPath, currentText)
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
        if (fileName.isBlank()) return
        val cleanName = fileName.trim().removePrefix("/")
        val cleanParent = if (parentPath.endsWith("/") && parentPath != "/") parentPath.dropLast(1) else parentPath
        val fullPath = if (cleanParent == "/") "/$cleanName" else "$cleanParent/$cleanName"
        viewModelScope.launch {
            repository.createFile(cleanName, fullPath, initialContent, cleanParent)
            _showNewFileDialog.value = false
            selectFile(fullPath)
            _toastMessage.value = "Archivo creado: $fullPath"
        }
    }

    fun createNewFolder(folderName: String, parentPath: String = "/") {
        if (folderName.isBlank()) return
        val cleanName = folderName.trim().removePrefix("/")
        val cleanParent = if (parentPath.endsWith("/") && parentPath != "/") parentPath.dropLast(1) else parentPath
        val fullPath = if (cleanParent == "/") "/$cleanName" else "$cleanParent/$cleanName"
        viewModelScope.launch {
            repository.createDirectory(cleanName, cleanParent)
            _showNewFileDialog.value = false
            _toastMessage.value = "Carpeta creada: $fullPath"
        }
    }

    fun deleteFile(file: ProjectFileEntity) {
        viewModelScope.launch {
            repository.deleteFile(file.id)
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

    fun sendAgentPrompt(userPrompt: String) {
        if (userPrompt.isBlank()) return

        viewModelScope.launch {
            // Save user message
            repository.addChatMessage(
                ChatMessageEntity(
                    sender = "user",
                    text = userPrompt,
                    timestamp = System.currentTimeMillis()
                )
            )

            // Insert initial agent message for real-time streaming
            val agentMsgId = repository.addChatMessage(
                ChatMessageEntity(
                    sender = "agent",
                    text = "Thinking...",
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
                    executeAgentTool(toolName, args)
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

    private suspend fun executeAgentTool(toolName: String, args: org.json.JSONObject): String {
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
                    val f = repository.getFileByPath(path)
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
                    val res = repository.editFileContentByTarget(path, targetContent, replacementContent)
                    if (_activeFilePath.value == path) {
                        val updated = repository.getFileByPath(path)
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
                    repository.createFile(fileName, cleanPath, content, parentPath = parent)
                    selectFile(cleanPath)
                    "✅ Éxito: Archivo '$cleanPath' creado y abierto."
                }
            }

            "delete_file" -> {
                val path = args.optString("path")
                if (path.isBlank()) "❌ Ruta requerida."
                else {
                    val res = repository.deleteFileByPath(path)
                    closeTab(path)
                    res
                }
            }

            else -> "⚠️ Herramienta '$toolName' no reconocida."
        }
    }

    fun applyAgentProposedCode(chatMessage: ChatMessageEntity) {
        val path = chatMessage.targetFilePath ?: _activeFilePath.value ?: return
        val newCode = chatMessage.proposedCode ?: return

        viewModelScope.launch {
            val existing = repository.getFileByPath(path)
            if (existing != null) {
                repository.updateFileContent(path, newCode)
            } else {
                val fileName = path.substringAfterLast('/')
                repository.createFile(fileName, path, newCode)
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

// Internal tuple helper for combining state flows
private data class Tuple7<A, B, C, D, E, F, G>(
    val v1: A, val v2: B, val v3: C, val v4: D, val v5: E, val v6: F, val v7: G
)
