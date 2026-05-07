package com.alialtinok.lexiup.ui.screens.study

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Shuffle
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alialtinok.lexiup.WislyApplication
import com.alialtinok.lexiup.i18n.LocalAppStrings
import com.alialtinok.lexiup.ui.screens.study.components.FlashcardFinished
import com.alialtinok.lexiup.ui.theme.LexiColors
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun PhrasalFlashcardScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as WislyApplication).container }
    val repo = container.wordRepository
    val tts = container.ttsManager
    val scope = rememberCoroutineScope()
    val s = LocalAppStrings.current

    var isShuffled by remember { mutableStateOf(false) }
    var sessionKey by remember { mutableIntStateOf(0) }

    val items = remember(isShuffled, sessionKey) {
        if (isShuffled) repo.allPhrasalVerbs.shuffled() else repo.allPhrasalVerbs
    }

    var currentIndex by remember(sessionKey) { mutableIntStateOf(0) }
    var knownCount by remember(sessionKey) { mutableIntStateOf(0) }
    var unknownCount by remember(sessionKey) { mutableIntStateOf(0) }
    var isFlipped by remember(sessionKey) { mutableStateOf(false) }
    var isFinished by remember(sessionKey) { mutableStateOf(false) }
    var isAnimating by remember(sessionKey) { mutableStateOf(false) }
    val offsetX = remember(sessionKey) { Animatable(0f) }

    LaunchedEffect(currentIndex, sessionKey) {
        if (items.isNotEmpty() && !isFinished) tts.speak(items[currentIndex].fullVerb)
    }

    fun advance(known: Boolean) {
        if (isAnimating || isFinished || items.isEmpty()) return
        isAnimating = true
        scope.launch {
            offsetX.animateTo(if (known) 1500f else -1500f, tween(220))
            if (known) knownCount++ else unknownCount++
            if (currentIndex + 1 >= items.size) isFinished = true else currentIndex++
            isFlipped = false
            offsetX.snapTo(0f)
            isAnimating = false
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(LexiColors.Background)) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(
                modifier = Modifier.fillMaxWidth().statusBarsPadding().padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                IconButton(onClick = onBack) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, null, tint = Color.White)
                }
                Text(
                    text = s.contentPhrasal,
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    modifier = Modifier.weight(1f).padding(start = 4.dp),
                )
                IconButton(onClick = { isShuffled = !isShuffled; sessionKey++ }) {
                    Icon(Icons.Filled.Shuffle, null, tint = if (isShuffled) LexiColors.AccentPurple else LexiColors.OnSurfaceMuted)
                }
            }

            if (isFinished) {
                FlashcardFinished(known = knownCount, unknown = unknownCount, onRestart = { sessionKey++ })
            } else if (items.isEmpty()) {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(s.flashcardNoWords, color = LexiColors.OnSurfaceMuted)
                }
            } else {
                val total = items.size
                ProgressBar(progress = (knownCount + unknownCount) / total.toFloat(), modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp))
                Text("${currentIndex + 1} / $total", fontSize = 13.sp, color = LexiColors.OnSurfaceMuted, modifier = Modifier.fillMaxWidth().padding(bottom = 4.dp), textAlign = TextAlign.Center)

                Spacer(Modifier.weight(1f))

                val verb = items[currentIndex]
                val density = LocalDensity.current
                val flipRotation by animateFloatAsState(if (isFlipped) 180f else 0f, tween(400), label = "flip")

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .offset { IntOffset(offsetX.value.roundToInt(), 0) }
                        .graphicsLayer { this.rotationY = flipRotation; cameraDistance = 12 * density.density }
                        .pointerInput(currentIndex) {
                            detectTapGestures { if (!isAnimating) isFlipped = !isFlipped }
                        }
                        .pointerInput(currentIndex) {
                            detectDragGestures(
                                onDragEnd = {
                                    scope.launch {
                                        if (offsetX.value > 150f) advance(true)
                                        else if (offsetX.value < -150f) advance(false)
                                        else offsetX.animateTo(0f, tween(200))
                                    }
                                },
                            ) { change, dragAmount ->
                                change.consume()
                                scope.launch { offsetX.snapTo(offsetX.value + dragAmount.x) }
                            }
                        }
                        .background(LexiColors.Surface, RoundedCornerShape(24.dp))
                        .border(1.dp, LexiColors.AccentPurple.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                        .padding(32.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    if (flipRotation <= 90f) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Text(verb.fullVerb, fontSize = 26.sp, fontWeight = FontWeight.Bold, color = Color.White, textAlign = TextAlign.Center)
                            Text(s.flashcardTapForTranslation, fontSize = 12.sp, color = LexiColors.OnSurfaceMuted)
                        }
                    } else {
                        Column(
                            modifier = Modifier.graphicsLayer { rotationY = 180f },
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            Text(verb.turkish, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = LexiColors.AccentPurple, textAlign = TextAlign.Center)
                            if (verb.example.isNotBlank()) {
                                Text(verb.example, fontSize = 13.sp, color = Color.White.copy(alpha = 0.7f), textAlign = TextAlign.Center, lineHeight = 18.sp)
                                Text(verb.exampleTr, fontSize = 13.sp, color = LexiColors.OnSurfaceMuted, textAlign = TextAlign.Center, lineHeight = 18.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                Row(
                    modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    ActionButton(text = s.flashcardActionDontKnow, color = LexiColors.AccentRed, onClick = { advance(false) }, modifier = Modifier.weight(1f))
                    ActionButton(text = s.flashcardActionKnow, color = LexiColors.AccentGreen, onClick = { advance(true) }, modifier = Modifier.weight(1f))
                }
            }
        }
    }
}
