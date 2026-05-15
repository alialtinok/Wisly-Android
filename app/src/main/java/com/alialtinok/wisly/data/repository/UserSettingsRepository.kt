package com.alialtinok.wisly.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.alialtinok.wisly.data.local.userPreferences
import com.alialtinok.wisly.data.model.NativeLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserSettingsRepository(context: Context) {

    private val ds = context.userPreferences

    val nativeLanguage: Flow<NativeLanguage?> = ds.data.map { prefs ->
        val id = prefs[Keys.nativeLanguage]
        if (id.isNullOrEmpty()) null else NativeLanguage.find(id)
    }

    val hasCompletedOnboarding: Flow<Boolean> =
        ds.data.map { it[Keys.onboarding] ?: false }

    val preferredLevel: Flow<String?> = ds.data.map { prefs ->
        prefs[Keys.preferredLevel]?.takeIf { it.isNotBlank() }
    }

    suspend fun setNativeLanguage(language: NativeLanguage) {
        ds.edit { it[Keys.nativeLanguage] = language.id }
    }

    suspend fun setPreferredLevel(level: String?) {
        ds.edit {
            if (level.isNullOrBlank()) it.remove(Keys.preferredLevel)
            else it[Keys.preferredLevel] = level
        }
    }

    suspend fun setOnboardingCompleted(done: Boolean) {
        ds.edit { it[Keys.onboarding] = done }
    }

    private object Keys {
        val nativeLanguage = stringPreferencesKey("wisly.nativeLanguage")
        val onboarding = booleanPreferencesKey("wisly.onboardingDone")
        val preferredLevel = stringPreferencesKey("wisly.preferredLevel")
    }
}
