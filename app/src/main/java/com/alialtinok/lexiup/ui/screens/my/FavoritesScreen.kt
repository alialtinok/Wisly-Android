package com.alialtinok.lexiup.ui.screens.my

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
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
import com.alialtinok.lexiup.ui.screens.my.components.FavoriteToggleButton
import com.alialtinok.lexiup.ui.screens.my.components.SubScreenScaffold
import com.alialtinok.lexiup.ui.screens.my.components.WordItemCard
import com.alialtinok.lexiup.ui.theme.LexiColors
import kotlinx.coroutines.launch

@Composable
fun FavoritesScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as WislyApplication).container }
    val repo = container.wordRepository
    val tts = container.ttsManager
    val s = LocalAppStrings.current

    val favoriteIds by repo.favoriteIds.collectAsState(initial = emptySet())
    val scope = rememberCoroutineScope()

    val favorites = remember(favoriteIds) {
        repo.allWords.filter { it.id in favoriteIds }
    }

    var expandedId by remember { mutableStateOf<Int?>(null) }

    SubScreenScaffold(title = s.myFavorites, onBack = onBack) {
        if (favorites.isEmpty()) {
            EmptyState(
                title = s.favoritesEmptyTitle,
                message = s.favoritesEmptyDesc,
                icon = Icons.Filled.Favorite,
                iconTint = LexiColors.C2,
            )
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(favorites, key = { it.id }) { word ->
                    WordItemCard(
                        word = word,
                        isExpanded = expandedId == word.id,
                        onToggleExpand = {
                            expandedId = if (expandedId == word.id) null else word.id
                        },
                        onSpeak = { tts.speak(word.word) },
                        trailing = {
                            FavoriteToggleButton(
                                isFavorite = true,
                                onToggle = { scope.launch { repo.toggleFavorite(word.id) } },
                            )
                        },
                    )
                }
            }
        }
    }
}
