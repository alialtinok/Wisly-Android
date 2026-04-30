package com.alialtinok.lexiup.i18n

import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import com.alialtinok.lexiup.data.model.NativeLanguage

data class AppStrings(
    val tabHome: String,
    val tabStudy: String,
    val tabPractice: String,
    val tabMy: String,

    val wordOfTheDay: String,
    val dayStreak: String,
    val statFavorites: String,
    val statToReview: String,
    val sectionContent: String,
    val contentWords: String,
    val contentPhrasal: String,
    val contentIdioms: String,
    val unitWords: String,
    val unitVerbs: String,
    val unitIdioms: String,

    val studyTitle: String,
    val studyFlashcards: String,
    val studyFlashcardsDesc: String,
    val studyQuiz: String,
    val studyQuizDesc: String,

    val practiceTitle: String,
    val practiceFillBlank: String,
    val practiceFillBlankDesc: String,
    val practicePhrasal: String,
    val practicePhrasalDesc: String,
    val practiceIdioms: String,
    val practiceIdiomsDesc: String,

    val myTitle: String,
    val myFavorites: String,
    val myFavoritesDesc: String,
    val myUnknown: String,
    val myUnknownDesc: String,
    val myWords: String,
    val myWordsDesc: String,
    val mySettings: String,
    val mySettingsDesc: String,

    val settingsTitle: String,
    val settingsLanguageSection: String,
    val settingsLanguageDesc: String,
    val settingsAboutSection: String,
    val settingsVersion: String,

    val back: String,
    val close: String,
) {
    companion object {
        val EN = AppStrings(
            tabHome = "Home",
            tabStudy = "Study",
            tabPractice = "Practice",
            tabMy = "My",

            wordOfTheDay = "WORD OF THE DAY",
            dayStreak = "day streak",
            statFavorites = "favorites",
            statToReview = "to review",
            sectionContent = "CONTENT",
            contentWords = "Words",
            contentPhrasal = "Phrasal Verbs",
            contentIdioms = "Idioms",
            unitWords = "words",
            unitVerbs = "verbs",
            unitIdioms = "idioms",

            studyTitle = "Study",
            studyFlashcards = "Flashcards",
            studyFlashcardsDesc = "Flip cards to memorize",
            studyQuiz = "Quiz",
            studyQuizDesc = "Multiple choice practice",

            practiceTitle = "Practice",
            practiceFillBlank = "Fill in the Blank",
            practiceFillBlankDesc = "Complete the sentence",
            practicePhrasal = "Phrasal Verbs",
            practicePhrasalDesc = "Match meaning to phrasal verb",
            practiceIdioms = "Idioms",
            practiceIdiomsDesc = "Match meaning to idiom",

            myTitle = "My",
            myFavorites = "Favorites",
            myFavoritesDesc = "Your saved words",
            myUnknown = "To Review",
            myUnknownDesc = "Words marked unknown",
            myWords = "My Words",
            myWordsDesc = "Words you added",
            mySettings = "Settings",
            mySettingsDesc = "Language and preferences",

            settingsTitle = "Settings",
            settingsLanguageSection = "LANGUAGE",
            settingsLanguageDesc = "Your native language for translations",
            settingsAboutSection = "ABOUT",
            settingsVersion = "Version",

            back = "Back",
            close = "Close",
        )

        val TR = AppStrings(
            tabHome = "Anasayfa",
            tabStudy = "Çalış",
            tabPractice = "Pratik",
            tabMy = "Profilim",

            wordOfTheDay = "GÜNÜN KELİMESİ",
            dayStreak = "günlük seri",
            statFavorites = "favori",
            statToReview = "tekrar",
            sectionContent = "İÇERİK",
            contentWords = "Kelimeler",
            contentPhrasal = "Phrasal Verb'ler",
            contentIdioms = "Deyimler",
            unitWords = "kelime",
            unitVerbs = "phrasal",
            unitIdioms = "deyim",

            studyTitle = "Çalış",
            studyFlashcards = "Kartlar",
            studyFlashcardsDesc = "Kartları çevir, ezberle",
            studyQuiz = "Quiz",
            studyQuizDesc = "Çoktan seçmeli pratik",

            practiceTitle = "Pratik",
            practiceFillBlank = "Boşluk Doldurma",
            practiceFillBlankDesc = "Cümleyi tamamla",
            practicePhrasal = "Phrasal Verb'ler",
            practicePhrasalDesc = "Anlamı phrasal verb ile eşleştir",
            practiceIdioms = "Deyimler",
            practiceIdiomsDesc = "Anlamı deyim ile eşleştir",

            myTitle = "Profilim",
            myFavorites = "Favoriler",
            myFavoritesDesc = "Kaydettiğin kelimeler",
            myUnknown = "Tekrar Edilecekler",
            myUnknownDesc = "Bilmediğin kelimeler",
            myWords = "Kelimelerim",
            myWordsDesc = "Eklediğin kelimeler",
            mySettings = "Ayarlar",
            mySettingsDesc = "Dil ve tercihler",

            settingsTitle = "Ayarlar",
            settingsLanguageSection = "DİL",
            settingsLanguageDesc = "Çeviri için ana dilin",
            settingsAboutSection = "HAKKINDA",
            settingsVersion = "Sürüm",

            back = "Geri",
            close = "Kapat",
        )

        fun forLanguage(language: NativeLanguage): AppStrings =
            if (language.id == "tr") TR else EN
    }
}

val LocalAppStrings = staticCompositionLocalOf { AppStrings.EN }

@Composable
fun ProvideAppStrings(language: NativeLanguage, content: @Composable () -> Unit) {
    val strings = AppStrings.forLanguage(language)
    CompositionLocalProvider(LocalAppStrings provides strings, content = content)
}
