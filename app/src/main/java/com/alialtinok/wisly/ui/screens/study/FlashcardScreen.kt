package com.alialtinok.wisly.ui.screens.study

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Shuffle
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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alialtinok.wisly.WislyApplication
import com.alialtinok.wisly.data.model.Word
import com.alialtinok.wisly.i18n.LocalAppStrings
import com.alialtinok.wisly.ui.screens.study.components.FlashcardBack
import com.alialtinok.wisly.ui.screens.study.components.FlashcardFinished
import com.alialtinok.wisly.ui.screens.study.components.FlashcardFront
import com.alialtinok.wisly.ui.screens.study.components.LevelOption
import com.alialtinok.wisly.ui.screens.study.components.LevelPickerContent
import com.alialtinok.wisly.ui.theme.WislyColors
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private val CefrLevels = listOf("A1", "A2", "B1", "B2", "C1")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashcardScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as WislyApplication).container }
    val repo = container.wordRepository
    val scope = rememberCoroutineScope()

    val translationRepo = container.translationRepository
    val nativeLanguage by container.userSettingsRepository.nativeLanguage.collectAsState(initial = null)
    val preferredLevel by container.userSettingsRepository.preferredLevel.collectAsState(initial = null)
    val favoriteIds by repo.favoriteIds.collectAsState(initial = emptySet())

    var selectedLevel by remember(preferredLevel) { mutableStateOf(preferredLevel) }
    var isShuffled by remember { mutableStateOf(false) }
    var sessionKey by remember { mutableIntStateOf(0) }
    var showLevelPicker by remember { mutableStateOf(false) }

    val sessionWords = remember(selectedLevel, isShuffled, sessionKey) {
        val base = if (selectedLevel == null) repo.allWords
        else repo.allWords.filter { it.level == selectedLevel }
        if (isShuffled) base.shuffled() else base
    }

    var currentIndex by remember(sessionKey) { mutableIntStateOf(0) }
    var knownCount by remember(sessionKey) { mutableIntStateOf(0) }
    var unknownCount by remember(sessionKey) { mutableIntStateOf(0) }
    var isFlipped by remember(sessionKey) { mutableStateOf(false) }
    var isFinished by remember(sessionKey) { mutableStateOf(false) }
    var isAnimating by remember(sessionKey) { mutableStateOf(false) }

    val tts = container.ttsManager

    var translation by remember { mutableStateOf<String?>(null) }

    LaunchedEffect(currentIndex, nativeLanguage, sessionKey) {
        val word = sessionWords.getOrNull(currentIndex) ?: return@LaunchedEffect
        val lang = nativeLanguage
        if (lang == null || lang.id == "tr") {
            translation = word.turkish
        } else {
            translation = null  // "…" göster, fetch bitene kadar eski kart çevirisi kalmasın
            translation = word.bundledTranslation(lang)
                ?: translationRepo.fetch(word.word, lang.translationCode)
        }
    }

    val offsetX = remember(sessionKey) { Animatable(0f) }
    val flipAnim = remember(sessionKey) { Animatable(0f) }

    fun resetSession() {
        sessionKey++
    }

    fun advance(known: Boolean) {
        if (isAnimating || isFinished || sessionWords.isEmpty()) return
        isAnimating = true
        scope.launch {
            val target = if (known) 1500f else -1500f
            offsetX.animateTo(target, tween(durationMillis = 220))
            if (known) {
                knownCount++
            } else {
                unknownCount++
                repo.markUnknown(sessionWords[currentIndex].id)
            }
            if (currentIndex + 1 >= sessionWords.size) {
                isFinished = true
            } else {
                currentIndex++
            }
            isFlipped = false
            flipAnim.snapTo(0f)
            offsetX.snapTo(0f)
            isAnimating = false
        }
    }

    LaunchedEffect(sessionWords) {
        if (currentIndex >= sessionWords.size) currentIndex = 0
    }

    LaunchedEffect(currentIndex, sessionKey) {
        if (sessionWords.isNotEmpty() && !isFinished) {
            tts.speak(sessionWords[currentIndex].word)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WislyColors.Background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            FlashcardTopBar(
                onBack = onBack,
                isShuffled = isShuffled,
                onToggleShuffle = {
                    isShuffled = !isShuffled
                    resetSession()
                },
                selectedLevel = selectedLevel,
                onOpenLevelPicker = { showLevelPicker = true },
            )

            if (sessionWords.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = LocalAppStrings.current.flashcardNoWords,
                        color = WislyColors.OnSurfaceMuted,
                    )
                }
            } else if (isFinished) {
                FlashcardFinished(
                    known = knownCount,
                    unknown = unknownCount,
                    onRestart = { resetSession() },
                )
            } else {
                val total = sessionWords.size
                val progress = (knownCount + unknownCount) / total.toFloat()
                ProgressBar(
                    progress = progress,
                    modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp),
                )

                Text(
                    text = "${currentIndex + 1} / $total",
                    fontSize = 14.sp,
                    color = WislyColors.OnSurfaceMuted,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )

                Spacer(Modifier.weight(1f))

                val word = sessionWords[currentIndex]
                FlashcardCard(
                    word = word,
                    translation = translation,
                    showExampleTranslation = nativeLanguage == null || nativeLanguage?.id == "tr",
                    flipRotation = flipAnim.value,
                    onFlip = {
                        if (!isAnimating) {
                            isFlipped = !isFlipped
                            scope.launch { flipAnim.animateTo(if (isFlipped) 180f else 0f, tween(480)) }
                        }
                    },
                    isFavorite = word.id in favoriteIds,
                    onToggleFavorite = { scope.launch { repo.toggleFavorite(word.id) } },
                    onSpeak = { tts.speak(word.word) },
                    onSpeakTranslation = {
                        val t = translation ?: return@FlashcardCard
                        val lang = nativeLanguage ?: return@FlashcardCard
                        tts.speakWithLocale(t, lang.speechCode)
                    },
                    offsetX = offsetX.value,
                    onDragX = { delta ->
                        if (isAnimating) return@FlashcardCard
                        scope.launch { offsetX.snapTo(offsetX.value + delta) }
                    },
                    onDragEnd = {
                        if (isAnimating) return@FlashcardCard
                        val o = offsetX.value
                        when {
                            o > 250f -> advance(known = true)
                            o < -250f -> advance(known = false)
                            else -> scope.launch {
                                offsetX.animateTo(0f, tween(200))
                            }
                        }
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp)
                        .height(440.dp),
                )

                Spacer(Modifier.weight(1f))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                ) {
                    val s = LocalAppStrings.current
                    ActionButton(
                        text = s.flashcardActionDontKnow,
                        color = WislyColors.AccentRed,
                        style = FlashcardActionStyle.Review,
                        onClick = { advance(known = false) },
                        modifier = Modifier.weight(1f),
                    )
                    ActionButton(
                        text = s.flashcardActionKnow,
                        color = WislyColors.AccentGreen,
                        style = FlashcardActionStyle.Mastered,
                        onClick = { advance(known = true) },
                        modifier = Modifier.weight(1f),
                    )
                }

                Text(
                    text = LocalAppStrings.current.flashcardSwipeHint,
                    fontSize = 11.sp,
                    color = WislyColors.OnSurfaceMuted.copy(alpha = 0.7f),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 24.dp),
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                )
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
                    scope.launch { container.userSettingsRepository.setPreferredLevel(id) }
                    showLevelPicker = false
                    resetSession()
                },
            )
        }
    }
}

@Composable
private fun FlashcardTopBar(
    onBack: () -> Unit,
    isShuffled: Boolean,
    onToggleShuffle: () -> Unit,
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
        IconButton(onClick = onToggleShuffle) {
            Icon(
                imageVector = Icons.Filled.Shuffle,
                contentDescription = s.flashcardShuffle,
                tint = if (isShuffled) WislyColors.AccentGreen else WislyColors.OnSurfaceMuted,
            )
        }
        Text(
            text = s.flashcardTitle,
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
internal fun ProgressBar(progress: Float, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(WislyColors.SurfaceBorder, RoundedCornerShape(2.dp)),
    ) {
        val animated by animateFloatAsState(targetValue = progress, label = "progress")
        Box(
            modifier = Modifier
                .fillMaxWidth(animated.coerceIn(0f, 1f))
                .height(4.dp)
                .background(WislyColors.Primary, RoundedCornerShape(2.dp)),
        )
    }
}

@Composable
private fun FlashcardCard(
    word: Word,
    translation: String?,
    showExampleTranslation: Boolean,
    flipRotation: Float,
    onFlip: () -> Unit,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    onSpeak: () -> Unit,
    onSpeakTranslation: () -> Unit,
    offsetX: Float,
    onDragX: (Float) -> Unit,
    onDragEnd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val rotation = flipRotation
    val density = LocalDensity.current
    val swipeColor = when {
        offsetX > 0 -> WislyColors.AccentGreen.copy(alpha = (abs(offsetX) / 600f).coerceAtMost(0.30f))
        offsetX < 0 -> WislyColors.AccentRed.copy(alpha = (abs(offsetX) / 600f).coerceAtMost(0.30f))
        else -> Color.Transparent
    }

    val swipeRotation = (offsetX / 25f).coerceIn(-15f, 15f)

    Box(
        modifier = modifier
            .offset { IntOffset(offsetX.roundToInt(), 0) }
            .graphicsLayer {
                rotationZ = swipeRotation
            }
            .pointerInput(word.id) {
                detectTapGestures(onTap = { onFlip() })
            }
            .pointerInput(word.id) {
                detectDragGestures(
                    onDragEnd = { onDragEnd() },
                    onDragCancel = { onDragEnd() },
                    onDrag = { change, dragAmount ->
                        change.consume()
                        onDragX(dragAmount.x)
                    },
                )
            },
    ) {
        // Front face — visible while rotation < 90
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = rotation
                    cameraDistance = 16f * density.density
                    alpha = if (rotation < 90f) 1f else 0f
                },
        ) {
            FlashcardFront(
                word = word,
                isFavorite = isFavorite,
                onToggleFavorite = onToggleFavorite,
                onSpeak = onSpeak,
                swipeColor = swipeColor,
            )
        }
        // Back face — visible while rotation >= 90; pre-rotated 180 so text reads correctly
        Box(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    rotationY = rotation - 180f
                    cameraDistance = 16f * density.density
                    alpha = if (rotation >= 90f) 1f else 0f
                },
        ) {
            FlashcardBack(
                word = word,
                translation = translation,
                showExampleTranslation = showExampleTranslation,
                onSpeak = onSpeakTranslation,
                swipeColor = swipeColor,
            )
        }
    }
}

internal enum class FlashcardActionStyle {
    Review,
    Mastered,
}

@Composable
internal fun ActionButton(
    text: String,
    color: Color,
    style: FlashcardActionStyle,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isPrimary = style == FlashcardActionStyle.Mastered
    val shape = RoundedCornerShape(18.dp)
    val icon: ImageVector = if (isPrimary) Icons.Filled.CheckCircle else Icons.Filled.Close
    val foreground = if (isPrimary) Color(0xFF07140F) else color
    val backgroundModifier = if (isPrimary) {
        Modifier.background(
            brush = Brush.linearGradient(
                listOf(color, Color(0xFF6EE7B7)),
            ),
            shape = shape,
        )
    } else {
        Modifier.background(color.copy(alpha = 0.08f), shape)
    }

    Row(
        modifier = modifier
            .height(58.dp)
            .then(if (isPrimary) Modifier.shadow(10.dp, shape, clip = false) else Modifier)
            .then(backgroundModifier)
            .border(
                width = 1.dp,
                color = if (isPrimary) Color.White.copy(alpha = 0.18f) else color.copy(alpha = 0.55f),
                shape = shape,
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = foreground,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(8.dp))
        Text(
            text = text,
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            color = foreground,
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
