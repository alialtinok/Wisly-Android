package com.alialtinok.lexiup.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alialtinok.lexiup.BuildConfig
import com.alialtinok.lexiup.WislyApplication
import com.alialtinok.lexiup.data.model.NativeLanguage
import com.alialtinok.lexiup.i18n.LocalAppStrings
import com.alialtinok.lexiup.ui.screens.my.components.SubScreenScaffold
import com.alialtinok.lexiup.ui.theme.LexiColors
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as WislyApplication).container }
    val settingsRepo = container.userSettingsRepository
    val scope = rememberCoroutineScope()
    val strings = LocalAppStrings.current

    val current by settingsRepo.nativeLanguage.collectAsState(initial = null)

    SubScreenScaffold(title = strings.settingsTitle, onBack = onBack) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            item { SectionLabel(strings.settingsLanguageSection) }
            item {
                Text(
                    text = strings.settingsLanguageDesc,
                    fontSize = 13.sp,
                    color = LexiColors.OnSurfaceMuted,
                    modifier = Modifier.padding(bottom = 4.dp),
                )
            }
            items(NativeLanguage.all, key = { it.id }) { language ->
                LanguageRow(
                    language = language,
                    isSelected = language.id == current?.id,
                    onClick = {
                        scope.launch { settingsRepo.setNativeLanguage(language) }
                    },
                )
            }
            item { Column(Modifier.padding(top = 16.dp)) { SectionLabel(strings.settingsAboutSection) } }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(LexiColors.Surface, RoundedCornerShape(14.dp))
                        .border(1.dp, LexiColors.SurfaceBorder, RoundedCornerShape(14.dp))
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                ) {
                    Text(
                        text = strings.settingsVersion,
                        fontSize = 14.sp,
                        color = Color.White,
                        fontWeight = FontWeight.Medium,
                    )
                    Text(
                        text = BuildConfig.VERSION_NAME,
                        fontSize = 14.sp,
                        color = LexiColors.OnSurfaceMuted,
                    )
                }
            }
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
        modifier = Modifier.padding(top = 4.dp, bottom = 6.dp),
    )
}

@Composable
private fun LanguageRow(
    language: NativeLanguage,
    isSelected: Boolean,
    onClick: () -> Unit,
) {
    val border = if (isSelected) LexiColors.Primary.copy(alpha = 0.6f) else LexiColors.SurfaceBorder
    val bg = if (isSelected) LexiColors.Primary.copy(alpha = 0.10f) else LexiColors.Surface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(bg, RoundedCornerShape(14.dp))
            .border(1.dp, border, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Text(text = language.flag, fontSize = 28.sp)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp),
        ) {
            Text(
                text = language.name,
                fontSize = 15.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            Text(
                text = language.nameInEnglish,
                fontSize = 12.sp,
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
