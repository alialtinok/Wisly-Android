package com.alialtinok.lexiup.data.model

import kotlinx.serialization.Serializable

@Serializable
data class Idiom(
    val id: Int,
    val idiom: String,
    val turkish: String,
    val example: String,
    val exampleTr: String,
)
