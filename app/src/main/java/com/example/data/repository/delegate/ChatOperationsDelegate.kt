package com.example.data.repository.delegate

import com.example.data.db.ChatMessageDao
import com.example.data.db.ChatMessageEntity

class ChatOperationsDelegate(
    private val chatMessageDao: ChatMessageDao
) {

    suspend fun addChatMessage(projectId: Long, message: ChatMessageEntity): Long {
        return chatMessageDao.insertMessage(message.copy(projectId = projectId))
    }

    suspend fun updateChatMessageContent(id: Long, text: String, targetFilePath: String?, proposedCode: String?) {
        chatMessageDao.updateMessageContent(id, text, targetFilePath, proposedCode)
    }

    suspend fun setMessageApplied(id: Long) {
        chatMessageDao.setMessageApplied(id)
    }

    suspend fun clearChatHistory(projectId: Long) {
        chatMessageDao.clearHistoryForProject(projectId)
    }
}
