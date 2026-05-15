package com.alialtinok.wisly.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alialtinok.wisly.WislyApplication
import com.alialtinok.wisly.i18n.LocalAppStrings
import com.alialtinok.wisly.ui.screens.my.components.FavoriteToggleButton
import com.alialtinok.wisly.ui.screens.my.components.SubScreenScaffold
import com.alialtinok.wisly.ui.screens.my.components.WordItemCard
import com.alialtinok.wisly.ui.theme.WislyColors
import kotlinx.coroutines.launch

private val CefrLevels = listOf("A1", "A2", "B1", "B2", "C1")

@Composable
fun WordsListScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as WislyApplication).container }
    val repo = container.wordRepository
    val tts = container.ttsManager
    val s = LocalAppStrings.current
    val scope = rememberCoroutineScope()
    val preferredLevel by container.userSettingsRepository.preferredLevel.collectAsState(initial = null)

    val favoriteIds by repo.favoriteIds.collectAsState(initial = emptySet())

    var selectedLevel by remember(preferredLevel) { mutableStateOf(preferredLevel) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedId by remember { mutableStateOf<Int?>(null) }

    val filtered = remember(selectedLevel, searchQuery) {
        val base = if (selectedLevel == null) repo.allWords
                   else repo.allWords.filter { it.level == selectedLevel }
        val deduped = base.distinctBy { it.word.lowercase() }
        if (searchQuery.isBlank()) deduped
        else deduped.filter {
            it.word.contains(searchQuery, ignoreCase = true) ||
            it.turkish.contains(searchQuery, ignoreCase = true)
        }
    }

    SubScreenScaffold(title = s.contentWords, onBack = onBack) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Search bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    expandedId = null
                },
                placeholder = {
                    Text(s.searchWords, color = WislyColors.OnSurfaceMuted.copy(alpha = 0.6f))
                },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = WislyColors.OnSurfaceMuted)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = null, tint = WislyColors.OnSurfaceMuted)
                        }
                    }
                },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                shape = RoundedCornerShape(12.dp),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.None),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = WislyColors.Primary,
                    unfocusedBorderColor = WislyColors.SurfaceBorder,
                    focusedContainerColor = WislyColors.Surface,
                    unfocusedContainerColor = WislyColors.Surface,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = WislyColors.Primary,
                ),
            )

            // Level pill filter row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                LevelPill(
                    label = s.levelPickerAll,
                    selected = selectedLevel == null,
                    color = WislyColors.Primary,
                    onClick = {
                        selectedLevel = null
                        expandedId = null
                        scope.launch { container.userSettingsRepository.setPreferredLevel(null) }
                    },
                )
                CefrLevels.forEach { level ->
                    LevelPill(
                        label = level,
                        selected = selectedLevel == level,
                        color = colorForLevel(level),
                        onClick = {
                            selectedLevel = level
                            expandedId = null
                            scope.launch { container.userSettingsRepository.setPreferredLevel(level) }
                        },
                    )
                }
            }

            // Result count
            val displayCount = if (searchQuery.isBlank() && selectedLevel == null) repo.allWords.size else filtered.size
            Text(
                text = "$displayCount ${s.wordsShowing}",
                fontSize = 12.sp,
                color = WislyColors.OnSurfaceMuted,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                items(filtered, key = { it.id }) { word ->
                    WordItemCard(
                        word = word,
                        isExpanded = expandedId == word.id,
                        onToggleExpand = {
                            expandedId = if (expandedId == word.id) null else word.id
                        },
                        onSpeak = { tts.speak(word.word) },
                        trailing = {
                            FavoriteToggleButton(
                                isFavorite = word.id in favoriteIds,
                                onToggle = { scope.launch { repo.toggleFavorite(word.id) } },
                            )
                        },
                    )
                }
            }
        }
    }
}

@Composable
private fun LevelPill(label: String, selected: Boolean, color: Color, onClick: () -> Unit) {
    Text(
        text = label,
        fontSize = 13.sp,
        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
        color = if (selected) color else WislyColors.OnSurfaceMuted,
        modifier = Modifier
            .background(
                if (selected) color.copy(alpha = 0.15f) else WislyColors.Surface,
                RoundedCornerShape(20.dp),
            )
            .border(
                1.dp,
                if (selected) color else WislyColors.SurfaceBorder,
                RoundedCornerShape(20.dp),
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    )
}

private fun colorForLevel(level: String): Color = when (level) {
    "A1" -> WislyColors.A1
    "A2" -> WislyColors.A2
    "B1" -> WislyColors.B1
    "B2" -> WislyColors.B2
    "C1" -> WislyColors.C1
    "C2" -> WislyColors.C2
    else -> WislyColors.OnSurfaceMuted
}
