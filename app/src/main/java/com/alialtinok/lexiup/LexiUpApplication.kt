package com.alialtinok.lexiup

import android.app.Application
import com.alialtinok.lexiup.data.repository.TranslationRepository
import com.alialtinok.lexiup.data.repository.UserSettingsRepository
import com.alialtinok.lexiup.data.repository.WordRepository
import com.alialtinok.lexiup.tts.TtsManager

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
