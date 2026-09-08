package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import com.example.data.model.ChatMode
import com.example.data.model.CompanionConfig
import com.example.data.model.CompanionPersonality
import com.example.data.model.ContentFilterLevel
import com.example.data.model.LanguageStyle

class ConfigPreferences(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences("chatme_prefs", Context.MODE_PRIVATE)

    fun loadConfig(): CompanionConfig {
        val botName = prefs.getString(KEY_BOT_NAME, "Aria") ?: "Aria"
        val userName = prefs.getString(KEY_USER_NAME, "Kamu") ?: "Kamu"

        val personalityStr = prefs.getString(KEY_PERSONALITY, CompanionPersonality.SWEET_GIRLFRIEND.name)
        val personality = try {
            CompanionPersonality.valueOf(personalityStr ?: CompanionPersonality.SWEET_GIRLFRIEND.name)
        } catch (_: Exception) {
            CompanionPersonality.SWEET_GIRLFRIEND
        }

        val languageStyleStr = prefs.getString(KEY_LANGUAGE_STYLE, LanguageStyle.MANJA_SAYANG.name)
        val languageStyle = try {
            LanguageStyle.valueOf(languageStyleStr ?: LanguageStyle.MANJA_SAYANG.name)
        } catch (_: Exception) {
            LanguageStyle.MANJA_SAYANG
        }

        val modeStr = prefs.getString(KEY_MODE, ChatMode.STANDARD.name)
        val mode = try {
            ChatMode.valueOf(modeStr ?: ChatMode.STANDARD.name)
        } catch (_: Exception) {
            ChatMode.STANDARD
        }

        val filterLevelStr = prefs.getString(KEY_FILTER_LEVEL, ContentFilterLevel.BALANCED.name)
        val filterLevel = try {
            ContentFilterLevel.valueOf(filterLevelStr ?: ContentFilterLevel.BALANCED.name)
        } catch (_: Exception) {
            ContentFilterLevel.BALANCED
        }

        val customApiKey = prefs.getString(KEY_CUSTOM_API_KEY, "") ?: ""
        val selectedModel = prefs.getString(KEY_MODEL, "gemini-3.5-flash") ?: "gemini-3.5-flash"
        val temperature = prefs.getFloat(KEY_TEMPERATURE, 0.90f)

        return CompanionConfig(
            botName = botName,
            userName = userName,
            personality = personality,
            languageStyle = languageStyle,
            mode = mode,
            filterLevel = filterLevel,
            customApiKey = customApiKey,
            selectedModel = selectedModel,
            temperature = temperature
        )
    }

    fun saveConfig(config: CompanionConfig) {
        prefs.edit().apply {
            putString(KEY_BOT_NAME, config.botName)
            putString(KEY_USER_NAME, config.userName)
            putString(KEY_PERSONALITY, config.personality.name)
            putString(KEY_LANGUAGE_STYLE, config.languageStyle.name)
            putString(KEY_MODE, config.mode.name)
            putString(KEY_FILTER_LEVEL, config.filterLevel.name)
            putString(KEY_CUSTOM_API_KEY, config.customApiKey)
            putString(KEY_MODEL, config.selectedModel)
            putFloat(KEY_TEMPERATURE, config.temperature)
            apply()
        }
    }

    companion object {
        private const val KEY_BOT_NAME = "key_bot_name"
        private const val KEY_USER_NAME = "key_user_name"
        private const val KEY_PERSONALITY = "key_personality"
        private const val KEY_LANGUAGE_STYLE = "key_language_style"
        private const val KEY_MODE = "key_mode"
        private const val KEY_FILTER_LEVEL = "key_filter_level"
        private const val KEY_CUSTOM_API_KEY = "key_custom_api_key"
        private const val KEY_MODEL = "key_model"
        private const val KEY_TEMPERATURE = "key_temperature"
    }
}
