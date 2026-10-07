package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.ChatMode
import com.example.data.model.CompanionConfig
import com.example.data.model.CompanionPersonality
import com.example.data.model.ContentFilterLevel
import com.example.data.model.GirlfriendMood
import com.example.data.model.LanguageStyle

class ConfigPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("chatme_prefs", Context.MODE_PRIVATE)

    fun loadConfig(): CompanionConfig {
        val botName = prefs.getString(KEY_BOT_NAME, "Aria") ?: "Aria"
        val userName = prefs.getString(KEY_USER_NAME, "Kamu") ?: "Kamu"

        val personalityStr = prefs.getString(KEY_PERSONALITY, CompanionPersonality.FLIRTY_ROMANTIC.name)
        val personality = try {
            CompanionPersonality.valueOf(personalityStr ?: CompanionPersonality.FLIRTY_ROMANTIC.name)
        } catch (_: Exception) {
            CompanionPersonality.FLIRTY_ROMANTIC
        }

        val languageStyleStr = prefs.getString(KEY_LANGUAGE_STYLE, LanguageStyle.MANJA_SAYANG.name)
        val languageStyle = try {
            LanguageStyle.valueOf(languageStyleStr ?: LanguageStyle.MANJA_SAYANG.name)
        } catch (_: Exception) {
            LanguageStyle.MANJA_SAYANG
        }

        val modeStr = prefs.getString(KEY_MODE, ChatMode.MATURE.name)
        val mode = try {
            ChatMode.valueOf(modeStr ?: ChatMode.MATURE.name)
        } catch (_: Exception) {
            ChatMode.MATURE
        }

        val moodStr = prefs.getString(KEY_MOOD, GirlfriendMood.BUCIN_MANJA.name)
        val mood = try {
            GirlfriendMood.valueOf(moodStr ?: GirlfriendMood.BUCIN_MANJA.name)
        } catch (_: Exception) {
            GirlfriendMood.BUCIN_MANJA
        }

        val filterLevelStr = prefs.getString(KEY_FILTER_LEVEL, ContentFilterLevel.OFF.name)
        val filterLevel = try {
            ContentFilterLevel.valueOf(filterLevelStr ?: ContentFilterLevel.OFF.name)
        } catch (_: Exception) {
            ContentFilterLevel.OFF
        }

        val customApiKey = prefs.getString(KEY_CUSTOM_API_KEY, "") ?: ""
        val customApiKeysJson = prefs.getString(KEY_CUSTOM_API_KEYS, null)
        val customApiKeys = if (!customApiKeysJson.isNullOrBlank()) {
            try {
                val jsonArr = org.json.JSONArray(customApiKeysJson)
                (0 until jsonArr.length()).map { jsonArr.getString(it) }.filter { it.isNotBlank() }
            } catch (_: Exception) {
                if (customApiKey.isNotBlank()) listOf(customApiKey) else emptyList()
            }
        } else if (customApiKey.isNotBlank()) {
            listOf(customApiKey)
        } else {
            emptyList()
        }

        val selectedModel = prefs.getString(KEY_MODEL, "gemini-3.5-flash") ?: "gemini-3.5-flash"
        val temperature = prefs.getFloat(KEY_TEMPERATURE, 0.90f)
        val avatarUri = prefs.getString(KEY_AVATAR_URI, "") ?: ""
        val userAvatarUri = prefs.getString(KEY_USER_AVATAR_URI, "") ?: ""

        return CompanionConfig(
            botName = botName,
            userName = userName,
            personality = personality,
            languageStyle = languageStyle,
            mode = mode,
            mood = mood,
            filterLevel = filterLevel,
            customApiKey = customApiKeys.firstOrNull() ?: customApiKey,
            customApiKeys = customApiKeys,
            selectedModel = selectedModel,
            temperature = temperature,
            avatarUri = avatarUri,
            userAvatarUri = userAvatarUri
        )
    }

    fun saveConfig(config: CompanionConfig) {
        val keysToSave = config.getActiveApiKeys()
        val jsonArr = org.json.JSONArray()
        keysToSave.forEach { jsonArr.put(it) }

        prefs.edit().apply {
            putString(KEY_BOT_NAME, config.botName)
            putString(KEY_USER_NAME, config.userName)
            putString(KEY_PERSONALITY, config.personality.name)
            putString(KEY_LANGUAGE_STYLE, config.languageStyle.name)
            putString(KEY_MODE, config.mode.name)
            putString(KEY_MOOD, config.mood.name)
            putString(KEY_FILTER_LEVEL, config.filterLevel.name)
            putString(KEY_CUSTOM_API_KEY, keysToSave.firstOrNull() ?: "")
            putString(KEY_CUSTOM_API_KEYS, jsonArr.toString())
            putString(KEY_MODEL, config.selectedModel)
            putFloat(KEY_TEMPERATURE, config.temperature)
            putString(KEY_AVATAR_URI, config.avatarUri)
            putString(KEY_USER_AVATAR_URI, config.userAvatarUri)
            apply()
        }
    }

    companion object {
        private const val KEY_BOT_NAME = "key_bot_name"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_PERSONALITY = "key_personality"
        private const val KEY_LANGUAGE_STYLE = "key_language_style"
        private const val KEY_MODE = "key_mode"
        private const val KEY_MOOD = "key_mood"
        private const val KEY_FILTER_LEVEL = "key_filter_level"
        private const val KEY_CUSTOM_API_KEY = "key_custom_api_key"
        private const val KEY_CUSTOM_API_KEYS = "key_custom_api_keys"
        private const val KEY_MODEL = "key_model"
        private const val KEY_TEMPERATURE = "key_temperature"
        private const val KEY_AVATAR_URI = "key_avatar_uri"
        private const val KEY_USER_AVATAR_URI = "key_user_avatar_uri"
    }
}
