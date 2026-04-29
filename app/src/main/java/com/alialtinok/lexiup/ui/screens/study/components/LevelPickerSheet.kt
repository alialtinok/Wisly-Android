package com.alialtinok.lexiup.ui.screens.study.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alialtinok.lexiup.ui.theme.LexiColors

data class LevelOption(
    val id: String?,
    val title: String,
    val subtitle: String,
    val color: Color,
)

@Composable
fun LevelPickerContent(
    options: List<LevelOption>,
    selected: String?,
    onSelect: (String?) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp, vertical = 8.dp)
            .navigationBarsPadding(),
        verticalArrangement = Arrangement.spacedBy(10.dp),
    ) {
        Text(
            text = "Choose Level",
            fontSize = 22.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(vertical = 4.dp),
        )
        Text(
            text = "Practice words at a specific CEFR level",
            fontSize = 13.sp,
            color = LexiColors.OnSurfaceMuted,
            modifier = Modifier.padding(bottom = 6.dp),
        )
        options.forEach { option ->
            LevelRow(
                option = option,
                isSelected = selected == option.id,
                onClick = { onSelect(option.id) },
            )
        }
        Spacer(Modifier.size(8.dp))
    }
}

@Composable
private fun LevelRow(
    option: LevelOption,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (isSelected) option.color.copy(alpha = 0.10f) else LexiColors.Surface,
                RoundedCornerShape(14.dp),
            )
            .border(
                1.dp,
                if (isSelected) option.color else LexiColors.SurfaceBorder,
                RoundedCornerShape(14.dp),
            )
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(option.color, CircleShape),
        )
        Spacer(Modifier.width(12.dp))
        Text(
            text = option.title,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            color = Color.White,
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = option.subtitle,
            fontSize = 13.sp,
            color = LexiColors.OnSurfaceMuted,
        )
        if (isSelected) {
            Spacer(Modifier.width(8.dp))
            Icon(
                imageVector = Icons.Filled.Check,
                contentDescription = null,
                tint = option.color,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}
