package com.alialtinok.lexiup.ui.screens.practice.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Cancel
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alialtinok.lexiup.ui.theme.LexiColors

enum class QuizOptionState { Neutral, Correct, Wrong, Dimmed }

@Composable
fun QuizScoreRow(
    correct: Int,
    wrong: Int,
    qNumber: Int,
    sessionSize: Int,
    accent: Color = LexiColors.Primary,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = LexiColors.AccentGreen,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = correct.toString(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = LexiColors.AccentGreen,
            )
        }
        Spacer(Modifier.width(16.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = Icons.Filled.Cancel,
                contentDescription = null,
                tint = LexiColors.AccentRed,
                modifier = Modifier.size(16.dp),
            )
            Spacer(Modifier.width(4.dp))
            Text(
                text = wrong.toString(),
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = LexiColors.AccentRed,
            )
        }
        Spacer(Modifier.weight(1f))
        Text(
            text = "$qNumber/$sessionSize",
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
            color = LexiColors.OnSurfaceMuted,
        )
        if (correct + wrong > 0) {
            val pct = (correct.toDouble() / (correct + wrong) * 100).toInt()
            Spacer(Modifier.width(10.dp))
            Text(
                text = "%$pct",
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = accent,
            )
        }
    }
}

@Composable
fun QuizProgressBar(
    progress: Float,
    color: Color = LexiColors.Primary,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(4.dp)
            .background(LexiColors.SurfaceBorder, RoundedCornerShape(2.dp)),
    ) {
        val animated by animateFloatAsState(targetValue = progress, label = "quiz-progress")
        Box(
            modifier = Modifier
                .fillMaxWidth(animated.coerceIn(0f, 1f))
                .height(4.dp)
                .background(color, RoundedCornerShape(2.dp)),
        )
    }
}

@Composable
fun QuizOption(
    text: String,
    state: QuizOptionState,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val bg = when (state) {
        QuizOptionState.Neutral -> LexiColors.Surface
        QuizOptionState.Correct -> LexiColors.AccentGreen.copy(alpha = 0.15f)
        QuizOptionState.Wrong -> LexiColors.AccentRed.copy(alpha = 0.15f)
        QuizOptionState.Dimmed -> LexiColors.Surface.copy(alpha = 0.5f)
    }
    val border = when (state) {
        QuizOptionState.Neutral -> LexiColors.SurfaceBorder
        QuizOptionState.Correct -> LexiColors.AccentGreen
        QuizOptionState.Wrong -> LexiColors.AccentRed
        QuizOptionState.Dimmed -> LexiColors.SurfaceBorder.copy(alpha = 0.4f)
    }
    val textColor = when (state) {
        QuizOptionState.Neutral -> Color.White
        QuizOptionState.Correct -> LexiColors.AccentGreen
        QuizOptionState.Wrong -> LexiColors.AccentRed
        QuizOptionState.Dimmed -> LexiColors.OnSurfaceMuted.copy(alpha = 0.6f)
    }

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(14.dp))
            .border(1.dp, border, RoundedCornerShape(14.dp))
            .clickable(enabled = state == QuizOptionState.Neutral, onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = text,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            color = textColor,
            modifier = Modifier.weight(1f),
        )
        when (state) {
            QuizOptionState.Correct -> Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = LexiColors.AccentGreen,
            )
            QuizOptionState.Wrong -> Icon(
                imageVector = Icons.Filled.Cancel,
                contentDescription = null,
                tint = LexiColors.AccentRed,
            )
            else -> Unit
        }
    }
}
