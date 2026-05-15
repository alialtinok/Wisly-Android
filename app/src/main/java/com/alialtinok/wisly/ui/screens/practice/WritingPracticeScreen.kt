package com.alialtinok.wisly.ui.screens.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alialtinok.wisly.WislyApplication
import com.alialtinok.wisly.data.model.Word
import com.alialtinok.wisly.i18n.LocalAppStrings
import com.alialtinok.wisly.ui.screens.practice.components.QuizScoreRow
import com.alialtinok.wisly.ui.screens.study.components.LevelOption
import com.alialtinok.wisly.ui.screens.study.components.LevelPickerContent
import com.alialtinok.wisly.ui.theme.WislyColors

private val WritingLevels = listOf("A1", "A2", "B1", "B2", "C1")

private data class LetterTile(
    val id: Int,
    val letter: Char,
    val isPlaced: Boolean = false,
    val isHint: Boolean = false,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun WritingPracticeScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as WislyApplication).container }
    val repo = container.wordRepository
    val translationRepo = container.translationRepository
    val nativeLanguage by container.userSettingsRepository.nativeLanguage.collectAsState(initial = null)
    val s = LocalAppStrings.current

    var selectedLevel by remember { mutableStateOf<String?>(null) }
    var showLevelPicker by remember { mutableStateOf(false) }
    var currentWord by remember { mutableStateOf<Word?>(null) }
    var translation by remember { mutableStateOf<String?>(null) }
    var tiles by remember { mutableStateOf<List<LetterTile>>(emptyList()) }
    var slots by remember { mutableStateOf<List<Int?>>(emptyList()) }
    var isChecked by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }
    var correct by remember { mutableIntStateOf(0) }
    var wrong by remember { mutableIntStateOf(0) }
    var hintsUsed by remember { mutableIntStateOf(0) }

    val pool = remember(selectedLevel) {
        val words = if (selectedLevel == null) repo.allWords else repo.allWords.filter { it.level == selectedLevel }
        words.distinctBy { it.word.lowercase() }.filter { it.word.any(Char::isLetter) }
    }

    fun answerFor(word: Word): String = word.word.trim()

    fun buildTiles(word: String): Pair<List<LetterTile>, List<Int?>> {
        val letters = word.toList()
        var shuffled = letters.shuffled()
        while (word.length > 1 && shuffled == letters) shuffled = letters.shuffled()
        return shuffled.mapIndexed { index, letter -> LetterTile(index, letter) } to List(letters.size) { null }
    }

    fun nextWord() {
        val word = pool.randomOrNull()
        currentWord = word
        isChecked = false
        isCorrect = false
        hintsUsed = 0
        translation = null
        if (word == null) {
            tiles = emptyList()
            slots = emptyList()
        } else {
            val built = buildTiles(answerFor(word))
            tiles = built.first
            slots = built.second
        }
    }

    fun currentAnswer(nextSlots: List<Int?> = slots): String =
        nextSlots.joinToString("") { id ->
            id?.let { tileId -> tiles.firstOrNull { it.id == tileId }?.letter?.toString() }.orEmpty()
        }

    fun checkAnswer(nextSlots: List<Int?> = slots) {
        val word = currentWord ?: return
        val matches = currentAnswer(nextSlots).equals(answerFor(word), ignoreCase = true)
        isCorrect = matches
        isChecked = true
        if (matches) correct++ else wrong++
    }

    fun clearSlots() {
        tiles = tiles.map { it.copy(isPlaced = false, isHint = false) }
        slots = List(slots.size) { null }
        hintsUsed = 0
        isChecked = false
    }

    fun useHint() {
        val word = currentWord ?: return
        val slotIndex = slots.indexOfFirst { it == null }
        if (slotIndex == -1) return
        val expected = answerFor(word).getOrNull(slotIndex) ?: return
        val tile = tiles.firstOrNull { !it.isPlaced && it.letter.equals(expected, ignoreCase = true) } ?: return
        val nextSlots = slots.toMutableList().also { it[slotIndex] = tile.id }
        slots = nextSlots
        tiles = tiles.map { if (it.id == tile.id) it.copy(isPlaced = true, isHint = true) else it }
        hintsUsed++
        if (nextSlots.none { it == null }) checkAnswer(nextSlots)
    }

    LaunchedEffect(pool) {
        if (currentWord == null || currentWord !in pool) nextWord()
    }

    LaunchedEffect(currentWord, nativeLanguage) {
        val word = currentWord ?: return@LaunchedEffect
        val lang = nativeLanguage
        translation = if (lang == null || lang.id == "tr") {
            word.turkish
        } else {
            translationRepo.fetch(word.word, lang.translationCode) ?: word.turkish
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(WislyColors.Background)
            .verticalScroll(rememberScrollState()),
    ) {
        WritingTopBar(
            onBack = onBack,
            selectedLevel = selectedLevel,
            onOpenLevelPicker = { showLevelPicker = true },
        )

        QuizScoreRow(
            correct = correct,
            wrong = wrong,
            qNumber = correct + wrong,
            sessionSize = 20,
            accent = WislyColors.Primary,
            modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
        )

        val word = currentWord
        if (word == null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(280.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(text = s.writingNeed, color = WislyColors.OnSurfaceMuted)
            }
        } else {
            WritingPromptCard(
                word = word,
                translation = translation,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )

            AnswerSlots(
                slots = slots,
                tiles = tiles,
                isChecked = isChecked,
                isCorrect = isCorrect,
                onRemove = { id ->
                    if (isChecked) return@AnswerSlots
                    slots = slots.map { if (it == id) null else it }
                    tiles = tiles.map { if (it.id == id) it.copy(isPlaced = false, isHint = false) else it }
                },
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
            )

            if (isChecked) {
                ResultBanner(
                    isCorrect = isCorrect,
                    answer = answerFor(word),
                    hintsUsed = hintsUsed,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                )
            } else {
                LetterTiles(
                    tiles = tiles.filterNot { it.isPlaced },
                    nextExpected = answerFor(word).getOrNull(slots.indexOfFirst { it == null }),
                    onTileClick = { tile ->
                        val slotIndex = slots.indexOfFirst { it == null }
                        if (slotIndex == -1) return@LetterTiles
                        val expected = answerFor(word).getOrNull(slotIndex)
                        if (expected != null && !tile.letter.equals(expected, ignoreCase = true)) return@LetterTiles
                        val nextSlots = slots.toMutableList().also { it[slotIndex] = tile.id }
                        slots = nextSlots
                        tiles = tiles.map { if (it.id == tile.id) it.copy(isPlaced = true) else it }
                        if (nextSlots.none { it == null }) checkAnswer(nextSlots)
                    },
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                )
            }

            Row(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(
                    onClick = { useHint() },
                    enabled = !isChecked && slots.any { it == null },
                    colors = ButtonDefaults.textButtonColors(contentColor = WislyColors.AccentAmber),
                    modifier = Modifier
                        .height(52.dp)
                        .border(1.dp, WislyColors.SurfaceBorder, RoundedCornerShape(14.dp)),
                ) {
                    Icon(Icons.Filled.Lightbulb, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(if (hintsUsed > 0) "${s.writingHint} $hintsUsed" else s.writingHint)
                }

                TextButton(
                    onClick = { clearSlots() },
                    enabled = !isChecked,
                    colors = ButtonDefaults.textButtonColors(contentColor = WislyColors.OnSurfaceMuted),
                    modifier = Modifier
                        .height(52.dp)
                        .border(1.dp, WislyColors.SurfaceBorder, RoundedCornerShape(14.dp)),
                ) {
                    Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(Modifier.width(6.dp))
                    Text(s.writingClear)
                }

                Button(
                    onClick = { if (isChecked) nextWord() else checkAnswer() },
                    colors = ButtonDefaults.buttonColors(containerColor = WislyColors.Primary),
                    modifier = Modifier
                        .weight(1f)
                        .height(52.dp),
                ) {
                    Text(if (isChecked) s.reviewNextWord else s.writingCheck, fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showLevelPicker) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showLevelPicker = false },
            sheetState = sheetState,
            containerColor = WislyColors.Background,
        ) {
            val options = buildList {
                add(LevelOption(null, s.levelPickerAll, "${repo.allWords.size} ${s.countWordsLabel}", WislyColors.Primary))
                WritingLevels.forEach { level ->
                    add(LevelOption(level, level, "${repo.allWords.count { it.level == level }} ${s.countWordsLabel}", colorForWritingLevel(level)))
                }
            }
            LevelPickerContent(
                options = options,
                selected = selectedLevel,
                onSelect = { id ->
                    selectedLevel = id
                    showLevelPicker = false
                    currentWord = null
                },
            )
        }
    }
}

@Composable
private fun WritingTopBar(
    onBack: () -> Unit,
    selectedLevel: String?,
    onOpenLevelPicker: () -> Unit,
) {
    val s = LocalAppStrings.current
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 4.dp, vertical = 4.dp)
            .height(52.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = s.back, tint = Color.White)
        }
        Text(
            text = s.writingTitle,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.weight(1f),
        )
        Row(
            modifier = Modifier
                .clickable(onClick = onOpenLevelPicker)
                .padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.Filled.FilterList,
                contentDescription = s.filterAction,
                tint = if (selectedLevel == null) WislyColors.OnSurfaceMuted else colorForWritingLevel(selectedLevel),
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = selectedLevel ?: s.levelPickerAll,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selectedLevel == null) WislyColors.OnSurfaceMuted else colorForWritingLevel(selectedLevel),
            )
        }
    }
}

@Composable
private fun WritingPromptCard(word: Word, translation: String?, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(WislyColors.Surface, RoundedCornerShape(20.dp))
            .border(1.dp, WislyColors.SurfaceBorder, RoundedCornerShape(20.dp))
            .padding(horizontal = 18.dp, vertical = 22.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = LocalAppStrings.current.writingPrompt,
            fontSize = 12.sp,
            color = WislyColors.OnSurfaceMuted,
        )
        Text(
            text = translation ?: "...",
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            color = WislyColors.Primary,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp,
        )
        Text(
            text = "${word.type} · ${word.level}",
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            color = WislyColors.OnSurfaceMuted,
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AnswerSlots(
    slots: List<Int?>,
    tiles: List<LetterTile>,
    isChecked: Boolean,
    isCorrect: Boolean,
    onRemove: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    val borderColor = when {
        !isChecked -> WislyColors.Primary
        isCorrect -> WislyColors.AccentGreen
        else -> WislyColors.AccentRed
    }
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        slots.forEach { id ->
            val tile = id?.let { tileId -> tiles.firstOrNull { it.id == tileId } }
            Box(
                modifier = Modifier
                    .size(38.dp)
                    .background(WislyColors.Surface, RoundedCornerShape(10.dp))
                    .border(1.5.dp, if (tile == null) WislyColors.SurfaceBorder else borderColor.copy(alpha = 0.65f), RoundedCornerShape(10.dp))
                    .clickable(enabled = tile != null && !isChecked) { onRemove(id!!) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tile?.letter?.uppercaseChar()?.toString().orEmpty(),
                    fontSize = 19.sp,
                    fontWeight = FontWeight.Black,
                    color = if (tile?.isHint == true) WislyColors.AccentAmber else Color.White,
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun LetterTiles(
    tiles: List<LetterTile>,
    nextExpected: Char?,
    onTileClick: (LetterTile) -> Unit,
    modifier: Modifier = Modifier,
) {
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        tiles.forEach { tile ->
            val isAvailable = nextExpected == null || tile.letter.equals(nextExpected, ignoreCase = true)
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .background(
                        if (isAvailable) Color(0xFF1E2235) else WislyColors.Surface,
                        RoundedCornerShape(12.dp),
                    )
                    .border(
                        1.5.dp,
                        if (isAvailable) WislyColors.Primary.copy(alpha = 0.45f) else WislyColors.SurfaceBorder,
                        RoundedCornerShape(12.dp),
                    )
                    .clickable(enabled = isAvailable) { onTileClick(tile) },
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = tile.letter.uppercaseChar().toString(),
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = if (isAvailable) Color.White else WislyColors.OnSurfaceMuted.copy(alpha = 0.35f),
                )
            }
        }
    }
}

@Composable
private fun ResultBanner(
    isCorrect: Boolean,
    answer: String,
    hintsUsed: Int,
    modifier: Modifier = Modifier,
) {
    val s = LocalAppStrings.current
    Column(
        modifier = modifier
            .fillMaxWidth()
            .background(
                if (isCorrect) WislyColors.AccentGreen.copy(alpha = 0.10f) else WislyColors.AccentRed.copy(alpha = 0.10f),
                RoundedCornerShape(14.dp),
            )
            .border(
                1.dp,
                if (isCorrect) WislyColors.AccentGreen.copy(alpha = 0.25f) else WislyColors.AccentRed.copy(alpha = 0.25f),
                RoundedCornerShape(14.dp),
            )
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = if (isCorrect) {
                if (hintsUsed == 0) s.writingPerfect else s.writingCorrect
            } else {
                s.writingWrong
            },
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold,
            color = if (isCorrect) WislyColors.AccentGreen else WislyColors.AccentRed,
        )
        if (!isCorrect) {
            Text(
                text = "${s.writingCorrectIs} $answer",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
            )
        }
    }
}

private fun colorForWritingLevel(level: String?): Color = when (level) {
    "A1" -> WislyColors.A1
    "A2" -> WislyColors.A2
    "B1" -> WislyColors.B1
    "B2" -> WislyColors.B2
    "C1" -> WislyColors.C1
    "C2" -> WislyColors.C2
    else -> WislyColors.OnSurfaceMuted
}
