package com.alialtinok.lexiup.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddCircle
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Sync
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alialtinok.lexiup.ui.theme.LexiColors

private data class Feature(
    val icon: ImageVector,
    val tint: Color,
    val title: String,
    val description: String,
)

private val features = listOf(
    Feature(Icons.Filled.Style, LexiColors.Primary, "Flashcards", "Swipe to learn the 5,000 most used English words"),
    Feature(Icons.Filled.Bolt, LexiColors.AccentAmber, "Quiz & Practice", "Multiple choice, fill the blank, writing"),
    Feature(Icons.Filled.LocalFireDepartment, Color(0xFFFF6B00), "Daily Streaks", "Word of the day, track your progress"),
    Feature(Icons.Filled.Favorite, Color(0xFFF472B6), "Save Favorites", "Build your personal word list"),
    Feature(Icons.Filled.AddCircle, LexiColors.AccentGreen, "My Words", "Add your own words or sync from LexiNews automatically"),
)

@Composable
fun FeaturesPage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))

        Text(
            text = "Everything you need",
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "All the tools to build your vocabulary",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 15.sp,
        )

        Spacer(Modifier.height(28.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier.fillMaxWidth(),
        ) {
            features.forEach { feature ->
                FeatureRow(feature)
            }
            SyncHighlight()
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun FeatureRow(feature: Feature) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LexiColors.Surface, RoundedCornerShape(16.dp))
            .border(1.dp, LexiColors.SurfaceBorder, RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        IconBadge(feature.icon, feature.tint)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = feature.title,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                text = feature.description,
                color = LexiColors.OnSurfaceMuted,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun SyncHighlight() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LexiColors.Surface, RoundedCornerShape(16.dp))
            .border(1.5.dp, LexiColors.AccentGreen.copy(alpha = 0.35f), RoundedCornerShape(16.dp))
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        IconBadge(Icons.Filled.Sync, LexiColors.AccentGreen)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                Text(
                    text = "Syncs with LexiNews",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = "NEW",
                    fontSize = 9.sp,
                    fontWeight = FontWeight.Black,
                    color = LexiColors.AccentGreen,
                    modifier = Modifier
                        .background(
                            LexiColors.AccentGreen.copy(alpha = 0.2f),
                            RoundedCornerShape(50),
                        )
                        .padding(horizontal = 5.dp, vertical = 2.dp),
                )
            }
            Text(
                text = "Words you save in LexiNews appear in My Words automatically",
                color = LexiColors.OnSurfaceMuted,
                fontSize = 13.sp,
            )
        }
    }
}

@Composable
private fun IconBadge(icon: ImageVector, tint: Color) {
    Box(
        modifier = Modifier
            .size(52.dp)
            .background(tint.copy(alpha = 0.15f), RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(22.dp),
        )
    }
}
