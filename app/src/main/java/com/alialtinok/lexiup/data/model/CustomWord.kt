package com.alialtinok.lexiup.data.model

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.util.UUID

@Serializable
data class CustomWord(
    val id: String = UUID.randomUUID().toString(),
    val word: String,
    @SerialName("turkish") val translation: String,
    val languageID: String = "tr",
    val example: String = "",
)
