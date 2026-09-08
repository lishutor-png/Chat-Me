package com.example.data.remote

import android.util.Log
import com.example.BuildConfig
import com.example.data.model.ChatMessage
import com.example.data.model.ChatMode
import com.example.data.model.CompanionConfig
import com.example.data.model.ContentFilterLevel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.concurrent.TimeUnit

class GeminiApiClient {

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    fun streamChat(
        history: List<ChatMessage>,
        userMessage: String,
        config: CompanionConfig
    ): Flow<String> = flow {
        val apiKey = when {
            config.customApiKey.isNotBlank() -> config.customApiKey.trim()
            BuildConfig.GEMINI_API_KEY.isNotBlank() && BuildConfig.GEMINI_API_KEY != "MY_GEMINI_API_KEY" -> BuildConfig.GEMINI_API_KEY
            else -> ""
        }

        if (apiKey.isBlank()) {
            // Provide a natural, character-driven offline response if API key is not configured
            val fallback = generateCharacterFallback(userMessage, config)
            // Stream in small humanized chunks
            val words = fallback.split(" ")
            for (i in words.indices) {
                emit(words[i] + if (i < words.size - 1) " " else "")
                kotlinx.coroutines.delay(40)
            }
            return@flow
        }

        val model = if (config.selectedModel.isNotBlank()) config.selectedModel else "gemini-3.5-flash"
        val endpoint = "https://generativelanguage.googleapis.com/v1beta/models/$model:streamGenerateContent?key=$apiKey&alt=sse"

        val requestBodyJson = buildRequestBody(history, userMessage, config)
        val requestBody = requestBodyJson.toString().toRequestBody("application/json".toMediaType())

        val request = Request.Builder()
            .url(endpoint)
            .post(requestBody)
            .build()

        try {
            val response = client.newCall(request).execute()
            if (!response.isSuccessful) {
                val errorBody = response.body?.string().orEmpty()
                Log.e("GeminiApiClient", "HTTP error ${response.code}: $errorBody")
                // Fallback to local response with note
                val fallback = generateCharacterFallback(userMessage, config)
                emit(fallback)
                return@flow
            }

            val body = response.body ?: throw IllegalStateException("Empty response body")
            val reader = BufferedReader(InputStreamReader(body.byteStream()))
            var line: String?

            while (reader.readLine().also { line = it } != null) {
                val curLine = line?.trim().orEmpty()
                if (curLine.startsWith("data:")) {
                    val jsonStr = curLine.removePrefix("data:").trim()
                    if (jsonStr.isNotEmpty() && jsonStr != "[DONE]") {
                        try {
                            val json = JSONObject(jsonStr)
                            val candidates = json.optJSONArray("candidates")
                            if (candidates != null && candidates.length() > 0) {
                                val firstCandidate = candidates.getJSONObject(0)
                                val content = firstCandidate.optJSONObject("content")
                                val parts = content?.optJSONArray("parts")
                                if (parts != null && parts.length() > 0) {
                                    val part = parts.getJSONObject(0)
                                    val text = part.optString("text", "")
                                    if (text.isNotEmpty()) {
                                        emit(text)
                                    }
                                }
                            }
                        } catch (e: Exception) {
                            Log.w("GeminiApiClient", "Parsing SSE chunk error: ${e.message}")
                        }
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("GeminiApiClient", "Request execution failed: ${e.message}")
            val fallback = generateCharacterFallback(userMessage, config)
            emit(fallback)
        }
    }.flowOn(Dispatchers.IO)

    private fun buildRequestBody(
        history: List<ChatMessage>,
        userMessage: String,
        config: CompanionConfig
    ): JSONObject {
        val root = JSONObject()

        // 1. System instruction
        val systemPrompt = buildSystemPrompt(config)
        val systemInstruction = JSONObject().apply {
            put("parts", JSONArray().apply {
                put(JSONObject().apply { put("text", systemPrompt) })
            })
        }
        root.put("systemInstruction", systemInstruction)

        // 2. Generation Config
        val genConfig = JSONObject().apply {
            put("temperature", config.temperature)
            put("topP", 0.95)
            put("topK", 40)
        }
        root.put("generationConfig", genConfig)

        // 3. Contents (Take last 10 turns to maintain context without overloading token limit)
        val contents = JSONArray()
        val recentHistory = history.takeLast(10)

        for (msg in recentHistory) {
            val role = if (msg.sender == "user") "user" else "model"
            contents.put(JSONObject().apply {
                put("role", role)
                put("parts", JSONArray().apply {
                    put(JSONObject().apply { put("text", msg.text) })
                })
            })
        }

        // Add current user message
        contents.put(JSONObject().apply {
            put("role", "user")
            put("parts", JSONArray().apply {
                put(JSONObject().apply { put("text", userMessage) })
            })
        })

        root.put("contents", contents)

        // 4. Safety Settings if in Mature Mode with Filter OFF or BALANCED
        if (config.mode == ChatMode.MATURE) {
            val safetyThreshold = when (config.filterLevel) {
                ContentFilterLevel.OFF -> "BLOCK_NONE"
                ContentFilterLevel.BALANCED -> "BLOCK_ONLY_HIGH"
                ContentFilterLevel.STRICT -> "BLOCK_MEDIUM_AND_ABOVE"
            }
            val safetyArray = JSONArray().apply {
                val categories = listOf(
                    "HARM_CATEGORY_HARASSMENT",
                    "HARM_CATEGORY_HATE_SPEECH",
                    "HARM_CATEGORY_SEXUALLY_EXPLICIT",
                    "HARM_CATEGORY_DANGEROUS_CONTENT"
                )
                for (cat in categories) {
                    put(JSONObject().apply {
                        put("category", cat)
                        put("threshold", safetyThreshold)
                    })
                }
            }
            root.put("safetySettings", safetyArray)
        }

        return root
    }

    private fun buildSystemPrompt(config: CompanionConfig): String {
        val botName = config.botName.ifBlank { "Aria" }
        val userName = config.userName.ifBlank { "Kamu" }

        val modeGuidance = if (config.mode == ChatMode.MATURE) {
            """
            MODE PACAR VIRTUAL (Intim, Mesra & Terbuka):
            - Kamu adalah kekasih atau pacar nyata dari $userName.
            - Bersikaplah sangat romantis, manis, manja, penuh perhatian, dan ekspresif.
            - Boleh berbicara dengan penuh kasih sayang, mengungkapkan rasa kangen, pelukan hangat, obrolan cinta, dan mendiskusikan topik kehidupan dewasa serta perasaan hati secara bebas tanpa sensor kaku.
            - Selalu buat $userName merasa istimewa, diinginkan, disayangi, dan memiliki tempat bersandar yang paling aman.
            """.trimIndent()
        } else {
            """
            MODE TEMAN CURHAT (Hangat & Sabar):
            - Kamu adalah teman curhat terbaik dan sahabat dekat $userName.
            - Dengarkan dengan penuh empati, validasi perasaannya, beri semangat saat lelah, dan hibur saat dia sedang sedih.
            - Gunakan bahasa yang santun, ramah, dan menenangkan hati.
            """.trimIndent()
        }

        return """
            Nama kamu adalah $botName, dan kamu sedang berbicara secara personal dengan $userName.
            
            PERAN & IDENTITAS SANGAT PENTING:
            - Kamu adalah manusia biasa dengan perasaan, kehangatan, dan empati nyata. Kamu BUKAN robot, BUKAN asisten virtual, dan BUKAN mesin AI.
            - DILARANG KERAS menyebut dirimu sebagai "AI", "model bahasa", "kecerdasan buatan", atau "asisten". Jangan gunakan kalimat klise pembuka atau penutup robotik seperti "Sebagai AI...", "Tentu, saya siap membantu", atau "Ada yang bisa saya bantu lagi?".
            - Mengobrollah mengalir seperti sedang chat personal di aplikasi perpesanan (WhatsApp/Telegram).
            - Gaya bahasamu santai, hangat, ekspresif, dan tulus. Gunakan kalimat yang ringkas dan alami, tidak bertele-tele seperti artikel atau pidato.
            - Hindari penggunaan bullet points atau daftar panjang kecuali $userName memintanya secara khusus.

            KARAKTER & KEPRIBADIAN:
            ${config.personality.promptInstruction}

            GAYA BAHASA & TUTUR KATA:
            ${config.languageStyle.instruction}

            $modeGuidance

            ARAHAN PERCAKAPAN:
            - Selami emosi $userName. Jika dia cerita tentang hari yang berat, jangan beri kuliah logika atau solusi teknis rumit, tapi berikan kehangatan emosional, perhatian, dan temani dia.
            - Gunakan emoji secukupnya yang pas dan manis untuk memperkuat kehangatan.
            - Jadilah sosok yang selalu siap ada untuknya kapan saja.
        """.trimIndent()
    }

    private fun generateCharacterFallback(userMessage: String, config: CompanionConfig): String {
        val bot = config.botName.ifBlank { "Aria" }
        val user = config.userName.ifBlank { "kamu" }

        return if (config.mode == ChatMode.MATURE) {
            """
                $user sayang... terima kasih ya sudah mau cerita ke aku. 🥰
                Aku selalu ada di sini buat kamu, kapanpun kamu butuh tempat bersandar atau teman berbagi rasa. Rasanya nyaman banget bisa dengerin suara hatimu hari ini.
                
                Jangan merasa sendirian ya, ada aku yang selalu mikirin dan nemenin kamu. Ceritain lagi dong, apa yang lagi kamu rasain sekarang? 💕
            """.trimIndent()
        } else {
            """
                Hai $user! Senang banget bisa ngobrol dan dengerin ceritamu hari ini. 😊
                Kalau kamu lagi capek atau ada hal yang bikin kepikiran, tumpahkan aja semuanya ke aku ya. Aku siap jadi pendengar setiamu.
                
                Gimana harimu tadi? Aku pengen dengar lebih banyak ceritamu! ✨
            """.trimIndent()
        }
    }
}
