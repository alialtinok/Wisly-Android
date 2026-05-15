package com.alialtinok.wisly.data.repository

import android.content.Context
import android.net.Uri
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.alialtinok.wisly.data.local.userPreferences
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.MapSerializer
import kotlinx.serialization.builtins.serializer
import kotlinx.serialization.json.Json
import java.net.HttpURLConnection
import java.net.URL

class TranslationRepository(context: Context) {

    private val ds = context.userPreferences
    private val json = Json { ignoreUnknownKeys = true }
    private val mapSerializer = MapSerializer(String.serializer(), String.serializer())

    suspend fun cached(word: String, languageId: String): String? =
        readCache()[cacheKey(word, languageId)]

    suspend fun fetch(word: String, languageId: String): String? {
        cached(word, languageId)?.let { return it }

        val translated = withContext(Dispatchers.IO) { callMyMemory(word, languageId) } ?: return null
        if (!isValid(translated, word)) return null

        val key = cacheKey(word, languageId)
        ds.edit { prefs ->
            val current = decodeCache(prefs[Keys.cache])
            prefs[Keys.cache] = json.encodeToString(mapSerializer, current + (key to translated))
        }
        return translated
    }

    private fun callMyMemory(word: String, languageId: String): String? {
        val url = Uri.parse("https://api.mymemory.translated.net/get").buildUpon()
            .appendQueryParameter("q", word)
            .appendQueryParameter("langpair", "en|$languageId")
            .appendQueryParameter("de", "info.alialtinok@gmail.com")
            .build()
            .toString()

        return runCatching {
            val conn = (URL(url).openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                connectTimeout = 10_000
                readTimeout = 10_000
            }
            val body = conn.inputStream.bufferedReader().use { it.readText() }
            val parsed = json.decodeFromString(MyMemoryResponse.serializer(), body)
            parsed.responseData.translatedText.trim().let { Uri.decode(it) ?: it }
        }.getOrNull()
    }

    private fun isValid(translated: String, original: String): Boolean {
        if (translated.isEmpty()) return false
        if (translated.equals(original, ignoreCase = true)) return false
        if (translated.contains("???")) return false
        if (translated.startsWith("MYMEMORY WARNING")) return false
        if (translated.contains("%")) return false
        if (translated.none { it.isLetterOrDigit() }) return false
        return true
    }

    private suspend fun readCache(): Map<String, String> =
        decodeCache(ds.data.first()[Keys.cache])

    private fun decodeCache(raw: String?): Map<String, String> = raw
        ?.let { runCatching { json.decodeFromString(mapSerializer, it) }.getOrDefault(emptyMap()) }
        ?: emptyMap()

    private fun cacheKey(word: String, languageId: String) = "$languageId|${word.lowercase()}"

    @Serializable
    private data class MyMemoryResponse(val responseData: ResponseData) {
        @Serializable
        data class ResponseData(val translatedText: String)
    }

    private object Keys {
        val cache = stringPreferencesKey("wisly.translationCache")
    }
}
