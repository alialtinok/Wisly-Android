package com.alialtinok.lexiup.ui.screens.practice.components

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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alialtinok.lexiup.i18n.LocalAppStrings
import com.alialtinok.lexiup.ui.theme.LexiColors
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

data class ReviewItem(
    val front: String,
    val back: String,
    val example: String,
)

@Composable
fun QuizReviewSheet(
    items: List<ReviewItem>,
    accentColor: Color = LexiColors.AccentRed,
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
            if (currentIndex + 1 < items.size) currentIndex++ else isDone = true
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
                    tint = LexiColors.AccentAmber,
                    modifier = Modifier.size(64.dp),
                )
                Text(s.reviewComplete, fontSize = 24.sp, fontWeight = FontWeight.Black, color = Color.White)
                Text(
                    text = "${items.size} ${s.reviewedWrongSuffix}",
                    fontSize = 14.sp,
                    color = LexiColors.OnSurfaceMuted,
                    textAlign = TextAlign.Center,
                )
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LexiColors.Primary, RoundedCornerShape(14.dp))
                        .clickable(onClick = onRestart)
                        .padding(vertical = 16.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(s.startNewSession, fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Spacer(Modifier.height(8.dp))
            }
        } else {
            val item = items[currentIndex]

            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                    Text(s.reviewWrongAnswers, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LexiColors.OnSurfaceMuted)
                    Text("${currentIndex + 1} / ${items.size}", fontSize = 15.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
                Icon(Icons.Filled.Cancel, null, tint = LexiColors.AccentRed, modifier = Modifier.size(28.dp))
            }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(LexiColors.SurfaceBorder, RoundedCornerShape(2.dp)),
            ) {
                val progress by animateFloatAsState(currentIndex.toFloat() / items.size, label = "review-progress")
                Box(
                    modifier = Modifier
                        .fillMaxWidth(progress.coerceIn(0f, 1f))
                        .fillMaxHeight()
                        .background(accentColor, RoundedCornerShape(2.dp)),
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
                    .background(LexiColors.Surface, RoundedCornerShape(24.dp))
                    .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(24.dp))
                    .padding(28.dp),
                contentAlignment = Alignment.Center,
            ) {
                if (flipRotation <= 90f) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                    ) {
                        Text(
                            text = item.front,
                            fontSize = 26.sp,
                            fontWeight = FontWeight.Black,
                            color = LexiColors.Primary,
                            textAlign = TextAlign.Center,
                        )
                        Text(s.reviewTapToFlip, fontSize = 11.sp, color = LexiColors.OnSurfaceMuted)
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
                            color = LexiColors.AccentGreen,
                            letterSpacing = 2.sp,
                        )
                        Text(
                            text = item.back,
                            fontSize = 28.sp,
                            fontWeight = FontWeight.Black,
                            color = LexiColors.AccentGreen,
                            textAlign = TextAlign.Center,
                        )
                        if (item.example.isNotBlank()) {
                            Text(
                                text = item.example,
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
                color = LexiColors.OnSurfaceMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp),
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LexiColors.Primary, RoundedCornerShape(14.dp))
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
