package com.alialtinok.wisly.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alialtinok.wisly.ui.theme.WislyColors

private data class OnboardingLevel(
    val id: String?,
    val title: String,
    val subtitle: String,
    val color: Color,
)

private val onboardingLevels = listOf(
    OnboardingLevel(null, "All", "Mixed levels", WislyColors.Primary),
    OnboardingLevel("A1", "A1", "Beginner", WislyColors.A1),
    OnboardingLevel("A2", "A2", "Elementary", WislyColors.A2),
    OnboardingLevel("B1", "B1", "Intermediate", WislyColors.B1),
    OnboardingLevel("B2", "B2", "Upper intermediate", WislyColors.B2),
    OnboardingLevel("C1", "C1", "Advanced", WislyColors.C1),
)

@Composable
fun LevelPage(
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))

        Text(
            text = "Choose your level",
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "Wisly will prioritize words\nat this CEFR level",
            color = Color.White.copy(alpha = 0.5f),
            fontSize = 15.sp,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp,
        )

        Spacer(Modifier.height(28.dp))

        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(horizontal = 24.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .weight(2f, fill = false),
        ) {
            items(onboardingLevels) { level ->
                LevelCard(
                    level = level,
                    isSelected = selected == level.id,
                    onClick = { onSelect(level.id) },
                )
            }
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun LevelCard(
    level: OnboardingLevel,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) level.color.copy(alpha = 0.75f) else WislyColors.SurfaceBorder
    val containerColor = if (isSelected) level.color.copy(alpha = 0.14f) else WislyColors.Surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp),
        ) {
            Text(
                text = level.title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Black,
                color = if (isSelected) level.color else Color.White,
            )
            Text(
                text = level.subtitle,
                fontSize = 11.sp,
                color = WislyColors.OnSurfaceMuted,
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = level.color,
            )
        }
    }
}
