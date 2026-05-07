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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alialtinok.lexiup.ui.theme.LexiColors

@Composable
fun WelcomePage() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))

        AppBadge()

        Spacer(Modifier.height(36.dp))

        Row(verticalAlignment = Alignment.Bottom) {
            Text(
                text = "Wis",
                fontSize = 52.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White,
            )
            Text(
                text = "ly",
                fontSize = 52.sp,
                fontWeight = FontWeight.ExtraBold,
                style = TextStyle(
                    brush = Brush.horizontalGradient(
                        colors = listOf(LexiColors.Primary, LexiColors.AccentPurple),
                    ),
                ),
            )
        }
        Spacer(Modifier.height(6.dp))
        Text(
            text = "Master English vocabulary\nat your own pace",
            color = Color.White.copy(alpha = 0.55f),
            fontSize = 17.sp,
            textAlign = TextAlign.Center,
            lineHeight = 24.sp,
        )

        Spacer(Modifier.height(48.dp))

        StatsRow()

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun AppBadge() {
    Box(
        modifier = Modifier
            .size(120.dp)
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(Color(0xFF1A1D2E), LexiColors.Background),
                ),
                shape = RoundedCornerShape(32.dp),
            )
            .border(
                width = 1.5.dp,
                brush = Brush.linearGradient(
                    colors = listOf(
                        LexiColors.Primary.copy(alpha = 0.6f),
                        LexiColors.AccentPurple.copy(alpha = 0.4f),
                    ),
                ),
                shape = RoundedCornerShape(32.dp),
            ),
        contentAlignment = Alignment.Center,
    ) {
        Icon(
            imageVector = Icons.Filled.Style,
            contentDescription = null,
            tint = LexiColors.Primary,
            modifier = Modifier.size(54.dp),
        )
    }
}

@Composable
private fun StatsRow() {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LexiColors.Surface, RoundedCornerShape(20.dp))
            .border(1.dp, LexiColors.SurfaceBorder, RoundedCornerShape(20.dp))
            .padding(vertical = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceEvenly,
    ) {
        StatBadge("5,000", "Words", Modifier.weight(1f))
        VerticalDivider()
        StatBadge("A1–C1", "CEFR Levels", Modifier.weight(1f))
        VerticalDivider()
        StatBadge("6+", "Languages", Modifier.weight(1f))
    }
}

@Composable
private fun StatBadge(value: String, label: String, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            color = Color.White,
            fontSize = 20.sp,
            fontWeight = FontWeight.Black,
        )
        Spacer(Modifier.height(3.dp))
        Text(
            text = label,
            color = LexiColors.OnSurfaceMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Medium,
        )
    }
}

@Composable
private fun VerticalDivider() {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(36.dp)
            .background(Color.White.copy(alpha = 0.1f)),
    )
}
