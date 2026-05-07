package com.alialtinok.lexiup.ui.screens.my

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.alialtinok.lexiup.WislyApplication
import com.alialtinok.lexiup.i18n.LocalAppStrings
import com.alialtinok.lexiup.ui.screens.my.components.EmptyState
import com.alialtinok.lexiup.ui.screens.my.components.SubScreenScaffold
import com.alialtinok.lexiup.ui.screens.my.components.TrailingButton
import com.alialtinok.lexiup.ui.screens.my.components.WordItemCard
import com.alialtinok.lexiup.ui.theme.LexiColors
import kotlinx.coroutines.launch

@Composable
fun UnknownWordsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as WislyApplication).container }
    val repo = container.wordRepository
    val tts = container.ttsManager
    val s = LocalAppStrings.current

    val unknownIds by repo.unknownIds.collectAsState(initial = emptySet())
    val scope = rememberCoroutineScope()

    val unknownWords = remember(unknownIds) {
        repo.allWords.filter { it.id in unknownIds }
    }

    var expandedId by remember { mutableStateOf<Int?>(null) }

    SubScreenScaffold(title = s.myUnknown, onBack = onBack) {
        if (unknownWords.isEmpty()) {
            EmptyState(
                title = s.unknownEmptyTitle,
                message = s.unknownEmptyDesc,
                icon = Icons.Filled.ErrorOutline,
                iconTint = LexiColors.B2,
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(unknownWords, key = { it.id }) { word ->
                    WordItemCard(
                        word = word,
                        isExpanded = expandedId == word.id,
                        onToggleExpand = {
                            expandedId = if (expandedId == word.id) null else word.id
                        },
                        onSpeak = { tts.speak(word.word) },
                        trailing = {
                            TrailingButton(
                                icon = Icons.Filled.CheckCircle,
                                tint = LexiColors.AccentGreen,
                                onClick = { scope.launch { repo.markKnown(word.id) } },
                            )
                        },
                    )
                }
            }
        }
    }
}
