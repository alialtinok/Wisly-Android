package com.alialtinok.lexiup.ui.screens.my.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alialtinok.lexiup.data.model.Word
import com.alialtinok.lexiup.i18n.LocalAppStrings
import com.alialtinok.lexiup.ui.theme.LexiColors

@Composable
fun WordItemCard(
    word: Word,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    onSpeak: () -> Unit,
    trailing: @Composable () -> Unit,
) {
    val levelColor = parseHex(word.cefrColor)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LexiColors.Surface, RoundedCornerShape(14.dp))
            .border(1.dp, LexiColors.SurfaceBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onToggleExpand)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(2.dp),
            ) {
                Text(
                    text = word.word,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                )
                Text(
                    text = word.type.replaceFirstChar { it.uppercase() },
                    fontSize = 11.sp,
                    color = LexiColors.OnSurfaceMuted,
                )
            }
            Text(
                text = word.level,
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = levelColor,
                modifier = Modifier
                    .background(levelColor.copy(alpha = 0.15f), CircleShape)
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            )
            IconButton(onClick = onSpeak, modifier = Modifier.size(36.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                    contentDescription = LocalAppStrings.current.ttsSpeak,
                    tint = LexiColors.OnSurfaceMuted,
                )
            }
            Box(modifier = Modifier.padding(start = 2.dp)) { trailing() }
        }

        Text(
            text = word.turkish,
            fontSize = 14.sp,
            color = levelColor,
            fontWeight = FontWeight.Medium,
        )

        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LexiColors.SurfaceBorder)
                    .size(width = 0.dp, height = 1.dp),
            )
            Text(
                text = word.example,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f),
                lineHeight = 18.sp,
            )
            Text(
                text = word.exampleTr,
                fontSize = 13.sp,
                color = LexiColors.OnSurfaceMuted,
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
fun FavoriteToggleButton(isFavorite: Boolean, onToggle: () -> Unit) {
    IconButton(onClick = onToggle, modifier = Modifier.size(36.dp)) {
        Icon(
            imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
            contentDescription = "Favorite",
            tint = if (isFavorite) LexiColors.C2 else LexiColors.OnSurfaceMuted,
        )
    }
}

@Composable
fun TrailingButton(
    icon: ImageVector,
    tint: Color,
    onClick: () -> Unit,
) {
    IconButton(onClick = onClick, modifier = Modifier.size(36.dp)) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
        )
    }
}

private fun parseHex(hex: String): Color {
    val clean = hex.removePrefix("#")
    val value = clean.toLong(16) or 0xFF000000
    return Color(value)
}
