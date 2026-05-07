package com.alialtinok.lexiup.ui.screens.study

import androidx.compose.animation.core.animateFloatAsState
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alialtinok.lexiup.WislyApplication
import com.alialtinok.lexiup.data.model.Word
import com.alialtinok.lexiup.i18n.LocalAppStrings
import com.alialtinok.lexiup.ui.screens.study.components.LevelOption
import com.alialtinok.lexiup.ui.screens.study.components.LevelPickerContent
import com.alialtinok.lexiup.ui.theme.LexiColors
import kotlinx.coroutines.delay

private const val SessionSize = 20
private val CefrLevels = listOf("A1", "A2", "B1", "B2", "C1")

private data class Question(
    val word: Word,
    val prompt: String,
    val correct: String,
    val options: List<String>,
)

private enum class OptionState { Neutral, Correct, Wrong, Dimmed }

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as WislyApplication).container }
    val repo = container.wordRepository

    var selectedLevel by remember { mutableStateOf<String?>(null) }
    var isTRtoEN by remember { mutableStateOf(false) }
    var sessionKey by remember { mutableIntStateOf(0) }
    var showLevelPicker by remember { mutableStateOf(false) }

    val pool = remember(selectedLevel) {
        if (selectedLevel == null) repo.allWords
        else repo.allWords.filter { it.level == selectedLevel }
    }

    var question by remember(sessionKey) { mutableStateOf<Question?>(null) }
    var selected by remember(sessionKey) { mutableStateOf<String?>(null) }
    var correct by remember(sessionKey) { mutableIntStateOf(0) }
    var wrong by remember(sessionKey) { mutableIntStateOf(0) }
    var qNumber by remember(sessionKey) { mutableIntStateOf(0) }
    val wrongWords = remember(sessionKey) { mutableStateListOf<Word>() }
    var showReview by remember(sessionKey) { mutableStateOf(false) }

    fun nextQuestion() {
        if (pool.size < 4) {
            question = null
            return
        }
        val w = pool.random()
        val correctAnswer = if (isTRtoEN) w.word else w.turkish
        val prompt = if (isTRtoEN) w.turkish else w.word
        val distractors = pool.filter { it.id != w.id }
            .shuffled()
            .take(3)
            .map { if (isTRtoEN) it.word else it.turkish }
        val opts = (distractors + correctAnswer).shuffled()
        question = Question(w, prompt, correctAnswer, opts)
        selected = null
    }

    fun resetSession() {
        sessionKey++
    }

    LaunchedEffect(sessionKey, isTRtoEN, pool) {
        if (pool.size >= 4 && question == null) nextQuestion()
    }

    LaunchedEffect(selected) {
        val s = selected ?: return@LaunchedEffect
        delay(900)
        if (qNumber >= SessionSize) {
            if (wrongWords.isNotEmpty()) {
                showReview = true
            } else {
                resetSession()
            }
        } else {
            nextQuestion()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LexiColors.Background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            QuizTopBar(
                onBack = onBack,
                isTRtoEN = isTRtoEN,
                onToggleDirection = {
                    isTRtoEN = !isTRtoEN
                    resetSession()
                },
                selectedLevel = selectedLevel,
                onOpenLevelPicker = { showLevelPicker = true },
            )

            ScoreRow(
                correct = correct,
                wrong = wrong,
                qNumber = qNumber,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )

            ProgressBar(
                progress = qNumber / SessionSize.toFloat(),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )

            val q = question
            if (q == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = LocalAppStrings.current.quizNeedWords,
                        color = LexiColors.OnSurfaceMuted,
                    )
                }
            } else {
                QuestionCard(
                    word = q.word,
                    prompt = q.prompt,
                    isTRtoEN = isTRtoEN,
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
                            selected == null -> OptionState.Neutral
                            option == q.correct -> OptionState.Correct
                            option == selected -> OptionState.Wrong
                            else -> OptionState.Dimmed
                        }
                        OptionButton(
                            text = option,
                            state = state,
                            onClick = {
                                if (selected != null) return@OptionButton
                                selected = option
                                if (option == q.correct) {
                                    correct++
                                } else {
                                    wrong++
                                    if (wrongWords.none { it.id == q.word.id }) {
                                        wrongWords.add(q.word)
                                    }
                                }
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

    if (showReview) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = {
                showReview = false
                resetSession()
            },
            sheetState = sheetState,
            containerColor = LexiColors.Background,
        ) {
            QuizReviewContent(
                wrongWords = wrongWords.toList(),
                isTRtoEN = isTRtoEN,
                onRestart = {
                    showReview = false
                    resetSession()
                },
            )
        }
    }
}

@Composable
private fun QuizTopBar(
    onBack: () -> Unit,
    isTRtoEN: Boolean,
    onToggleDirection: () -> Unit,
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
        Box(
            modifier = Modifier
                .clickable(onClick = onToggleDirection)
                .background(
                    if (isTRtoEN) LexiColors.AccentPurple.copy(alpha = 0.15f) else LexiColors.Surface,
                    RoundedCornerShape(8.dp),
                )
                .border(
                    1.dp,
                    if (isTRtoEN) LexiColors.AccentPurple else LexiColors.SurfaceBorder,
                    RoundedCornerShape(8.dp),
                )
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(
                text = if (isTRtoEN) "TR→EN" else "EN→TR",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isTRtoEN) LexiColors.AccentPurple else LexiColors.OnSurfaceMuted,
            )
        }
        Text(
            text = s.quizTitle,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
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
private fun ScoreRow(
    correct: Int,
    wrong: Int,
    qNumber: Int,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = LexiColors.AccentGreen,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = correct.toString(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = LexiColors.AccentGreen,
            )
        }
        Spacer(Modifier.width(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Cancel,
                contentDescription = null,
                tint = LexiColors.AccentRed,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = wrong.toString(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = LexiColors.AccentRed,
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = "$qNumber/$SessionSize",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = LexiColors.OnSurfaceMuted,
        )
        if (correct + wrong > 0) {
            val pct = (correct.toDouble() / (correct + wrong) * 100).toInt()
            Spacer(Modifier.width(10.dp))
            Text(
                text = "%$pct",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = LexiColors.Primary,
            )
        }
    }
}

@Composable
private fun ProgressBar(progress: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(LexiColors.SurfaceBorder, RoundedCornerShape(2.dp)),
    ) {
        val animated by animateFloatAsState(targetValue = progress, label = "quiz-progress")
        Box(
            modifier = Modifier
                .fillMaxWidth(animated.coerceIn(0f, 1f))
                .height(4.dp)
                .background(LexiColors.Primary, RoundedCornerShape(2.dp)),
        )
    }
}

@Composable
private fun QuestionCard(
    word: Word,
    prompt: String,
    isTRtoEN: Boolean,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(LexiColors.Surface, RoundedCornerShape(20.dp))
            .border(1.dp, LexiColors.SurfaceBorder, RoundedCornerShape(20.dp))
            .padding(vertical = 28.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (!isTRtoEN) {
            Text(
                text = word.type.replaceFirstChar { it.uppercase() },
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
                color = LexiColors.OnSurfaceMuted,
            )
        }
        Text(
            text = prompt,
            fontSize = if (isTRtoEN) 28.sp else 38.sp,
            fontWeight = FontWeight.Black,
            color = if (isTRtoEN) LexiColors.Primary else Color.White,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun OptionButton(
    text: String,
    state: OptionState,
    onClick: () -> Unit,
) {
    val bg = when (state) {
        OptionState.Neutral -> LexiColors.Surface
        OptionState.Correct -> LexiColors.AccentGreen.copy(alpha = 0.15f)
        OptionState.Wrong -> LexiColors.AccentRed.copy(alpha = 0.15f)
        OptionState.Dimmed -> LexiColors.Surface.copy(alpha = 0.5f)
    }
    val border = when (state) {
        OptionState.Neutral -> LexiColors.SurfaceBorder
        OptionState.Correct -> LexiColors.AccentGreen
        OptionState.Wrong -> LexiColors.AccentRed
        OptionState.Dimmed -> LexiColors.SurfaceBorder.copy(alpha = 0.4f)
    }
    val textColor = when (state) {
        OptionState.Neutral -> Color.White
        OptionState.Correct -> LexiColors.AccentGreen
        OptionState.Wrong -> LexiColors.AccentRed
        OptionState.Dimmed -> LexiColors.OnSurfaceMuted.copy(alpha = 0.6f)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(14.dp))
            .border(1.dp, border, RoundedCornerShape(14.dp))
            .clickable(enabled = state == OptionState.Neutral, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.weight(1f),
        )
        when (state) {
            OptionState.Correct -> Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = LexiColors.AccentGreen,
            )
            OptionState.Wrong -> Icon(
                imageVector = Icons.Filled.Cancel,
                contentDescription = null,
                tint = LexiColors.AccentRed,
            )
            else -> Unit
        }
    }
}

@Composable
private fun QuizReviewContent(
    wrongWords: List<Word>,
    isTRtoEN: Boolean,
    onRestart: () -> Unit,
) {
    val s = LocalAppStrings.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = s.quizReview,
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Text(
            text = "${wrongWords.size} ${s.countWordsLabel} ${s.quizReviewSuffix}",
            fontSize = 13.sp,
            color = LexiColors.OnSurfaceMuted,
            modifier = Modifier.padding(bottom = 4.dp),
        )
        LazyColumn(
            modifier = Modifier
                .fillMaxWidth()
                .height(360.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(wrongWords, key = { it.id }) { w ->
                ReviewRow(word = w, isTRtoEN = isTRtoEN)
            }
        }
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(LexiColors.Primary, RoundedCornerShape(14.dp))
                .clickable(onClick = onRestart)
                .padding(vertical = 16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = s.quizRestart,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
        Spacer(Modifier.height(8.dp))
    }
}

@Composable
private fun ReviewRow(word: Word, isTRtoEN: Boolean) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LexiColors.Surface, RoundedCornerShape(12.dp))
            .border(1.dp, LexiColors.SurfaceBorder, RoundedCornerShape(12.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = if (isTRtoEN) word.turkish else word.word,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                text = if (isTRtoEN) word.word else word.turkish,
                fontSize = 13.sp,
                color = LexiColors.Primary,
            )
        }
        Text(
            text = word.level,
            fontSize = 11.sp,
            fontWeight = FontWeight.Black,
            color = colorForLevel(word.level),
            modifier = Modifier
                .background(colorForLevel(word.level).copy(alpha = 0.15f), RoundedCornerShape(6.dp))
                .padding(horizontal = 8.dp, vertical = 3.dp),
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
