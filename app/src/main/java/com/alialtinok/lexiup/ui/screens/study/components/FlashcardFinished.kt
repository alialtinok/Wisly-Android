package com.alialtinok.lexiup.ui.screens.study.components

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alialtinok.lexiup.i18n.LocalAppStrings
import com.alialtinok.lexiup.ui.theme.LexiColors

@Composable
fun FlashcardFinished(
    known: Int,
    unknown: Int,
    onRestart: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val s = LocalAppStrings.current
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = "🎉", fontSize = 64.sp)
        Spacer(Modifier.height(20.dp))
        Text(
            text = s.flashcardCongrats,
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = s.flashcardSessionDone,
            fontSize = 14.sp,
            color = LexiColors.OnSurfaceMuted,
        )
        Spacer(Modifier.height(20.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            StatBox(
                number = known,
                label = s.flashcardKnewIt,
                color = LexiColors.AccentGreen,
                modifier = Modifier.weight(1f),
            )
            StatBox(
                number = unknown,
                label = s.flashcardDidntKnow,
                color = LexiColors.AccentRed,
                modifier = Modifier.weight(1f),
            )
        }
        Spacer(Modifier.height(24.dp))
        Box(
            modifier = Modifier
                .background(LexiColors.Primary, RoundedCornerShape(16.dp))
                .clickable(onClick = onRestart)
                .padding(horizontal = 40.dp, vertical = 16.dp),
        ) {
            Text(
                text = s.quizRestart,
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
        }
    }
}

@Composable
private fun StatBox(
    number: Int,
    label: String,
    color: Color,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(LexiColors.Surface, RoundedCornerShape(16.dp))
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(16.dp))
            .padding(vertical = 20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Text(
            text = number.toString(),
            fontSize = 36.sp,
            fontWeight = FontWeight.Black,
            color = color,
        )
        Text(
            text = label,
            fontSize = 13.sp,
            color = LexiColors.OnSurfaceMuted,
        )
    }
}
