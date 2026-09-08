package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chat_messages")
data class ChatMessageEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val sender: String, // "user" or "assistant"
    val encryptedText: String,
    val iv: String,
    val timestamp: Long = System.currentTimeMillis(),
    val latencyMs: Long? = null,
    val mode: String = "standard" // "standard" or "mature"
)
