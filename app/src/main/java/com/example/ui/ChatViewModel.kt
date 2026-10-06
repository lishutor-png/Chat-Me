package com.example.ui

import android.app.Application
import android.net.Uri
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.ChatDatabase
import com.example.data.model.ApiConfigStatusInfo
import com.example.data.model.ApiHealthState
import com.example.data.model.ChatMessage
import com.example.data.model.ChatMode
import com.example.data.model.CompanionConfig
import com.example.data.remote.GeminiApiClient
import com.example.data.repository.ChatRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

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

    private val _apiStatus = MutableStateFlow(
         buildInitialApiStatus(_config.value)
    )
    val apiStatus: StateFlow<ApiConfigStatusInfo> = _apiStatus.asStateFlow()

    private val _isCheckingApi = MutableStateFlow(false)
    val isCheckingApi: StateFlow<Boolean> = _isCheckingApi.asStateFlow()

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
        // Automatically check API connection in background on startup
        verifyApiConnection(_config.value)
    }

    private fun buildInitialApiStatus(cfg: CompanionConfig): ApiConfigStatusInfo {
        val (keys, isSys) = geminiClient.getRawActiveKeys(cfg)
        return if (keys.isEmpty()) {
            ApiConfigStatusInfo(
                state = ApiHealthState.FALLBACK_READY,
                summaryTitle = "Mode Mandiri (Tanpa API Key)",
                detailMessage = "Belum ada API key eksternal. Chat tetap aktif dengan mode mandiri lokal.",
                totalKeysCount = 0,
                reachableKeysCount = 0
            )
        } else {
            ApiConfigStatusInfo(
                state = ApiHealthState.CONNECTED,
                summaryTitle = if (isSys) "Tersambung • API Sistem Siap"
                else if (keys.size > 1) "Tersambung • ${keys.size} Slot API Siap"
                else "Tersambung • API Config Siap",
                detailMessage = "API Config terkonfigurasi dan siap diakses.",
                activeKeyIndex = 1,
                totalKeysCount = keys.size,
                reachableKeysCount = keys.size,
                usingSystemDefaultKey = isSys
            )
        }
    }

    fun verifyApiConnection(configToTest: CompanionConfig = _config.value) {
        if (_isCheckingApi.value) return
        viewModelScope.launch {
            _isCheckingApi.value = true
            _apiStatus.value = _apiStatus.value.copy(
                state = ApiHealthState.CHECKING,
                summaryTitle = "Mengecek Koneksi API...",
                detailMessage = "Memeriksa apakah API Config masih tersambung dan dapat diakses..."
            )
            try {
                val result = geminiClient.checkApiConfigAccess(configToTest)
                _apiStatus.value = result
                if (result.latencyMs != null) {
                    _lastLatencyMs.value = result.latencyMs
                }
            } finally {
                _isCheckingApi.value = false
            }
        }
    }

    fun copyGalleryImageToInternalStorage(
        uri: Uri,
        prefix: String = "avatar",
        onResult: (String) -> Unit
    ) {
        viewModelScope.launch {
            val savedPath = withContext(Dispatchers.IO) {
                try {
                    val app = getApplication<Application>()
                    val inputStream = app.contentResolver.openInputStream(uri) ?: return@withContext null
                    val dir = File(app.filesDir, "avatars").apply { mkdirs() }
                    val destFile = File(dir, "${prefix}_${System.currentTimeMillis()}.jpg")
                    inputStream.use { input ->
                        FileOutputStream(destFile).use { output ->
                            input.copyTo(output)
                        }
                    }
                    destFile.absolutePath
                } catch (e: Exception) {
                    null
                }
            }
            if (!savedPath.isNullOrBlank()) {
                onResult(savedPath)
            } else {
                onResult(uri.toString())
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
                    config = currentConfig,
                    onStatusUpdated = { liveStatus ->
                        _apiStatus.value = liveStatus
                    }
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
        val oldKeys = _config.value.getActiveApiKeys()
        _config.value = newConfig
        configPrefs.saveConfig(newConfig)
        if (newConfig.getActiveApiKeys() != oldKeys) {
            verifyApiConnection(newConfig)
        }
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

