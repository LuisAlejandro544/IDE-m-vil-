package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.api.GeminiAgentService
import com.example.data.db.AppDatabase
import com.example.data.db.ChatMessageEntity
import com.example.data.db.ProjectFileEntity
import com.example.data.repository.IdeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

enum class IdeViewMode {
    EDITOR,
    PREVIEW,
    SPLIT
}

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
    val showNewFileDialog: Boolean = false,
    val pendingActionMessage: String? = null
)

class IdeViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getDatabase(application)
    private val repository = IdeRepository(db.projectFileDao(), db.chatMessageDao())
    private val agentService = GeminiAgentService()

    private val _openTabs = MutableStateFlow<List<String>>(listOf("/index.html", "/style.css", "/script.js"))
    private val _activeFilePath = MutableStateFlow<String?>("/index.html")
    private val _editorContent = MutableStateFlow<String>("")
    private val _savedContent = MutableStateFlow<String>("")
    private val _viewMode = MutableStateFlow(IdeViewMode.EDITOR)
    private val _isChatOpen = MutableStateFlow(false)
    private val _isAiLoading = MutableStateFlow(false)
    private val _showNewFileDialog = MutableStateFlow(false)
    private val _toastMessage = MutableStateFlow<String?>(null)

    val uiState: StateFlow<IdeUiState> = combine(
        repository.allFiles,
        _openTabs,
        _activeFilePath,
        _editorContent,
        _savedContent,
        _viewMode,
        _isChatOpen,
        repository.chatMessages,
        _isAiLoading,
        _showNewFileDialog,
        _toastMessage
    ) { args ->
        @Suppress("UNCHECKED_CAST")
        val files = args[0] as List<ProjectFileEntity>
        val tabs = args[1] as List<String>
        val activePath = args[2] as String?
        val editorText = args[3] as String
        val savedText = args[4] as String
        val vMode = args[5] as IdeViewMode
        val chatOpen = args[6] as Boolean
        val messages = args[7] as List<ChatMessageEntity>
        val aiLoading = args[8] as Boolean
        val showDialog = args[9] as Boolean
        val toast = args[10] as String?

        IdeUiState(
            files = files,
            openTabs = tabs,
            activeFilePath = activePath,
            activeFileContent = editorText,
            hasUnsavedChanges = editorText != savedText,
            viewMode = vMode,
            isChatOpen = chatOpen,
            chatMessages = messages,
            isAiLoading = aiLoading,
            showNewFileDialog = showDialog,
            pendingActionMessage = toast
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = IdeUiState()
    )

    init {
        viewModelScope.launch {
            repository.ensureDefaultFilesExist()
            // Load active file
            selectFile("/index.html")
        }
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

    fun createNewFile(fileName: String, initialContent: String = "") {
        if (fileName.isBlank()) return
        val cleanName = fileName.trim().removePrefix("/")
        val path = "/$cleanName"
        viewModelScope.launch {
            repository.createFile(cleanName, path, initialContent)
            _showNewFileDialog.value = false
            selectFile(path)
            _toastMessage.value = "Archivo creado: $path"
        }
    }

    fun deleteFile(file: ProjectFileEntity) {
        viewModelScope.launch {
            repository.deleteFile(file.id)
            closeTab(file.path)
            _toastMessage.value = "Archivo eliminado: ${file.name}"
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

            _isAiLoading.value = true

            val activePath = _activeFilePath.value
            val activeContent = _editorContent.value
            val filesList = uiState.value.files
            val filesSummary = filesList.joinToString("\n") { f ->
                "- ${f.path} (${f.content.length} caracteres)"
            }

            val response = agentService.processUserPrompt(
                userPrompt = userPrompt,
                currentFileContent = activeContent,
                currentFilePath = activePath,
                allFilesSummary = filesSummary
            )

            repository.addChatMessage(
                ChatMessageEntity(
                    sender = "agent",
                    text = response.explanation,
                    targetFilePath = response.targetFilePath,
                    proposedCode = response.proposedCode,
                    timestamp = System.currentTimeMillis()
                )
            )

            _isAiLoading.value = false
        }
    }

    fun applyAgentProposedCode(chatMessage: ChatMessageEntity) {
        val path = chatMessage.targetFilePath ?: _activeFilePath.value ?: return
        val newCode = chatMessage.proposedCode ?: return

        viewModelScope.launch {
            // Check if file exists, if not create it
            val existing = repository.getFileByPath(path)
            if (existing != null) {
                repository.updateFileContent(path, newCode)
            } else {
                val fileName = path.substringAfterLast('/')
                repository.createFile(fileName, path, newCode)
            }

            // Mark message as applied
            repository.setMessageApplied(chatMessage.id)

            // If active file, update editor content
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
