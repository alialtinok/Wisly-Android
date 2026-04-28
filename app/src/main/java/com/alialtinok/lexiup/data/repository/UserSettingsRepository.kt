package com.alialtinok.lexiup.data.repository

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import com.alialtinok.lexiup.data.local.userPreferences
import com.alialtinok.lexiup.data.model.NativeLanguage
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserSettingsRepository(context: Context) {

    private val ds = context.userPreferences

    val nativeLanguage: Flow<NativeLanguage> = ds.data.map { prefs ->
        NativeLanguage.find(prefs[Keys.nativeLanguage].orEmpty())
    }

    val hasCompletedOnboarding: Flow<Boolean> =
        ds.data.map { it[Keys.onboarding] ?: false }

    suspend fun setNativeLanguage(language: NativeLanguage) {
        ds.edit { it[Keys.nativeLanguage] = language.id }
    }

    suspend fun setOnboardingCompleted(done: Boolean) {
        ds.edit { it[Keys.onboarding] = done }
    }

    private object Keys {
        val nativeLanguage = stringPreferencesKey("lexiup.nativeLanguage")
        val onboarding = booleanPreferencesKey("lexiup.onboardingDone")
    }
}
