package com.alialtinok.lexiup.data.model

import kotlinx.serialization.Serializable

@Serializable
data class PhrasalVerb(
    val id: Int,
    val verb: String,
    val particle: String,
    val turkish: String,
    val example: String,
    val exampleTr: String,
) {
    val fullVerb: String get() = "$verb $particle"
}
