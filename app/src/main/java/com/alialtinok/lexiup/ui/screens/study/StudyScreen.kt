package com.alialtinok.lexiup.ui.screens.study

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
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alialtinok.lexiup.i18n.LocalAppStrings
import com.alialtinok.lexiup.ui.theme.LexiColors

@Composable
fun StudyScreen(
    onNavigateToFlashcards: () -> Unit,
    onNavigateToQuiz: () -> Unit,
) {
    val strings = LocalAppStrings.current
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(LexiColors.Background),
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(top = 8.dp, bottom = 32.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp),
        ) {
            item {
                Text(
                    text = strings.studyTitle,
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White,
                    modifier = Modifier
                        .statusBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 8.dp),
                )
            }
            item {
                StudyCard(
                    title = strings.studyFlashcards,
                    subtitle = strings.studyFlashcardsDesc,
                    icon = Icons.Filled.Style,
                    gradient = listOf(Color(0xFF1A3A6B), Color(0xFF0F2244)),
                    accent = LexiColors.Primary,
                    onClick = onNavigateToFlashcards,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
            item {
                StudyCard(
                    title = strings.studyQuiz,
                    subtitle = strings.studyQuizDesc,
                    icon = Icons.Filled.Quiz,
                    gradient = listOf(Color(0xFF3A1A6B), Color(0xFF220F44)),
                    accent = LexiColors.AccentPurple,
                    onClick = onNavigateToQuiz,
                    modifier = Modifier.padding(horizontal = 20.dp),
                )
            }
        }
    }
}

@Composable
private fun StudyCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    gradient: List<Color>,
    accent: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(
                brush = Brush.linearGradient(gradient),
                shape = RoundedCornerShape(20.dp),
            )
            .border(1.dp, accent.copy(alpha = if (enabled) 0.25f else 0.10f), RoundedCornerShape(20.dp))
            .let { if (enabled) it.clickable(onClick = onClick) else it }
            .padding(20.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Box(
            modifier = Modifier
                .size(56.dp)
                .background(
                    accent.copy(alpha = if (enabled) 0.20f else 0.10f),
                    RoundedCornerShape(14.dp),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) accent else accent.copy(alpha = 0.5f),
                modifier = Modifier.size(26.dp),
            )
        }
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = title,
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold,
                color = if (enabled) Color.White else Color.White.copy(alpha = 0.5f),
            )
            Text(
                text = subtitle,
                fontSize = 12.sp,
                color = if (enabled) accent else accent.copy(alpha = 0.5f),
            )
        }
        if (enabled) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.30f),
                modifier = Modifier.size(22.dp),
            )
        }
    }
}
