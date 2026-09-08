package com.example.data.repository

import com.example.data.local.ChatDao
import com.example.data.local.ChatMessageEntity
import com.example.data.model.ChatMessage
import com.example.data.security.EncryptionHelper
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ChatRepository(private val chatDao: ChatDao) {

    val allMessages: Flow<List<ChatMessage>> = chatDao.getAllMessages().map { entities ->
        entities.map { entity ->
            val decryptedText = EncryptionHelper.decrypt(entity.encryptedText, entity.iv)
            ChatMessage(
                id = entity.id,
                sender = entity.sender,
                text = decryptedText,
                timestamp = entity.timestamp,
                latencyMs = entity.latencyMs,
                mode = entity.mode
            )
        }
    }

    suspend fun saveMessage(message: ChatMessage): Long {
        val encrypted = EncryptionHelper.encrypt(message.text)
        val entity = ChatMessageEntity(
            id = message.id,
            sender = message.sender,
            encryptedText = encrypted.ciphertext,
            iv = encrypted.iv,
            timestamp = message.timestamp,
            latencyMs = message.latencyMs,
            mode = message.mode
        )
        return chatDao.insertMessage(entity)
    }

    suspend fun deleteMessage(id: Long) {
        chatDao.deleteMessageById(id)
    }

    suspend fun clearAllMessages() {
        chatDao.clearAllMessages()
    }
}
