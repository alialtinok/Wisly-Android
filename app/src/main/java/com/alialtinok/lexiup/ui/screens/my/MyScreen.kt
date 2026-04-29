package com.alialtinok.lexiup.ui.screens.my

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
import com.alialtinok.lexiup.LexiUpApplication
import com.alialtinok.lexiup.ui.theme.LexiColors

@Composable
fun MyScreen(
    onNavigateToFavorites: () -> Unit,
    onNavigateToUnknown: () -> Unit,
    onNavigateToMyWords: () -> Unit,
) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as LexiUpApplication).container }
    val repo = container.wordRepository

    val favorites by repo.favoriteIds.collectAsState(initial = emptySet())
    val unknown by repo.unknownIds.collectAsState(initial = emptySet())
    val myWords by repo.myWords.collectAsState(initial = emptyList())

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .background(LexiColors.Background),
        contentPadding = PaddingValues(horizontal = 20.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        item {
            Text(
                text = "My",
                fontSize = 32.sp,
                fontWeight = FontWeight.Black,
                color = Color.White,
                modifier = Modifier.padding(bottom = 8.dp),
            )
        }
        item { SectionLabel("MY LISTS") }
        item {
            MyRow(
                icon = Icons.Filled.Favorite,
                color = LexiColors.C2,
                title = "Favorites",
                count = favorites.size,
                subtitle = "Saved words",
                onClick = onNavigateToFavorites,
            )
        }
        item {
            MyRow(
                icon = Icons.Filled.ErrorOutline,
                color = LexiColors.B2,
                title = "Unknown Words",
                count = unknown.size,
                subtitle = "Need review",
                onClick = onNavigateToUnknown,
            )
        }
        item {
            MyRow(
                icon = Icons.Filled.AddCircle,
                color = LexiColors.AccentGreen,
                title = "My Words",
                count = myWords.size,
                subtitle = "Your own words",
                onClick = onNavigateToMyWords,
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
        color = LexiColors.OnSurfaceMuted,
        letterSpacing = 1.sp,
        modifier = Modifier.padding(top = 4.dp, bottom = 4.dp),
    )
}

@Composable
private fun MyRow(
    icon: ImageVector,
    color: Color,
    title: String,
    count: Int,
    subtitle: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(LexiColors.Surface, RoundedCornerShape(16.dp))
            .border(1.dp, LexiColors.SurfaceBorder, RoundedCornerShape(16.dp))
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
                color = LexiColors.OnSurfaceMuted,
            )
        }
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
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = LexiColors.OnSurfaceMuted,
            modifier = Modifier.size(20.dp),
        )
    }
}
