package com.example.ui.handler

import com.example.data.api.AiAgentService
import com.example.data.api.AiProvider
import com.example.data.api.ChatMode
import com.example.data.db.ChatMessageEntity
import com.example.data.db.ProjectFileEntity
import com.example.data.repository.IdeRepository
import com.example.ui.delegate.AgentToolExecutor

class AgentPromptRunner(
    private val repository: IdeRepository,
    private val aiAgentService: AiAgentService,
    private val toolExecutor: AgentToolExecutor
) {

    suspend fun runPromptStream(
        projectId: Long,
        userPrompt: String,
        activeFilePath: String?,
        editorContent: String,
        filesList: List<ProjectFileEntity>,
        currentProvider: AiProvider,
        currentChatMode: ChatMode,
        openRouterKey: String,
        geminiKey: String,
        onFileContentUpdated: (String, String) -> Unit,
        onFileSelected: (String) -> Unit,
        onTabClosed: (String) -> Unit,
        onAgentMessageUpdate: (Long, String, String?, String?) -> Unit,
        onAutoApplyProposedCode: (Long, String?, String?) -> Unit
    ) {
        val filesSummary = filesList.joinToString("\n") { f ->
            "- ${f.path} (${f.content.length} caracteres)"
        }

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

        var lastTargetFile: String? = null
        var lastProposedCode: String? = null

        aiAgentService.streamUserPrompt(
            provider = currentProvider,
            chatMode = currentChatMode,
            userPrompt = userPrompt,
            currentFileContent = editorContent,
            currentFilePath = activeFilePath,
            allFilesSummary = filesSummary,
            openRouterApiKey = openRouterKey,
            customGeminiApiKey = geminiKey,
            onExecuteTool = { toolName, args ->
                toolExecutor.executeTool(
                    projectId = projectId,
                    toolName = toolName,
                    args = args,
                    filesList = filesList,
                    activeFilePath = activeFilePath,
                    onFileContentUpdated = onFileContentUpdated,
                    onFileSelected = onFileSelected,
                    onTabClosed = onTabClosed
                )
            }
        ).collect { streamResult ->
            lastTargetFile = streamResult.targetFilePath
            lastProposedCode = streamResult.proposedCode

            onAgentMessageUpdate(
                agentMsgId,
                streamResult.explanation,
                streamResult.targetFilePath,
                streamResult.proposedCode
            )
        }

        if (currentChatMode == ChatMode.FULL_AUTONOMOUS && lastProposedCode != null) {
            onAutoApplyProposedCode(agentMsgId, lastTargetFile, lastProposedCode)
        }
    }
}
