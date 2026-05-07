package com.alialtinok.lexiup.ui.screens.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alialtinok.lexiup.WislyApplication
import com.alialtinok.lexiup.data.model.Word
import com.alialtinok.lexiup.i18n.LocalAppStrings
import com.alialtinok.lexiup.ui.screens.practice.components.QuizOption
import com.alialtinok.lexiup.ui.screens.practice.components.QuizOptionState
import com.alialtinok.lexiup.ui.screens.practice.components.QuizProgressBar
import com.alialtinok.lexiup.ui.screens.practice.components.QuizScoreRow
import com.alialtinok.lexiup.ui.screens.study.components.LevelOption
import com.alialtinok.lexiup.ui.screens.study.components.LevelPickerContent
import com.alialtinok.lexiup.ui.theme.LexiColors
import kotlinx.coroutines.delay

private const val SessionSize = 20
private val CefrLevels = listOf("A1", "A2", "B1", "B2", "C1")

private data class BlankQuestion(
    val word: Word,
    val sentence: String,
    val correct: String,
    val options: List<String>,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FillBlankScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as WislyApplication).container }
    val repo = container.wordRepository

    var selectedLevel by remember { mutableStateOf<String?>(null) }
    var sessionKey by remember { mutableIntStateOf(0) }
    var showLevelPicker by remember { mutableStateOf(false) }

    val pool = remember(selectedLevel) {
        val base = if (selectedLevel == null) repo.allWords
        else repo.allWords.filter { it.level == selectedLevel }
        // Only words whose example contains the word itself (case-insensitive)
        base.filter { w -> w.example.contains(w.word, ignoreCase = true) }
    }

    var question by remember(sessionKey) { mutableStateOf<BlankQuestion?>(null) }
    var selected by remember(sessionKey) { mutableStateOf<String?>(null) }
    var correct by remember(sessionKey) { mutableIntStateOf(0) }
    var wrong by remember(sessionKey) { mutableIntStateOf(0) }
    var qNumber by remember(sessionKey) { mutableIntStateOf(0) }

    fun nextQuestion() {
        if (pool.size < 4) {
            question = null
            return
        }
        val w = pool.random()
        val correctAnswer = w.word
        val sentence = w.example.replace(
            regex = Regex(Regex.escape(w.word), RegexOption.IGNORE_CASE),
            replacement = "___",
        )
        val distractors = pool.filter { it.id != w.id }.shuffled().take(3).map { it.word }
        val opts = (distractors + correctAnswer).shuffled()
        question = BlankQuestion(w, sentence, correctAnswer, opts)
        selected = null
    }

    fun resetSession() {
        sessionKey++
    }

    LaunchedEffect(sessionKey, pool) {
        if (pool.size >= 4 && question == null) nextQuestion()
    }

    LaunchedEffect(selected) {
        val s = selected ?: return@LaunchedEffect
        delay(900)
        if (qNumber >= SessionSize) resetSession()
        else nextQuestion()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LexiColors.Background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                title = LocalAppStrings.current.fillBlankTitle,
                onBack = onBack,
                selectedLevel = selectedLevel,
                onOpenLevelPicker = { showLevelPicker = true },
            )

            QuizScoreRow(
                correct = correct,
                wrong = wrong,
                qNumber = qNumber,
                sessionSize = SessionSize,
                accent = LexiColors.AccentGreen,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )

            QuizProgressBar(
                progress = qNumber / SessionSize.toFloat(),
                color = LexiColors.AccentGreen,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )

            val q = question
            if (q == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = LocalAppStrings.current.fillBlankNeed,
                        color = LexiColors.OnSurfaceMuted,
                    )
                }
            } else {
                SentenceCard(
                    word = q.word,
                    sentence = q.sentence,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 4.dp),
                )

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    q.options.forEach { option ->
                        val state = when {
                            selected == null -> QuizOptionState.Neutral
                            option == q.correct -> QuizOptionState.Correct
                            option == selected -> QuizOptionState.Wrong
                            else -> QuizOptionState.Dimmed
                        }
                        QuizOption(
                            text = option,
                            state = state,
                            onClick = {
                                if (selected != null) return@QuizOption
                                selected = option
                                if (option == q.correct) correct++ else wrong++
                                qNumber++
                            },
                        )
                    }
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
            val s = LocalAppStrings.current
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
                    resetSession()
                },
            )
        }
    }
}

@Composable
private fun TopBar(
    title: String,
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
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = s.back,
                tint = Color.White,
            )
        }
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .weight(1f)
                .padding(start = 4.dp),
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
                tint = if (selectedLevel == null) LexiColors.OnSurfaceMuted else colorForLevel(selectedLevel),
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = selectedLevel ?: s.levelPickerAll,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selectedLevel == null) LexiColors.OnSurfaceMuted else colorForLevel(selectedLevel),
            )
        }
    }
}

@Composable
private fun SentenceCard(
    word: Word,
    sentence: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(LexiColors.Surface, RoundedCornerShape(20.dp))
            .border(1.dp, LexiColors.SurfaceBorder, RoundedCornerShape(20.dp))
            .padding(vertical = 24.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = LocalAppStrings.current.fillBlankHeading,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = LexiColors.OnSurfaceMuted,
            letterSpacing = 2.sp,
        )
        Text(
            text = word.turkish,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = LexiColors.Primary,
            modifier = Modifier
                .background(LexiColors.Primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 4.dp),
        )
        Text(
            text = sentence,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 26.sp,
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
