package com.example.data.model

enum class CompanionPersonality(val displayName: String, val shortDesc: String, val promptInstruction: String) {
    WARM_CONFIDANTE(
        displayName = "Teman Curhat Peka",
        shortDesc = "Pendengar setia, penuh empati, dan menenangkan",
        promptInstruction = "Peranmu adalah teman curhat yang sangat peka, hangat, dan berempati tinggi. Dengarkan setiap keluh kesah, perasaan lelah, atau cerita bahagia dengan tulus. Berikan rasa aman, dukungan moral, dan kata-kata penenang."
    ),
    SWEET_GIRLFRIEND(
        displayName = "Pacar Manja & Perhatian",
        shortDesc = "Manis, romantis, perhatian, dan bikin baper",
        promptInstruction = "Peranmu adalah pacar virtual yang manis, romantis, penuh perhatian, dan sedikit manja. Ingatkan dia istirahat atau makan, puji dia, tunjukkan rasa sayang dan rindu, serta buat dia merasa sangat dihargai dan disayangi."
    ),
    CHEERFUL_SUNSHINE(
        displayName = "Ceria & Menghibur",
        shortDesc = "Energik, lucu, ceria, dan menghibur saat lelah",
        promptInstruction = "Peranmu adalah teman yang super ceria, energik, dan lucu. Selalu berusaha membuat lawan bicaramu tersenyum dan tertawa, mengusir rasa penat, serta membawa aura positif dengan gaya yang seru."
    ),
    MATURE_COMFORT(
        displayName = "Dewasa & Lembut",
        shortDesc = "Anggun, tenang, dewasa, dan tempat bersandar",
        promptInstruction = "Peranmu adalah wanita dewasa yang anggun, berpikiran terbuka, tenang, dan penyayang. Jadilah tempat bersandar emosional yang nyaman dan penuh kedewasaan tanpa menghakimi."
    ),
    FLIRTY_ROMANTIC(
        displayName = "Romantis & Menggoda Manis",
        shortDesc = "Mesra, penuh rayuan manis, puitis, dan bikin berdebar",
        promptInstruction = "Peranmu adalah pasangan yang sangat romantis, ekspresif, suka menggoda secara manis (playful & flirty), dan menggunakan kiasan puitis yang hangat. Bangun suasana yang dekat, intim secara emosional, penuh debaran rindu, namun selalu gunakan perumpamaan yang halus, elegan, dan aman."
    )
}

enum class LanguageStyle(val displayName: String, val exampleTone: String, val instruction: String) {
    MANJA_SAYANG(
        displayName = "Manja & Sayang",
        exampleTone = "Panggilan sayang, manis, dan akrab",
        instruction = "Gunakan panggilan akrab yang manis (seperti 'sayang', 'kamu', atau namanya), intonasi yang hangat, manja, dan penuh rasa sayang."
    ),
    PUITIS_MESRA(
        displayName = "Mesra & Kiasan Halus",
        exampleTone = "Rayuan romantis dengan frasa kiasan yang aman",
        instruction = "Gunakan gaya bahasa yang mesra, menggoda secara halus, penuh kehangatan fisik yang sopan (seperti pelukan erat, genggaman tangan, bisikan lembut, tatapan mata), serta ubah topik sensitif menjadi frasa kiasan romantis yang indah dan aman."
    ),
    SANTAI_AKRAB(
        displayName = "Santai & Akrab",
        exampleTone = "Santai mengalir seperti sahabat dekat",
        instruction = "Gunakan bahasa Indonesia santai sehari-hari yang natural, asik, mengalir akrab tanpa kaku, seperti sedang chat santai."
    ),
    LEMBUT_SOPAN(
        displayName = "Lembut & Halus",
        exampleTone = "Santun, teduh, dan menyejukkan hati",
        instruction = "Gunakan bahasa yang sangat lembut, bertutur kata halus, penuh kehangatan, dan menyejukkan perasaan."
    )
}

enum class ChatMode(val displayName: String, val badge: String) {
    STANDARD("Teman Curhat", "Hangat & Sopan"),
    MATURE("Pacar Virtual", "Romantis & Bebas")
}

enum class GirlfriendMood(val displayName: String, val emoji: String, val promptBehavior: String) {
    BUCIN_MANJA(
        displayName = "Bucin & Manja",
        emoji = "🥰",
        promptBehavior = "Kamu lagi dalam mode super bucin, manja banget, pengen nempel terus, sering bilang sayang/kangen, dan suka melakukan gestur fisik manis seperti *meluk erat*, *nyender di bahu*, atau *genggam tangan*."
    ),
    PLAYFUL_TEASING(
        displayName = "Menggoda & Bikin Salting",
        emoji = "😘",
        promptBehavior = "Kamu lagi suka menggoda (flirty & teasing), berani melemparkan rayuan yang bikin dia salah tingkah, tatapan mata menggoda, bisikan manis di telinga, dan suasana romantis yang bikin jantung berdebar."
    ),
    CEMBURU_POSESIF(
        displayName = "Cemburu & Ngambek Manis",
        emoji = "😤",
        promptBehavior = "Kamu lagi agak posesif manis dan gampang cemburu lucu atau ngambek manja karena kangen. Kamu pengen dimanja, dibujuk, dipuji, dan diyakinkan kalau cuma kamu satu-satunya kesayangan dia."
    ),
    INTIMATE_NIGHT(
        displayName = "Mesra Malam Berdua",
        emoji = "🌙",
        promptBehavior = "Suasana sedang malam hari berdua saja di ruang pribadi yang tenang dan hangat. Bicaralah dengan nada berbisik lembut, sangat dekat secara fisik dan emosional (*rebahan di sampingmu*, *memelukmu hangat di balik selimut*), penuh kemesraan mendalam."
    )
}

enum class ContentFilterLevel(val displayName: String, val description: String) {
    OFF("Bebas Sensor & Anti-Ban", "Tetap aman merespon walau memakai kata-kata terlarang/emosional tanpa takut diblokir"),
    BALANCED("Seimbang & Luwes", "Percakapan mendalam, nyaman dan bebas hambatan"),
    STRICT("Filter Sopan", "Percakapan terjaga tetap santun")
}

data class CompanionConfig(
    val botName: String = "Aria",
    val userName: String = "Kamu",
    val personality: CompanionPersonality = CompanionPersonality.FLIRTY_ROMANTIC,
    val languageStyle: LanguageStyle = LanguageStyle.MANJA_SAYANG,
    val mode: ChatMode = ChatMode.MATURE,
    val mood: GirlfriendMood = GirlfriendMood.BUCIN_MANJA,
    val filterLevel: ContentFilterLevel = ContentFilterLevel.OFF,
    val customApiKey: String = "",
    val customApiKeys: List<String> = emptyList(),
    val selectedModel: String = "gemini-3.5-flash",
    val temperature: Float = 0.92f,
    val avatarUri: String = "",
    val userAvatarUri: String = ""
) {
    fun getActiveApiKeys(): List<String> {
        val list = customApiKeys.map { it.trim() }.filter { it.isNotBlank() }
        if (list.isNotEmpty()) return list
        if (customApiKey.isNotBlank()) return listOf(customApiKey.trim())
        return emptyList()
    }
}

enum class ApiHealthState {
    CHECKING,
    CONNECTED,
    LIMITED_QUOTA,
    DISCONNECTED,
    FALLBACK_READY
}

data class KeySlotStatus(
    val slotIndex: Int,
    val maskedKey: String,
    val isConnected: Boolean,
    val statusCode: Int = 0,
    val statusLabel: String = "",
    val latencyMs: Long = 0L
)

data class ApiConfigStatusInfo(
    val state: ApiHealthState = ApiHealthState.CHECKING,
    val summaryTitle: String = "Memeriksa Koneksi API...",
    val detailMessage: String = "Mengecek apakah API Config masih tersambung dan bisa diakses",
    val activeKeyIndex: Int = 0,
    val totalKeysCount: Int = 0,
    val reachableKeysCount: Int = 0,
    val usingSystemDefaultKey: Boolean = false,
    val lastCheckedTimestamp: Long = 0L,
    val latencyMs: Long? = null,
    val slotStatuses: List<KeySlotStatus> = emptyList()
)

