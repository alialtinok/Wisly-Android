package com.alialtinok.wisly.data.local

import android.content.Context
import androidx.datastore.preferences.preferencesDataStore

private const val DATASTORE_NAME = "wisly_prefs"

val Context.userPreferences by preferencesDataStore(name = DATASTORE_NAME)
