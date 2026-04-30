package com.alialtinok.lexiup.ui.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Style
import androidx.compose.ui.graphics.vector.ImageVector

enum class TopLevelRoute(
    val route: String,
    val label: String,
    val icon: ImageVector,
) {
    Home("home", "Home", Icons.Filled.Home),
    Study("study", "Study", Icons.Filled.Style),
    Practice("practice", "Practice", Icons.Filled.Bolt),
    My("my", "My", Icons.Filled.Person),
}

object Routes {
    const val FAVORITES = "my/favorites"
    const val UNKNOWN = "my/unknown"
    const val MY_WORDS = "my/words"
    const val FLASHCARDS = "study/flashcards"
    const val QUIZ = "study/quiz"
    const val FILL_BLANK = "practice/fill-blank"
    const val PHRASAL_QUIZ = "practice/phrasal"
    const val IDIOM_QUIZ = "practice/idiom"
    const val WORDS_LIST = "library/words"
    const val PHRASAL_LIST = "library/phrasal"
    const val IDIOMS_LIST = "library/idioms"
    const val SETTINGS = "my/settings"
}
