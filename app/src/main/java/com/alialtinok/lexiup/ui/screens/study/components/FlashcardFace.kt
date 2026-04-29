package com.alialtinok.lexiup.ui.screens.study.components

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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alialtinok.lexiup.data.model.Word
import com.alialtinok.lexiup.ui.theme.LexiColors

@Composable
fun FlashcardFront(
    word: Word,
    isFavorite: Boolean,
    onToggleFavorite: () -> Unit,
    swipeColor: Color,
    modifier: Modifier = Modifier,
) {
    val levelColor = parseHex(word.cefrColor)
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LexiColors.Surface, RoundedCornerShape(24.dp))
            .background(swipeColor, RoundedCornerShape(24.dp))
            .border(1.dp, LexiColors.SurfaceBorder, RoundedCornerShape(24.dp))
            .padding(24.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onToggleFavorite) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                        contentDescription = "Favorite",
                        tint = if (isFavorite) LexiColors.C2 else LexiColors.OnSurfaceMuted,
                    )
                }
                Spacer(Modifier.weight(1f))
                Text(
                    text = word.level,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Black,
                    color = levelColor,
                    modifier = Modifier
                        .background(levelColor.copy(alpha = 0.15f), CircleShape)
                        .padding(horizontal = 10.dp, vertical = 5.dp),
                )
            }

            Spacer(Modifier.weight(1f))

            Text(
                text = word.type.replaceFirstChar { it.uppercase() },
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                color = LexiColors.OnSurfaceMuted,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                textAlign = TextAlign.Center,
            )

            Text(
                text = word.word,
                fontSize = 42.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.weight(1f))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = Icons.Filled.TouchApp,
                    contentDescription = null,
                    tint = LexiColors.OnSurfaceMuted,
                    modifier = Modifier.width(14.dp).height(14.dp),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "Tap for translation",
                    fontSize = 12.sp,
                    color = LexiColors.OnSurfaceMuted,
                )
            }
        }
    }
}

@Composable
fun FlashcardBack(
    word: Word,
    swipeColor: Color,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF1E2D4A), RoundedCornerShape(24.dp))
            .background(swipeColor, RoundedCornerShape(24.dp))
            .border(1.dp, LexiColors.Primary.copy(alpha = 0.4f), RoundedCornerShape(24.dp))
            .padding(24.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.weight(1f))

            Text(
                text = word.turkish,
                fontSize = 28.sp,
                fontWeight = FontWeight.Bold,
                color = LexiColors.Primary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(16.dp))

            Box(
                modifier = Modifier
                    .width(40.dp)
                    .height(2.dp)
                    .background(LexiColors.SurfaceBorder),
            )

            Spacer(Modifier.height(16.dp))

            Text(
                text = "“${word.example}”",
                fontSize = 14.sp,
                fontStyle = FontStyle.Italic,
                color = Color.White,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(8.dp))

            Text(
                text = word.exampleTr,
                fontSize = 12.sp,
                color = LexiColors.OnSurfaceMuted,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.weight(1f))
        }
    }
}

private fun parseHex(hex: String): Color {
    val clean = hex.removePrefix("#")
    val value = clean.toLong(16) or 0xFF000000
    return Color(value)
}
