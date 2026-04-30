package com.alialtinok.lexiup.ui.screens.practice

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
import com.alialtinok.lexiup.LexiUpApplication
import com.alialtinok.lexiup.data.model.PhrasalVerb
import com.alialtinok.lexiup.i18n.LocalAppStrings
import com.alialtinok.lexiup.ui.screens.practice.components.QuizOption
import com.alialtinok.lexiup.ui.screens.practice.components.QuizOptionState
import com.alialtinok.lexiup.ui.screens.practice.components.QuizProgressBar
import com.alialtinok.lexiup.ui.screens.practice.components.QuizScoreRow
import com.alialtinok.lexiup.ui.theme.LexiColors
import kotlinx.coroutines.delay

private const val SessionSize = 20

private data class PhrasalQuestion(
    val verb: PhrasalVerb,
    val prompt: String,
    val correct: String,
    val options: List<String>,
)

@Composable
fun PhrasalVerbQuizScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as LexiUpApplication).container }
    val repo = container.wordRepository

    var isTRtoEN by remember { mutableStateOf(false) }
    var sessionKey by remember { mutableIntStateOf(0) }

    val pool = remember { repo.allPhrasalVerbs }

    var question by remember(sessionKey) { mutableStateOf<PhrasalQuestion?>(null) }
    var selected by remember(sessionKey) { mutableStateOf<String?>(null) }
    var correct by remember(sessionKey) { mutableIntStateOf(0) }
    var wrong by remember(sessionKey) { mutableIntStateOf(0) }
    var qNumber by remember(sessionKey) { mutableIntStateOf(0) }

    fun nextQuestion() {
        if (pool.size < 4) {
            question = null
            return
        }
        val v = pool.random()
        val correctAnswer = if (isTRtoEN) v.fullVerb else v.turkish
        val prompt = if (isTRtoEN) v.turkish else v.fullVerb
        val distractors = pool.filter { it.id != v.id }
            .shuffled()
            .take(3)
            .map { if (isTRtoEN) it.fullVerb else it.turkish }
        val opts = (distractors + correctAnswer).shuffled()
        question = PhrasalQuestion(v, prompt, correctAnswer, opts)
        selected = null
    }

    fun resetSession() {
        sessionKey++
    }

    LaunchedEffect(sessionKey, isTRtoEN) {
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
                title = LocalAppStrings.current.practicePhrasal,
                onBack = onBack,
                isTRtoEN = isTRtoEN,
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
                accent = LexiColors.AccentPurple,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 4.dp),
            )

            QuizProgressBar(
                progress = qNumber / SessionSize.toFloat(),
                color = LexiColors.AccentPurple,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
            )

            val q = question
            if (q == null) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = LocalAppStrings.current.quizNeedPhrasal,
                        color = LexiColors.OnSurfaceMuted,
                    )
                }
            } else {
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
                                if (option == q.correct) correct++ else wrong++
                                qNumber++
                            },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun TopBar(
    title: String,
    onBack: () -> Unit,
    isTRtoEN: Boolean,
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
            .background(LexiColors.Surface, RoundedCornerShape(20.dp))
            .border(1.dp, LexiColors.SurfaceBorder, RoundedCornerShape(20.dp))
            .padding(vertical = 28.dp, horizontal = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = if (isTRtoEN) "FIND THE PHRASAL VERB" else "WHAT DOES IT MEAN?",
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = LexiColors.OnSurfaceMuted,
            letterSpacing = 1.5.sp,
        )
        Text(
            text = prompt,
            fontSize = if (isTRtoEN) 22.sp else 30.sp,
            fontWeight = FontWeight.Black,
            color = if (isTRtoEN) LexiColors.Primary else Color.White,
            textAlign = TextAlign.Center,
            lineHeight = 36.sp,
        )
    }
}
