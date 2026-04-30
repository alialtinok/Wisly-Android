package com.alialtinok.lexiup.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import com.alialtinok.lexiup.LexiUpApplication
import com.alialtinok.lexiup.i18n.LocalAppStrings
import com.alialtinok.lexiup.ui.screens.my.components.FavoriteToggleButton
import com.alialtinok.lexiup.ui.screens.my.components.SubScreenScaffold
import com.alialtinok.lexiup.ui.screens.my.components.WordItemCard
import com.alialtinok.lexiup.ui.screens.study.components.LevelOption
import com.alialtinok.lexiup.ui.screens.study.components.LevelPickerContent
import com.alialtinok.lexiup.ui.theme.LexiColors
import kotlinx.coroutines.launch

private val CefrLevels = listOf("A1", "A2", "B1", "B2", "C1")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WordsListScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as LexiUpApplication).container }
    val repo = container.wordRepository
    val tts = container.ttsManager
    val s = LocalAppStrings.current
    val scope = rememberCoroutineScope()

    val favoriteIds by repo.favoriteIds.collectAsState(initial = emptySet())

    var selectedLevel by remember { mutableStateOf<String?>(null) }
    var searchQuery by remember { mutableStateOf("") }
    var expandedId by remember { mutableStateOf<Int?>(null) }
    var showLevelPicker by remember { mutableStateOf(false) }

    val filtered = remember(selectedLevel, searchQuery) {
        val base = if (selectedLevel == null) repo.allWords
                   else repo.allWords.filter { it.level == selectedLevel }
        if (searchQuery.isBlank()) base
        else base.filter {
            it.word.contains(searchQuery, ignoreCase = true) ||
            it.turkish.contains(searchQuery, ignoreCase = true)
        }
    }

    SubScreenScaffold(
        title = s.contentWords,
        onBack = onBack,
        actions = {
            LevelChip(
                level = selectedLevel,
                levelPickerAll = s.levelPickerAll,
                onClick = { showLevelPicker = true },
            )
        },
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            OutlinedTextField(
                value = searchQuery,
                onValueChange = {
                    searchQuery = it
                    expandedId = null
                },
                placeholder = { Text(s.searchWords, color = LexiColors.OnSurfaceMuted.copy(alpha = 0.6f)) },
                leadingIcon = {
                    Icon(Icons.Filled.Search, contentDescription = null, tint = LexiColors.OnSurfaceMuted)
                },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { searchQuery = "" }) {
                            Icon(Icons.Filled.Close, contentDescription = null, tint = LexiColors.OnSurfaceMuted)
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
                    focusedBorderColor = LexiColors.Primary,
                    unfocusedBorderColor = LexiColors.SurfaceBorder,
                    focusedContainerColor = LexiColors.Surface,
                    unfocusedContainerColor = LexiColors.Surface,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White,
                    cursorColor = LexiColors.Primary,
                ),
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

    if (showLevelPicker) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showLevelPicker = false },
            sheetState = sheetState,
            containerColor = LexiColors.Background,
        ) {
            val options = buildList {
                add(
                    LevelOption(
                        id = null,
                        title = s.levelPickerAll,
                        subtitle = "${repo.allWords.size} ${s.countWordsLabel}",
                        color = LexiColors.Primary,
                    ),
                )
                CefrLevels.forEach { level ->
                    val count = repo.allWords.count { it.level == level }
                    add(
                        LevelOption(
                            id = level,
                            title = level,
                            subtitle = "$count ${s.countWordsLabel}",
                            color = colorForLevel(level),
                        ),
                    )
                }
            }
            LevelPickerContent(
                options = options,
                selected = selectedLevel,
                onSelect = { id ->
                    selectedLevel = id
                    showLevelPicker = false
                },
            )
        }
    }
}

@Composable
private fun LevelChip(level: String?, levelPickerAll: String, onClick: () -> Unit) {
    val color = if (level == null) LexiColors.OnSurfaceMuted else colorForLevel(level)
    Row(
        modifier = Modifier
            .clickable(onClick = onClick)
            .background(color.copy(alpha = 0.10f), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.30f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Filled.FilterList,
            contentDescription = "Filter",
            tint = color,
            modifier = Modifier.size(16.dp),
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = level ?: levelPickerAll,
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = color,
        )
    }
}

private fun colorForLevel(level: String): Color = when (level) {
    "A1" -> LexiColors.A1
    "A2" -> LexiColors.A2
    "B1" -> LexiColors.B1
    "B2" -> LexiColors.B2
    "C1" -> LexiColors.C1
    "C2" -> LexiColors.C2
    else -> LexiColors.OnSurfaceMuted
}
