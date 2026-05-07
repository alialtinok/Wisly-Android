package com.alialtinok.lexiup.ui.screens.onboarding

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
import com.alialtinok.lexiup.data.model.NativeLanguage
import com.alialtinok.lexiup.ui.theme.LexiColors

@Composable
fun LanguagePage(
    selected: NativeLanguage?,
    onSelect: (NativeLanguage) -> Unit,
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Spacer(Modifier.weight(1f))

        Text(
            text = "Choose your language",
            fontSize = 30.sp,
            fontWeight = FontWeight.Black,
            color = Color.White,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = "We'll show word translations\nin your native language",
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
            items(NativeLanguage.all) { language ->
                LanguageCard(
                    language = language,
                    isSelected = language.id == selected?.id,
                    onClick = { onSelect(language) },
                )
            }
        }

        Spacer(Modifier.weight(1f))
    }
}

@Composable
private fun LanguageCard(
    language: NativeLanguage,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (isSelected) LexiColors.Primary.copy(alpha = 0.6f) else LexiColors.SurfaceBorder
    val containerColor = if (isSelected) LexiColors.Primary.copy(alpha = 0.12f) else LexiColors.Surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(containerColor)
            .border(1.5.dp, borderColor, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Text(text = language.flag, fontSize = 28.sp)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = language.name,
                fontSize = 14.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                text = language.nameInEnglish,
                fontSize = 11.sp,
                color = LexiColors.OnSurfaceMuted,
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = LexiColors.Primary,
            )
        }
    }
}
