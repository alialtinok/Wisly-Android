package com.alialtinok.wisly.tts

import android.content.Context
import android.speech.tts.TextToSpeech
import java.util.Locale

class TtsManager(context: Context) {
    private var tts: TextToSpeech? = null
    private var ready = false

    init {
        tts = TextToSpeech(context.applicationContext) { status ->
            if (status == TextToSpeech.SUCCESS) {
                tts?.language = Locale.US
                ready = true
            }
        }
    }

    fun speak(text: String) {
        if (!ready) return
        tts?.language = Locale.US
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun speakWithLocale(text: String, localeTag: String) {
        if (!ready) return
        tts?.language = Locale.forLanguageTag(localeTag)
        tts?.speak(text, TextToSpeech.QUEUE_FLUSH, null, null)
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        ready = false
    }
}
