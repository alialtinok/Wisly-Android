package com.alialtinok.wisly

import android.app.Application
import com.alialtinok.wisly.data.repository.TranslationRepository
import com.alialtinok.wisly.data.repository.UserSettingsRepository
import com.alialtinok.wisly.data.repository.WordRepository
import com.alialtinok.wisly.tts.TtsManager

class WislyApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}

class AppContainer(application: Application) {
    val wordRepository = WordRepository(application)
    val userSettingsRepository = UserSettingsRepository(application)
    val translationRepository = TranslationRepository(application)
    val ttsManager = TtsManager(application)
}
