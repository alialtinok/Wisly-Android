package com.alialtinok.lexiup.data.model

import kotlinx.serialization.Serializable

@Serializable
data class NativeLanguage(
    val id: String,
    val name: String,
    val nameInEnglish: String,
    val flag: String,
    val translationCode: String,
) {
    val speechCode: String
        get() = when (id) {
            "tr" -> "tr-TR"
            "fr" -> "fr-FR"
            "ru" -> "ru-RU"
            "de" -> "de-DE"
            "ar" -> "ar-SA"
            "es" -> "es-ES"
            else -> "$id-${id.uppercase()}"
        }

    companion object {
        val all: List<NativeLanguage> = listOf(
            NativeLanguage("tr", "Türkçe", "Turkish", "🇹🇷", "tr"),
            NativeLanguage("fr", "Français", "French", "🇫🇷", "fr"),
            NativeLanguage("ru", "Русский", "Russian", "🇷🇺", "ru"),
            NativeLanguage("de", "Deutsch", "German", "🇩🇪", "de"),
            NativeLanguage("ar", "العربية", "Arabic", "🇸🇦", "ar"),
            NativeLanguage("es", "Español", "Spanish", "🇪🇸", "es"),
        )

        val Default: NativeLanguage = all.first()

        fun find(id: String): NativeLanguage = all.firstOrNull { it.id == id } ?: Default
    }
}
