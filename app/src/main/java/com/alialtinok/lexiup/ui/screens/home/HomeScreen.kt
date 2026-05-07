package com.alialtinok.lexiup.ui.screens.home

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.MenuBook
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FormatQuote
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alialtinok.lexiup.WislyApplication
import com.alialtinok.lexiup.data.model.Word
import com.alialtinok.lexiup.data.repository.WordRepository
import com.alialtinok.lexiup.i18n.LocalAppStrings
import com.alialtinok.lexiup.ui.theme.LexiColors

@Composable
fun HomeScreen(
    onNavigateToWordsList: () -> Unit,
    onNavigateToPhrasalList: () -> Unit,
    onNavigateToIdiomsList: () -> Unit,
) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as WislyApplication).container }
    val repo = container.wordRepository

    val streak by repo.currentStreak.collectAsState(initial = 0)
    val favorites by repo.favoriteIds.collectAsState(initial = emptySet())
    val unknown by repo.unknownIds.collectAsState(initial = emptySet())

    val word = remember { repo.wordOfTheDay }

    LaunchedEffect(Unit) { repo.recordActivity() }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LexiColors.Background),
        contentPadding = androidx.compose.foundation.layout.PaddingValues(
            top = 8.dp,
            bottom = 32.dp,
        ),
        verticalArrangement = Arrangement.spacedBy(24.dp),
    ) {
        item { Header() }
        item {
            StreakHeroCard(
                streak = streak,
                favorites = favorites.size,
                unknown = unknown.size,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
        if (word != null) {
            item {
                WordOfTheDayCard(
                    word = word,
                    streak = streak,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
        item {
            ContentSection(
                wordCount = repo.allWords.size,
                phrasalCount = repo.allPhrasalVerbs.size,
                idiomCount = repo.allIdioms.size,
                onWordsClick = onNavigateToWordsList,
                onPhrasalClick = onNavigateToPhrasalList,
                onIdiomsClick = onNavigateToIdiomsList,
                modifier = Modifier.padding(horizontal = 20.dp),
            )
        }
    }
}

@Composable
private fun Header() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.Bottom, modifier = Modifier.weight(1f)) {
            Text(
                text = "Wis",
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
            Text(
                text = "ly",
                fontSize = 38.sp,
                fontWeight = FontWeight.ExtraBold,
                style = TextStyle(
                    brush = Brush.horizontalGradient(
                        colors = listOf(LexiColors.Primary, LexiColors.AccentPurple),
                    ),
                ),
            )
        }
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            LexiColors.Primary.copy(alpha = 0.25f),
                            LexiColors.AccentPurple.copy(alpha = 0.25f),
                        ),
                    ),
                    shape = RoundedCornerShape(16.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Style,
                contentDescription = null,
                tint = LexiColors.Primary,
                modifier = Modifier.size(26.dp),
            )
        }
    }
}

@Composable
private fun StreakHeroCard(
    streak: Int,
    favorites: Int,
    unknown: Int,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFFF6B00).copy(alpha = 0.18f),
                        Color(0xFF1A1D2E),
                    ),
                ),
                shape = RoundedCornerShape(24.dp),
            )
            .border(1.dp, Color(0xFFFF6B00).copy(alpha = 0.25f), RoundedCornerShape(24.dp))
            .padding(vertical = 20.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            StreakColumn(streak = streak, modifier = Modifier.weight(1f))
            StatDivider()
            StatColumn(value = favorites.toString(), label = strings.statFavorites, modifier = Modifier.weight(1f))
            StatDivider()
            StatColumn(value = unknown.toString(), label = strings.statToReview, modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun StreakColumn(streak: Int, modifier: Modifier = Modifier) {
    val strings = LocalAppStrings.current
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = streak.toString(),
                fontSize = 48.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
            )
            Spacer(Modifier.width(4.dp))
            Text(text = "🔥", fontSize = 28.sp)
        }
        Text(
            text = strings.dayStreak,
            color = Color(0xFFFF6B00),
            fontSize = 12.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun StatColumn(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = value,
            fontSize = 28.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
        )
        Text(
            text = label,
            color = LexiColors.OnSurfaceMuted,
            fontSize = 11.sp,
        )
    }
}

@Composable
private fun StatDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(50.dp)
            .background(Color.White.copy(alpha = 0.1f)),
    )
}

@Composable
private fun WordOfTheDayCard(
    word: Word,
    streak: Int,
    modifier: Modifier = Modifier,
) {
    val levelColor = parseHex(word.cefrColor)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF1A1D2E), Color(0xFF12152A)),
                ),
                shape = RoundedCornerShape(24.dp),
            )
            .border(1.dp, levelColor.copy(alpha = 0.30f), RoundedCornerShape(24.dp)),
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.AutoAwesome,
                    contentDescription = null,
                    tint = levelColor,
                    modifier = Modifier.size(14.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = LocalAppStrings.current.wordOfTheDay,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = levelColor,
                    letterSpacing = 0.8.sp,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = word.level,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = levelColor,
                    modifier = Modifier
                        .background(levelColor.copy(alpha = 0.15f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 4.dp),
                )
            }

            Text(
                text = word.word,
                fontSize = 34.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
            )

            Text(
                text = word.type.replaceFirstChar { it.uppercase() },
                fontSize = 12.sp,
                color = LexiColors.OnSurfaceMuted,
                fontWeight = FontWeight.Medium,
            )

            Text(
                text = word.turkish,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = levelColor,
            )

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(1.dp)
                    .background(Color.White.copy(alpha = 0.08f)),
            )

            Text(
                text = word.example,
                fontSize = 14.sp,
                color = Color.White.copy(alpha = 0.65f),
                lineHeight = 20.sp,
            )
        }

        if (streak > 0) {
            Row(
                modifier = Modifier
                    .align(Alignment.TopEnd)
                    .padding(16.dp)
                    .background(Color(0xFFFF6B00).copy(alpha = 0.85f), CircleShape)
                    .padding(horizontal = 10.dp, vertical = 5.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(text = "🔥", fontSize = 13.sp)
                Spacer(Modifier.width(4.dp))
                Text(
                    text = streak.toString(),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                )
            }
        }
    }
}

@Composable
private fun ContentSection(
    wordCount: Int,
    phrasalCount: Int,
    idiomCount: Int,
    onWordsClick: () -> Unit,
    onPhrasalClick: () -> Unit,
    onIdiomsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(
            text = strings.sectionContent,
            fontSize = 11.sp,
            fontWeight = FontWeight.SemiBold,
            color = LexiColors.OnSurfaceMuted,
            letterSpacing = 1.sp,
        )
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            ContentCard(
                icon = Icons.AutoMirrored.Filled.MenuBook,
                gradient = listOf(Color(0xFF1A3A6B), Color(0xFF0F2244)),
                accent = LexiColors.Primary,
                title = strings.contentWords,
                value = "$wordCount ${strings.unitWords}",
                onClick = onWordsClick,
            )
            ContentCard(
                icon = Icons.Filled.Style,
                gradient = listOf(Color(0xFF3A1A6B), Color(0xFF220F44)),
                accent = LexiColors.AccentPurple,
                title = strings.contentPhrasal,
                value = "$phrasalCount ${strings.unitVerbs}",
                onClick = onPhrasalClick,
            )
            ContentCard(
                icon = Icons.Filled.FormatQuote,
                gradient = listOf(Color(0xFF5A3600), Color(0xFF3A2200)),
                accent = LexiColors.AccentAmber,
                title = strings.contentIdioms,
                value = "$idiomCount ${strings.unitIdioms}",
                onClick = onIdiomsClick,
            )
        }
    }
}

@Composable
private fun ContentCard(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    gradient: List<Color>,
    accent: Color,
    title: String,
    value: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(brush = Brush.linearGradient(gradient), shape = RoundedCornerShape(18.dp))
            .border(1.dp, accent.copy(alpha = 0.20f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .background(accent.copy(alpha = 0.20f), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accent,
                modifier = Modifier.size(20.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                text = value,
                fontSize = 12.sp,
                color = accent,
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.25f),
            modifier = Modifier.size(20.dp),
        )
    }
}

private fun parseHex(hex: String): Color {
    val clean = hex.removePrefix("#")
    val value = clean.toLong(16) or 0xFF000000
    return Color(value)
}
