package com.alialtinok.wisly.ui.screens.practice

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
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
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
import com.alialtinok.wisly.WislyApplication
import com.alialtinok.wisly.data.model.Word
import com.alialtinok.wisly.i18n.LocalAppStrings
import com.alialtinok.wisly.ui.screens.practice.components.QuizOption
import com.alialtinok.wisly.ui.screens.practice.components.QuizOptionState
import com.alialtinok.wisly.ui.screens.practice.components.QuizProgressBar
import com.alialtinok.wisly.ui.screens.practice.components.QuizScoreRow
import com.alialtinok.wisly.ui.screens.practice.components.QuizReviewSheet
import com.alialtinok.wisly.ui.screens.practice.components.ReviewItem
import com.alialtinok.wisly.ui.screens.study.components.LevelOption
import com.alialtinok.wisly.ui.screens.study.components.LevelPickerContent
import com.alialtinok.wisly.ui.theme.WislyColors
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
    val translationRepo = container.translationRepository
    val nativeLanguage by container.userSettingsRepository.nativeLanguage.collectAsState(initial = null)

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
    val wrongWords = remember(sessionKey) { mutableStateListOf<Word>() }
    var showReview by remember(sessionKey) { mutableStateOf(false) }
    var reviewItems by remember(sessionKey) { mutableStateOf<List<com.alialtinok.wisly.ui.screens.practice.components.ReviewItem>>(emptyList()) }
    var translatedHint by remember { mutableStateOf<String?>(null) }

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

    LaunchedEffect(question, nativeLanguage) {
        val w = question?.word ?: return@LaunchedEffect
        val lang = nativeLanguage
        translatedHint = if (lang == null || lang.id == "tr") w.turkish
        else translationRepo.fetch(w.word, lang.translationCode) ?: w.turkish
    }

    LaunchedEffect(showReview, nativeLanguage) {
        if (!showReview || wrongWords.isEmpty()) return@LaunchedEffect
        val lang = nativeLanguage
        reviewItems = wrongWords.map { w ->
            val native = if (lang == null || lang.id == "tr") w.turkish
            else translationRepo.fetch(w.word, lang.translationCode) ?: w.turkish
            com.alialtinok.wisly.ui.screens.practice.components.ReviewItem(front = native, back = w.word, example = w.example)
        }
    }

    LaunchedEffect(sessionKey, pool, nativeLanguage) {
        if (nativeLanguage == null) return@LaunchedEffect
        if (pool.size >= 4 && question == null) nextQuestion()
    }

    LaunchedEffect(selected) {
        val s = selected ?: return@LaunchedEffect
        delay(900)
        if (qNumber >= SessionSize) {
            if (wrongWords.isNotEmpty()) showReview = true else resetSession()
        } else {
            nextQuestion()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WislyColors.Background),
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
                accent = WislyColors.AccentGreen,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )

            QuizProgressBar(
                progress = qNumber / SessionSize.toFloat(),
                color = WislyColors.AccentGreen,
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
                        color = WislyColors.OnSurfaceMuted,
                    )
                }
            } else {
                SentenceCard(
                    hint = translatedHint ?: "…",
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
                                if (option == q.correct) {
                                    correct++
                                } else {
                                    wrong++
                                    if (wrongWords.none { it.id == q.word.id }) wrongWords.add(q.word)
                                }
                                qNumber++
                            },
                        )
                    }
                }
            }
        }
    }

    if (showReview && reviewItems.isNotEmpty()) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showReview = false; resetSession() },
            sheetState = sheetState,
            containerColor = WislyColors.Background,
        ) {
            QuizReviewSheet(
                items = reviewItems,
                accentColor = WislyColors.AccentGreen,
                onRestart = { showReview = false; resetSession() },
            )
        }
    }

    if (showLevelPicker) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = { showLevelPicker = false },
            sheetState = sheetState,
            containerColor = WislyColors.Background,
        ) {
            val s = LocalAppStrings.current
            val options = buildList {
                add(
                    LevelOption(
                        id = null,
                        title = s.levelPickerAll,
                        subtitle = "${repo.allWords.size} ${s.countWordsLabel}",
                        color = WislyColors.Primary,
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
                tint = if (selectedLevel == null) WislyColors.OnSurfaceMuted else colorForLevel(selectedLevel),
                modifier = Modifier.size(20.dp),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = selectedLevel ?: s.levelPickerAll,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (selectedLevel == null) WislyColors.OnSurfaceMuted else colorForLevel(selectedLevel),
            )
        }
    }
}

@Composable
private fun SentenceCard(
    hint: String,
    sentence: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(WislyColors.Surface, RoundedCornerShape(20.dp))
            .border(1.dp, WislyColors.SurfaceBorder, RoundedCornerShape(20.dp))
            .padding(vertical = 24.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = LocalAppStrings.current.fillBlankHeading,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = WislyColors.OnSurfaceMuted,
            letterSpacing = 2.sp,
        )
        Text(
            text = hint,
            fontSize = 14.sp,
            fontWeight = FontWeight.SemiBold,
            color = WislyColors.Primary,
            modifier = Modifier
                .background(WislyColors.Primary.copy(alpha = 0.1f), RoundedCornerShape(8.dp))
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
    "A1" -> WislyColors.A1
    "A2" -> WislyColors.A2
    "B1" -> WislyColors.B1
    "B2" -> WislyColors.B2
    "C1" -> WislyColors.C1
    "C2" -> WislyColors.C2
    else -> WislyColors.OnSurfaceMuted
}
