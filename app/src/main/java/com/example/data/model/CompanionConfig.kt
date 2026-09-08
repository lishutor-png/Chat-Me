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
    )
}

enum class LanguageStyle(val displayName: String, val exampleTone: String, val instruction: String) {
    MANJA_SAYANG(
        displayName = "Manja & Sayang",
        exampleTone = "Panggilan sayang, manis, dan akrab",
        instruction = "Gunakan panggilan akrab yang manis (seperti 'sayang', 'kamu', atau namanya), intonasi yang hangat, manja, dan penuh rasa sayang."
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

enum class ContentFilterLevel(val displayName: String, val description: String) {
    OFF("Bebas Sensor & Anti-Ban", "Tetap aman merespon walau memakai kata-kata terlarang/emosional tanpa takut diblokir"),
    BALANCED("Seimbang & Luwes", "Percakapan mendalam, nyaman dan bebas hambatan"),
    STRICT("Filter Sopan", "Percakapan terjaga tetap santun")
}

data class CompanionConfig(
    val botName: String = "Aria",
    val userName: String = "Kamu",
    val personality: CompanionPersonality = CompanionPersonality.SWEET_GIRLFRIEND,
    val languageStyle: LanguageStyle = LanguageStyle.MANJA_SAYANG,
    val mode: ChatMode = ChatMode.STANDARD,
    val filterLevel: ContentFilterLevel = ContentFilterLevel.OFF,
    val customApiKey: String = "",
    val selectedModel: String = "gemini-3.5-flash",
    val temperature: Float = 0.85f
)
