package com.alialtinok.wisly.ui.screens.practice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import com.alialtinok.wisly.WislyApplication
import com.alialtinok.wisly.data.model.PhrasalVerb
import com.alialtinok.wisly.i18n.LocalAppStrings
import com.alialtinok.wisly.ui.screens.practice.components.QuizOption
import com.alialtinok.wisly.ui.screens.practice.components.QuizOptionState
import com.alialtinok.wisly.ui.screens.practice.components.QuizProgressBar
import com.alialtinok.wisly.ui.screens.practice.components.QuizReviewSheet
import com.alialtinok.wisly.ui.screens.practice.components.QuizScoreRow
import com.alialtinok.wisly.ui.screens.practice.components.ReviewItem
import com.alialtinok.wisly.ui.theme.WislyColors
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.delay

private const val SessionSize = 20

private data class PhrasalQuestion(
    val verb: PhrasalVerb,
    val prompt: String,
    val correct: String,
    val options: List<String>,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhrasalVerbQuizScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as WislyApplication).container }
    val repo = container.wordRepository
    val translationRepo = container.translationRepository
    val nativeLanguage by container.userSettingsRepository.nativeLanguage.collectAsState(initial = null)

    var isTRtoEN by remember { mutableStateOf(false) }
    var sessionKey by remember { mutableIntStateOf(0) }

    val pool = remember { repo.allPhrasalVerbs }

    var question by remember(sessionKey) { mutableStateOf<PhrasalQuestion?>(null) }
    var selected by remember(sessionKey) { mutableStateOf<String?>(null) }
    var correct by remember(sessionKey) { mutableIntStateOf(0) }
    var wrong by remember(sessionKey) { mutableIntStateOf(0) }
    var qNumber by remember(sessionKey) { mutableIntStateOf(0) }
    val wrongVerbs = remember(sessionKey) { mutableStateListOf<PhrasalVerb>() }
    var showReview by remember(sessionKey) { mutableStateOf(false) }
    var reviewItems by remember(sessionKey) { mutableStateOf<List<ReviewItem>>(emptyList()) }
    var isGenerating by remember(sessionKey) { mutableStateOf(false) }

    suspend fun nextQuestion() {
        if (pool.size < 4) { question = null; return }
        selected = null
        question = null
        isGenerating = true
        val v = pool.random()
        val others = pool.filter { it.id != v.id }.shuffled().take(3)
        val lang = nativeLanguage

        suspend fun nat(verb: PhrasalVerb): String {
            if (lang == null || lang.id == "tr") return verb.turkish
            return translationRepo.fetch(verb.fullVerb, lang.translationCode) ?: verb.turkish
        }

        if (isTRtoEN) {
            val prompt = nat(v)
            question = PhrasalQuestion(v, prompt, v.fullVerb, (others.map { it.fullVerb } + v.fullVerb).shuffled())
        } else {
            val results = coroutineScope {
                listOf(v, others[0], others[1], others[2]).map { item -> async { nat(item) } }.map { it.await() }
            }
            val correctNative = results[0]
            question = PhrasalQuestion(v, v.fullVerb, correctNative, (results.drop(1) + correctNative).shuffled())
        }
        isGenerating = false
    }

    fun resetSession() { sessionKey++ }

    LaunchedEffect(sessionKey, isTRtoEN, nativeLanguage) {
        if (nativeLanguage == null) return@LaunchedEffect
        if (pool.size >= 4 && question == null) nextQuestion()
    }

    LaunchedEffect(selected) {
        val s = selected ?: return@LaunchedEffect
        delay(900)
        if (qNumber >= SessionSize) {
            if (wrongVerbs.isNotEmpty()) showReview = true else resetSession()
        } else {
            nextQuestion()
        }
    }

    LaunchedEffect(showReview, nativeLanguage) {
        if (!showReview || wrongVerbs.isEmpty()) return@LaunchedEffect
        val lang = nativeLanguage
        reviewItems = wrongVerbs.map { v ->
            val native = if (lang == null || lang.id == "tr") v.turkish
            else translationRepo.fetch(v.fullVerb, lang.translationCode) ?: v.turkish
            if (isTRtoEN) ReviewItem(front = native, back = v.fullVerb, example = v.example)
            else ReviewItem(front = v.fullVerb, back = native, example = v.example)
        }
    }

    val langCode = (nativeLanguage?.id ?: "tr").uppercase()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WislyColors.Background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            TopBar(
                title = LocalAppStrings.current.practicePhrasal,
                onBack = onBack,
                isTRtoEN = isTRtoEN,
                langCode = langCode,
                onToggle = {
                    isTRtoEN = !isTRtoEN
                    resetSession()
                },
            )

            QuizScoreRow(
                correct = correct,
                wrong = wrong,
                qNumber = qNumber,
                sessionSize = SessionSize,
                accent = WislyColors.AccentPurple,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )

            QuizProgressBar(
                progress = qNumber / SessionSize.toFloat(),
                color = WislyColors.AccentPurple,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )

            val q = question
            if (q == null && !isGenerating) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = LocalAppStrings.current.quizNeedPhrasal,
                        color = WislyColors.OnSurfaceMuted,
                    )
                }
            } else if (q != null) {
                QuestionCard(
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
                                    if (wrongVerbs.none { it.id == q.verb.id }) wrongVerbs.add(q.verb)
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
                accentColor = WislyColors.AccentPurple,
                onRestart = { showReview = false; resetSession() },
            )
        }
    }
}

@Composable
private fun TopBar(
    title: String,
    onBack: () -> Unit,
    isTRtoEN: Boolean,
    langCode: String,
    onToggle: () -> Unit,
) {
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
                contentDescription = LocalAppStrings.current.back,
                tint = Color.White,
            )
        }
        Box(
            modifier = Modifier
                .clickable(onClick = onToggle)
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
                text = if (isTRtoEN) "$langCode→EN" else "EN→$langCode",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (isTRtoEN) WislyColors.AccentPurple else WislyColors.OnSurfaceMuted,
            )
        }
        Text(
            text = title,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
        )
    }
}

@Composable
private fun QuestionCard(
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
        Text(
            text = if (isTRtoEN) "FIND THE PHRASAL VERB" else "WHAT DOES IT MEAN?",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = WislyColors.OnSurfaceMuted,
            letterSpacing = 1.5.sp,
        )
        Text(
            text = prompt,
            fontSize = if (isTRtoEN) 22.sp else 30.sp,
            fontWeight = FontWeight.Black,
            color = if (isTRtoEN) WislyColors.Primary else Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp,
        )
    }
}
