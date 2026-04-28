package com.alialtinok.lexiup.data.local

import android.content.Context
import android.util.Log
import com.alialtinok.lexiup.data.model.Idiom
import com.alialtinok.lexiup.data.model.PhrasalVerb
import com.alialtinok.lexiup.data.model.Word
import kotlinx.serialization.json.Json

object ContentLoader {
    private const val TAG = "ContentLoader"

    private val json = Json { ignoreUnknownKeys = true }

    fun loadWords(context: Context): List<Word> = parseAsset(context, "words.json")

    fun loadIdioms(context: Context): List<Idiom> = parseAsset(context, "idioms.json")

    fun loadPhrasalVerbs(context: Context): List<PhrasalVerb> = parseAsset(context, "phrasal_verbs.json")

    private inline fun <reified T> parseAsset(context: Context, fileName: String): List<T> = runCatching {
        context.assets.open(fileName).bufferedReader().use { reader ->
            json.decodeFromString<List<T>>(reader.readText())
        }
    }.getOrElse { error ->
        Log.e(TAG, "Failed to load $fileName", error)
        emptyList()
    }
}
