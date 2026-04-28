package com.alialtinok.lexiup.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alialtinok.lexiup.LexiUpApplication

@Composable
fun HomeScreen() {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as LexiUpApplication).container }
    val repo = container.wordRepository

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("Home", style = MaterialTheme.typography.headlineLarge)
        Text("${repo.allWords.size} words")
        Text("${repo.allPhrasalVerbs.size} phrasal verbs")
        Text("${repo.allIdioms.size} idioms")
    }
}
