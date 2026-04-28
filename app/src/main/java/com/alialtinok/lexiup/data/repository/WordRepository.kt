package com.alialtinok.lexiup.data.repository

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.alialtinok.lexiup.data.local.ContentLoader
import com.alialtinok.lexiup.data.local.userPreferences
import com.alialtinok.lexiup.data.model.CustomWord
import com.alialtinok.lexiup.data.model.Idiom
import com.alialtinok.lexiup.data.model.PhrasalVerb
import com.alialtinok.lexiup.data.model.Word
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import java.util.Calendar

class WordRepository(private val context: Context) {

    val allWords: List<Word> by lazy { ContentLoader.loadWords(context) }
    val allIdioms: List<Idiom> by lazy { ContentLoader.loadIdioms(context) }
    val allPhrasalVerbs: List<PhrasalVerb> by lazy { ContentLoader.loadPhrasalVerbs(context) }

    private val ds = context.userPreferences
    private val json = Json { ignoreUnknownKeys = true }
    private val customWordListSerializer = ListSerializer(CustomWord.serializer())

    val favoriteIds: Flow<Set<Int>> = idSetFlow(Keys.favorites)
    val unknownIds: Flow<Set<Int>> = idSetFlow(Keys.unknowns)
    val unknownPhrasalIds: Flow<Set<Int>> = idSetFlow(Keys.unknownPhrasals)
    val unknownIdiomIds: Flow<Set<Int>> = idSetFlow(Keys.unknownIdioms)
    val currentStreak: Flow<Int> = ds.data.map { it[Keys.streak] ?: 0 }
    val myWords: Flow<List<CustomWord>> = ds.data.map { decodeMyWords(it[Keys.myWords]) }

    val wordOfTheDay: Word?
        get() = allWords.takeIf { it.isNotEmpty() }
            ?.let { it[Calendar.getInstance().get(Calendar.DAY_OF_YEAR) % it.size] }

    suspend fun toggleFavorite(id: Int) =
        updateIdSet(Keys.favorites) { if (id in it) it - id else it + id }

    suspend fun markUnknown(id: Int) = updateIdSet(Keys.unknowns) { it + id }
    suspend fun markKnown(id: Int) = updateIdSet(Keys.unknowns) { it - id }
    suspend fun markUnknownPhrasal(id: Int) = updateIdSet(Keys.unknownPhrasals) { it + id }
    suspend fun markKnownPhrasal(id: Int) = updateIdSet(Keys.unknownPhrasals) { it - id }
    suspend fun markUnknownIdiom(id: Int) = updateIdSet(Keys.unknownIdioms) { it + id }
    suspend fun markKnownIdiom(id: Int) = updateIdSet(Keys.unknownIdioms) { it - id }

    suspend fun addMyWord(word: CustomWord) {
        ds.edit { prefs ->
            val current = decodeMyWords(prefs[Keys.myWords])
            prefs[Keys.myWords] = encodeMyWords(listOf(word) + current)
        }
    }

    suspend fun removeMyWord(word: CustomWord) {
        ds.edit { prefs ->
            val current = decodeMyWords(prefs[Keys.myWords])
            prefs[Keys.myWords] = encodeMyWords(current.filterNot { it.id == word.id })
        }
    }

    suspend fun recordActivity() {
        ds.edit { prefs ->
            val today = startOfDayMillis(System.currentTimeMillis())
            val last = prefs[Keys.lastActive]?.let(::startOfDayMillis)
            val streak = prefs[Keys.streak] ?: 0
            val newStreak = when {
                last == null -> 1
                last == today -> return@edit
                (today - last) / DAY_MILLIS == 1L -> streak + 1
                else -> 1
            }
            prefs[Keys.streak] = newStreak
            prefs[Keys.lastActive] = today
        }
    }

    private fun idSetFlow(key: Preferences.Key<Set<String>>): Flow<Set<Int>> =
        ds.data.map { prefs ->
            prefs[key].orEmpty().mapNotNullTo(mutableSetOf()) { it.toIntOrNull() }
        }

    private suspend fun updateIdSet(
        key: Preferences.Key<Set<String>>,
        transform: (Set<Int>) -> Set<Int>,
    ) {
        ds.edit { prefs ->
            val current = prefs[key].orEmpty().mapNotNullTo(mutableSetOf()) { it.toIntOrNull() }
            prefs[key] = transform(current).map { it.toString() }.toSet()
        }
    }

    private fun decodeMyWords(raw: String?): List<CustomWord> = raw
        ?.let { runCatching { json.decodeFromString(customWordListSerializer, it) }.getOrDefault(emptyList()) }
        ?: emptyList()

    private fun encodeMyWords(words: List<CustomWord>): String =
        json.encodeToString(customWordListSerializer, words)

    private fun startOfDayMillis(millis: Long): Long = Calendar.getInstance().apply {
        timeInMillis = millis
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis

    private object Keys {
        val favorites = stringSetPreferencesKey("favoriteIDs")
        val unknowns = stringSetPreferencesKey("unknownIDs")
        val unknownPhrasals = stringSetPreferencesKey("unknownPhrasalVerbIDs")
        val unknownIdioms = stringSetPreferencesKey("unknownIdiomIDs")
        val myWords = stringPreferencesKey("myWords")
        val streak = intPreferencesKey("currentStreak")
        val lastActive = longPreferencesKey("lastActiveDate")
    }

    private companion object {
        const val DAY_MILLIS = 1000L * 60 * 60 * 24
    }
}
