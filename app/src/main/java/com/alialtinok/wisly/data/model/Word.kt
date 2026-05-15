package com.alialtinok.wisly.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Word(
    val id: Int,
    val word: String,
    val type: String,
    val level: String,
    val turkish: String,
    val example: String,
    val exampleTr: String,
) {
    fun bundledTranslation(language: NativeLanguage): String? =
        if (language.id == "tr") turkish else null

    val cefrColor: String
        get() = when (level) {
            "A1" -> "#34D399"
            "A2" -> "#6EE7B7"
            "B1" -> "#60A5FA"
            "B2" -> "#818CF8"
            "C1" -> "#C084FC"
            "C2" -> "#F472B6"
            else -> "#94A3B8"
        }
}
