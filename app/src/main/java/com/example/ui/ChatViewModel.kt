package com.example.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChatDatabase
import com.example.data.model.ChatMessage
import com.example.data.model.ChatMode
import com.example.data.model.CompanionConfig
import com.example.data.remote.GeminiApiClient
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class ChatViewModel(application: Application) : AndroidViewModel(application) {

    private val database = ChatDatabase.getDatabase(application)
    private val repository = ChatRepository(database.chatDao())
    private val geminiClient = GeminiApiClient()
    private val configPrefs = com.example.data.local.ConfigPreferences(application)

    private val _config = MutableStateFlow(configPrefs.loadConfig())
    val config: StateFlow<CompanionConfig> = _config.asStateFlow()

    private val _isGenerating = MutableStateFlow(false)
    val isGenerating: StateFlow<Boolean> = _isGenerating.asStateFlow()

    private val _streamingMessage = MutableStateFlow<ChatMessage?>(null)
    val streamingMessage: StateFlow<ChatMessage?> = _streamingMessage.asStateFlow()

    private val _lastLatencyMs = MutableStateFlow<Long?>(null)
    val lastLatencyMs: StateFlow<Long?> = _lastLatencyMs.asStateFlow()

    // Combined list of persisted messages + currently streaming message
    val messages: StateFlow<List<ChatMessage>> = combine(
        repository.allMessages,
        _streamingMessage
    ) { storedMessages, streamingMsg ->
        if (streamingMsg != null) {
            storedMessages + streamingMsg
        } else {
            storedMessages
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        // Initialize welcoming companion message if database is empty
        viewModelScope.launch {
            repository.allMessages.collect { list ->
                if (list.isEmpty() && _streamingMessage.value == null) {
                    val currentCfg = _config.value
                    val greeting = "Hai ${currentCfg.userName}! Aku ${currentCfg.botName}. Aku senang banget bisa nemenin kamu hari ini. Kalau ada cerita, unek-unek, atau apapun yang bikin harimu lelah, ceritain ke aku ya... Aku siap dengerin semuanya dengan tulus. 💕 Gimana harimu hari ini?"
                    repository.saveMessage(
                        ChatMessage(
                            sender = "assistant",
                            text = greeting,
                            mode = currentCfg.mode.name.lowercase()
                        )
                    )
                }
            }
        }
    }

    fun sendMessage(userText: String) {
        val trimmed = userText.trim()
        if (trimmed.isBlank() || _isGenerating.value) return

        val currentConfig = _config.value
        val startTime = System.currentTimeMillis()

        viewModelScope.launch {
            // 1. Save user message to encrypted Room database
            val userMessage = ChatMessage(
                sender = "user",
                text = trimmed,
                mode = currentConfig.mode.name.lowercase()
            )
            repository.saveMessage(userMessage)

            // 2. Prepare streaming state
            _isGenerating.value = true
            val initialAssistantMsg = ChatMessage(
                sender = "assistant",
                text = "",
                isStreaming = true,
                mode = currentConfig.mode.name.lowercase()
            )
            _streamingMessage.value = initialAssistantMsg

            var firstChunkReceived = false
            val fullResponseBuilder = StringBuilder()

            try {
                geminiClient.streamChat(
                    history = messages.value.filter { !it.isStreaming },
                    userMessage = trimmed,
                    config = currentConfig
                ).collect { chunk ->
                    if (!firstChunkReceived) {
                        firstChunkReceived = true
                        val latency = System.currentTimeMillis() - startTime
                        _lastLatencyMs.value = latency
                    }
                    fullResponseBuilder.append(chunk)
                    _streamingMessage.value = initialAssistantMsg.copy(
                        text = fullResponseBuilder.toString(),
                        latencyMs = _lastLatencyMs.value
                    )
                }

                // 3. Save completed assistant message to encrypted database
                val completeText = fullResponseBuilder.toString().ifBlank {
                    "${currentConfig.userName} sayang, maaf tadi aku melamun sebentar. Bisa ulangi lagi ceritanya?"
                }

                val finalMessage = ChatMessage(
                    sender = "assistant",
                    text = completeText,
                    latencyMs = _lastLatencyMs.value,
                    mode = currentConfig.mode.name.lowercase()
                )
                repository.saveMessage(finalMessage)
            } catch (e: Exception) {
                val errorMsg = "Maaf ya, ada kendala koneksi sebentar. Tapi aku tetap setia di sini buat kamu!"
                repository.saveMessage(
                    ChatMessage(
                        sender = "assistant",
                        text = errorMsg,
                        mode = currentConfig.mode.name.lowercase()
                    )
                )
            } finally {
                _streamingMessage.value = null
                _isGenerating.value = false
            }
        }
    }

    fun updateConfig(newConfig: CompanionConfig) {
        _config.value = newConfig
        configPrefs.saveConfig(newConfig)
    }

    fun toggleMode(targetMode: ChatMode) {
        val updated = _config.value.copy(mode = targetMode)
        _config.value = updated
        configPrefs.saveConfig(updated)
    }

    fun clearHistory() {
        viewModelScope.launch {
            repository.clearAllMessages()
        }
    }
}
