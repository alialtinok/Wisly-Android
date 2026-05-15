package com.alialtinok.wisly.ui.screens.my

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alialtinok.wisly.WislyApplication
import com.alialtinok.wisly.i18n.LocalAppStrings
import com.alialtinok.wisly.ui.theme.WislyColors

@Composable
fun MyScreen(
    onNavigateToFavorites: () -> Unit,
    onNavigateToUnknown: () -> Unit,
    onNavigateToMyWords: () -> Unit,
    onNavigateToSettings: () -> Unit,
) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as WislyApplication).container }
    val repo = container.wordRepository
    val strings = LocalAppStrings.current

    val favorites by repo.favoriteIds.collectAsState(initial = emptySet())
    val unknown by repo.unknownIds.collectAsState(initial = emptySet())
    val unknownPhrasals by repo.unknownPhrasalIds.collectAsState(initial = emptySet())
    val unknownIdioms by repo.unknownIdiomIds.collectAsState(initial = emptySet())
    val myWords by repo.myWords.collectAsState(initial = emptyList())
    val streak by repo.currentStreak.collectAsState(initial = 0)

    val reviewCount = unknown.size + unknownPhrasals.size + unknownIdioms.size
    val savedCount = favorites.size + myWords.size

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(WislyColors.Background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        item {
            YouHeader()
        }
        item {
            ProfileSummaryCard(
                streak = streak,
                saved = savedCount,
                review = reviewCount,
            )
        }
        item {
            MyWordsHeroRow(
                count = myWords.size,
                onClick = onNavigateToMyWords,
            )
        }
        item { SectionLabel("LIBRARY") }
        item {
            MyRow(
                icon = Icons.Filled.Favorite,
                color = WislyColors.C2,
                title = strings.myFavorites,
                count = favorites.size,
                subtitle = "Saved words",
                onClick = onNavigateToFavorites,
            )
        }
        item {
            MyRow(
                icon = Icons.Filled.ErrorOutline,
                color = WislyColors.B2,
                title = "Review Queue",
                count = reviewCount,
                subtitle = "Words, phrasal verbs and idioms",
                onClick = onNavigateToUnknown,
            )
        }
        item { SectionLabel(strings.settingsTitle.uppercase()) }
        item {
            MyRow(
                icon = Icons.Filled.Settings,
                color = WislyColors.OnSurfaceMuted,
                title = strings.mySettings,
                count = null,
                subtitle = strings.mySettingsDesc,
                onClick = onNavigateToSettings,
            )
        }
    }
}

@Composable
private fun YouHeader() {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                text = "You",
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
            )
            Text(
                text = "Your words, progress and preferences",
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White.copy(alpha = 0.54f),
            )
        }
        Box(
            modifier = Modifier
                .size(54.dp)
                .background(Color.White.copy(alpha = 0.06f), RoundedCornerShape(27.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = WislyColors.Primary,
                modifier = Modifier.size(30.dp),
            )
        }
    }
}

@Composable
private fun ProfileSummaryCard(
    streak: Int,
    saved: Int,
    review: Int,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WislyColors.Surface, RoundedCornerShape(24.dp))
            .border(1.dp, WislyColors.Primary.copy(alpha = 0.18f), RoundedCornerShape(24.dp))
            .padding(18.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(5.dp),
            ) {
                Text(
                    text = "Your learning space",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Black,
                    color = Color.White,
                )
                Text(
                    text = "Keep saved words and reviews close.",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium,
                    color = Color.White.copy(alpha = 0.52f),
                )
            }
            Icon(
                imageVector = Icons.Filled.Person,
                contentDescription = null,
                tint = WislyColors.Primary,
                modifier = Modifier.size(34.dp),
            )
        }
        Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            SummaryMetric(value = streak, label = "Streak", modifier = Modifier.weight(1f))
            SummaryMetric(value = saved, label = "Saved", modifier = Modifier.weight(1f))
            SummaryMetric(value = review, label = "Review", modifier = Modifier.weight(1f))
        }
    }
}

@Composable
private fun SummaryMetric(
    value: Int,
    label: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(Color.White.copy(alpha = 0.045f), RoundedCornerShape(14.dp))
            .padding(vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(value.toString(), fontSize = 20.sp, fontWeight = FontWeight.Black, color = Color.White)
        Text(label, fontSize = 11.sp, fontWeight = FontWeight.SemiBold, color = Color.White.copy(alpha = 0.45f))
    }
}

@Composable
private fun MyWordsHeroRow(
    count: Int,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(WislyColors.Surface, RoundedCornerShape(24.dp))
            .border(1.dp, WislyColors.AccentGreen.copy(alpha = 0.22f), RoundedCornerShape(24.dp))
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(64.dp)
                .background(WislyColors.AccentGreen.copy(alpha = 0.16f), RoundedCornerShape(18.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Filled.AddCircle,
                contentDescription = null,
                tint = WislyColors.AccentGreen,
                modifier = Modifier.size(30.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(5.dp),
        ) {
            Text(
                text = "My Words",
                fontSize = 20.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
            )
            Text(
                text = "Add your own words or import from Wisly News",
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                color = Color.White.copy(alpha = 0.52f),
                lineHeight = 18.sp,
            )
        }
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(count.toString(), fontSize = 20.sp, fontWeight = FontWeight.Black, color = WislyColors.AccentGreen)
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.30f),
                modifier = Modifier.size(18.dp),
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String) {
    Text(
        text = text,
        fontSize = 11.sp,
        fontWeight = FontWeight.SemiBold,
        color = WislyColors.OnSurfaceMuted,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
    )
}

@Composable
private fun MyRow(
    icon: ImageVector,
    color: Color,
    title: String,
    count: Int?,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(WislyColors.Surface, RoundedCornerShape(16.dp))
            .border(1.dp, WislyColors.SurfaceBorder, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .background(color.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(22.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = WislyColors.OnSurfaceMuted,
            )
        }
        if (count != null) {
            Text(
                text = count.toString(),
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = color,
                modifier = Modifier
                    .widthIn(min = 32.dp)
                    .background(color.copy(alpha = 0.15f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 10.dp, vertical = 5.dp),
            )
        }
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = WislyColors.OnSurfaceMuted,
            modifier = Modifier.size(20.dp),
        )
    }
}
