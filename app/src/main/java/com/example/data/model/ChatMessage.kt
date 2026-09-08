package com.example.data.model

data class ChatMessage(
    val id: Long = 0,
    val sender: String, // "user" or "assistant"
    val text: String,
    val timestamp: Long = System.currentTimeMillis(),
    val isStreaming: Boolean = false,
    val latencyMs: Long? = null,
    val mode: String = "standard" // "standard" or "mature"
)
