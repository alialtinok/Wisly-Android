package com.alialtinok.wisly.ui.screens.study

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alialtinok.wisly.WislyApplication
import com.alialtinok.wisly.data.model.NativeLanguage
import com.alialtinok.wisly.data.model.Word
import com.alialtinok.wisly.data.repository.TranslationRepository
import com.alialtinok.wisly.i18n.LocalAppStrings
import com.alialtinok.wisly.ui.screens.study.components.LevelOption
import com.alialtinok.wisly.ui.screens.study.components.LevelPickerContent
import com.alialtinok.wisly.ui.theme.WislyColors
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

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
    val translationRepo = container.translationRepository
    val nativeLanguage by container.userSettingsRepository.nativeLanguage.collectAsState(initial = null)

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
    var isGenerating by remember(sessionKey) { mutableStateOf(false) }

    suspend fun nextQuestion() {
        if (pool.size < 4) { question = null; return }
        selected = null
        question = null
        isGenerating = true
        val w = pool.random()
        val others = pool.filter { it.id != w.id }.shuffled().take(3)
        val lang = nativeLanguage

        suspend fun nat(word: Word): String {
            if (lang == null || lang.id == "tr") return word.turkish
            return translationRepo.fetch(word.word, lang.translationCode) ?: word.turkish
        }

        if (isTRtoEN) {
            val prompt = nat(w)
            question = Question(w, prompt, w.word, (others.map { it.word } + w.word).shuffled())
        } else {
            val results = coroutineScope {
                listOf(w, others[0], others[1], others[2]).map { item -> async { nat(item) } }.map { it.await() }
            }
            val correctNative = results[0]
            val opts = (results.drop(1) + correctNative).shuffled()
            question = Question(w, w.word, correctNative, opts)
        }
        isGenerating = false
    }

    fun resetSession() {
        sessionKey++
    }

    LaunchedEffect(sessionKey, isTRtoEN, pool, nativeLanguage) {
        if (nativeLanguage == null) return@LaunchedEffect
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
            .background(WislyColors.Background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            QuizTopBar(
                onBack = onBack,
                isTRtoEN = isTRtoEN,
                nativeLanguage = nativeLanguage,
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

            QuizProgressBar(
                progress = qNumber / SessionSize.toFloat(),
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )

            val q = question
            if (q == null && !isGenerating) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = LocalAppStrings.current.quizNeedWords,
                        color = WislyColors.OnSurfaceMuted,
                    )
                }
            } else if (q != null) {
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

    if (showReview) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = {
                showReview = false
                resetSession()
            },
            sheetState = sheetState,
            containerColor = WislyColors.Background,
        ) {
            QuizReviewContent(
                wrongWords = wrongWords.toList(),
                isTRtoEN = isTRtoEN,
                nativeLanguage = nativeLanguage,
                translationRepo = translationRepo,
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
    nativeLanguage: NativeLanguage?,
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
                    if (isTRtoEN) WislyColors.AccentPurple.copy(alpha = 0.15f) else WislyColors.Surface,
                    RoundedCornerShape(8.dp),
                )
                .border(
                    1.dp,
                    if (isTRtoEN) WislyColors.AccentPurple else WislyColors.SurfaceBorder,
                    RoundedCornerShape(8.dp),
                )
                .padding(horizontal = 10.dp, vertical = 6.dp),
        ) {
            Text(
                text = run { val c = (nativeLanguage?.id ?: "tr").uppercase(); if (isTRtoEN) "$c→EN" else "EN→$c" },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isTRtoEN) WislyColors.AccentPurple else WislyColors.OnSurfaceMuted,
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
                tint = WislyColors.AccentGreen,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = correct.toString(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = WislyColors.AccentGreen,
            )
        }
        Spacer(Modifier.width(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Cancel,
                contentDescription = null,
                tint = WislyColors.AccentRed,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = wrong.toString(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = WislyColors.AccentRed,
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = "$qNumber/$SessionSize",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = WislyColors.OnSurfaceMuted,
        )
        if (correct + wrong > 0) {
            val pct = (correct.toDouble() / (correct + wrong) * 100).toInt()
            Spacer(Modifier.width(10.dp))
            Text(
                text = "%$pct",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = WislyColors.Primary,
            )
        }
    }
}

@Composable
private fun QuizProgressBar(progress: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(WislyColors.SurfaceBorder, RoundedCornerShape(2.dp)),
    ) {
        val animated by animateFloatAsState(targetValue = progress, label = "quiz-progress")
        Box(
            modifier = Modifier
                .fillMaxWidth(animated.coerceIn(0f, 1f))
                .height(4.dp)
                .background(WislyColors.Primary, RoundedCornerShape(2.dp)),
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
            .background(WislyColors.Surface, RoundedCornerShape(20.dp))
            .border(1.dp, WislyColors.SurfaceBorder, RoundedCornerShape(20.dp))
            .padding(vertical = 28.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (!isTRtoEN) {
            Text(
                text = word.type.replaceFirstChar { it.uppercase() },
                fontSize = 12.sp,
                fontStyle = FontStyle.Italic,
                color = WislyColors.OnSurfaceMuted,
            )
        }
        Text(
            text = prompt,
            fontSize = if (isTRtoEN) 28.sp else 38.sp,
            fontWeight = FontWeight.Black,
            color = if (isTRtoEN) WislyColors.Primary else Color.White,
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
        OptionState.Neutral -> WislyColors.Surface
        OptionState.Correct -> WislyColors.AccentGreen.copy(alpha = 0.15f)
        OptionState.Wrong -> WislyColors.AccentRed.copy(alpha = 0.15f)
        OptionState.Dimmed -> WislyColors.Surface.copy(alpha = 0.5f)
    }
    val border = when (state) {
        OptionState.Neutral -> WislyColors.SurfaceBorder
        OptionState.Correct -> WislyColors.AccentGreen
        OptionState.Wrong -> WislyColors.AccentRed
        OptionState.Dimmed -> WislyColors.SurfaceBorder.copy(alpha = 0.4f)
    }
    val textColor = when (state) {
        OptionState.Neutral -> Color.White
        OptionState.Correct -> WislyColors.AccentGreen
        OptionState.Wrong -> WislyColors.AccentRed
        OptionState.Dimmed -> WislyColors.OnSurfaceMuted.copy(alpha = 0.6f)
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
                tint = WislyColors.AccentGreen,
            )
            OptionState.Wrong -> Icon(
                imageVector = Icons.Filled.Cancel,
                contentDescription = null,
                tint = WislyColors.AccentRed,
            )
            else -> Unit
        }
    }
}

@Composable
private fun QuizReviewContent(
    wrongWords: List<Word>,
    isTRtoEN: Boolean,
    nativeLanguage: NativeLanguage?,
    translationRepo: TranslationRepository,
    onRestart: () -> Unit,
) {
    val s = LocalAppStrings.current
    val scope = rememberCoroutineScope()
    val density = LocalDensity.current

    var currentIndex by remember { mutableIntStateOf(0) }
    var isFlipped by remember { mutableStateOf(false) }
    var isDone by remember { mutableStateOf(false) }
    var isAdvancing by remember { mutableStateOf(false) }
    val offsetX = remember { Animatable(0f) }
    val flipRotation by animateFloatAsState(if (isFlipped) 180f else 0f, tween(400), label = "flip")

    fun nextCard() {
        if (isAdvancing) return
        isAdvancing = true
        scope.launch {
            offsetX.animateTo(700f, tween(250))
            isFlipped = false
            if (currentIndex + 1 < wrongWords.size) currentIndex++ else isDone = true
            offsetX.snapTo(0f)
            isAdvancing = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp),
    ) {
        if (isDone) {
            Column(
                modifier = Modifier.fillMaxWidth().padding(vertical = 32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.EmojiEvents,
                    contentDescription = null,
                    tint = WislyColors.AccentAmber,
                    modifier = Modifier.size(64.dp),
                )
                Text(s.reviewComplete, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text(
                    text = "${wrongWords.size} ${s.reviewedWrongSuffix}",
                    fontSize = 14.sp,
                    color = WislyColors.OnSurfaceMuted,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WislyColors.Primary, RoundedCornerShape(14.dp))
                        .clickable(onClick = onRestart)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(s.startNewSession, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(Modifier.height(8.dp))
            }
        } else {
            val word = wrongWords[currentIndex]

            var cardNative by remember(currentIndex) { mutableStateOf(word.turkish) }
            LaunchedEffect(currentIndex, nativeLanguage) {
                val lang = nativeLanguage
                cardNative = if (lang == null || lang.id == "tr") word.turkish
                else translationRepo.fetch(word.word, lang.translationCode) ?: word.turkish
            }

            val langCode = (nativeLanguage?.id ?: "tr").uppercase()

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(s.reviewWrongAnswers, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = WislyColors.OnSurfaceMuted)
                    Text("${currentIndex + 1} / ${wrongWords.size}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Icon(Icons.Filled.Cancel, null, tint = WislyColors.AccentRed, modifier = Modifier.size(28.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(WislyColors.SurfaceBorder, RoundedCornerShape(2.dp)),
            ) {
                val progress by animateFloatAsState(currentIndex.toFloat() / wrongWords.size, label = "review-progress")
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(WislyColors.AccentRed, RoundedCornerShape(2.dp)),
                )
            }

            Spacer(Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(240.dp)
                    .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                    .graphicsLayer { this.rotationY = flipRotation; cameraDistance = 12 * density.density }
                    .pointerInput(currentIndex) {
                        detectTapGestures { if (!isAdvancing) isFlipped = !isFlipped }
                    }
                    .pointerInput(currentIndex) {
                        detectHorizontalDragGestures(
                            onDragEnd = {
                                scope.launch {
                                    if (abs(offsetX.value) > 80f) nextCard()
                                    else offsetX.animateTo(0f, tween(200))
                                }
                            },
                        ) { change, dragAmount ->
                            change.consume()
                            scope.launch { offsetX.snapTo(offsetX.value + dragAmount) }
                        }
                    }
                    .background(WislyColors.Surface, RoundedCornerShape(24.dp))
                    .border(1.dp, WislyColors.AccentRed.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .padding(28.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (flipRotation <= 90f) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = if (isTRtoEN) "$langCode → EN" else "EN → $langCode",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WislyColors.OnSurfaceMuted,
                            letterSpacing = 2.sp,
                        )
                        Text(
                            text = if (isTRtoEN) cardNative else word.word,
                            fontSize = if (isTRtoEN) 26.sp else 36.sp,
                            fontWeight = FontWeight.Black,
                            color = if (isTRtoEN) WislyColors.Primary else Color.White,
                            textAlign = TextAlign.Center,
                        )
                        if (!isTRtoEN) {
                            Text(
                                text = word.type.replaceFirstChar { it.uppercase() },
                                fontSize = 12.sp,
                                fontStyle = FontStyle.Italic,
                                color = WislyColors.OnSurfaceMuted,
                            )
                        }
                        Text(s.reviewTapToFlip, fontSize = 11.sp, color = WislyColors.OnSurfaceMuted)
                    }
                } else {
                    Column(
                        modifier = Modifier.graphicsLayer { rotationY = 180f },
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = s.reviewCorrectAnswer,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold,
                            color = WislyColors.AccentGreen,
                            letterSpacing = 2.sp,
                        )
                        Text(
                            text = if (isTRtoEN) word.word else cardNative,
                            fontSize = 32.sp,
                            fontWeight = FontWeight.Black,
                            color = WislyColors.AccentGreen,
                            textAlign = TextAlign.Center,
                        )
                        if (word.example.isNotBlank()) {
                            Text(
                                text = word.example,
                                fontSize = 13.sp,
                                fontStyle = FontStyle.Italic,
                                color = Color.White.copy(alpha = 0.7f),
                                textAlign = TextAlign.Center,
                            )
                        }
                    }
                }
            }

            Text(
                text = s.reviewSwipeToSkip,
                fontSize = 12.sp,
                color = WislyColors.OnSurfaceMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WislyColors.Primary, RoundedCornerShape(14.dp))
                    .clickable { nextCard() }
                    .padding(vertical = 16.dp),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = "${s.reviewNextWord} →",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
            }

            Spacer(Modifier.height(16.dp))
        }
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
