package com.alialtinok.wisly.ui.screens.settings

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PrivacyTip
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
import com.alialtinok.wisly.BuildConfig
import com.alialtinok.wisly.WislyApplication
import com.alialtinok.wisly.data.model.NativeLanguage
import com.alialtinok.wisly.i18n.LocalAppStrings
import com.alialtinok.wisly.ui.screens.my.components.SubScreenScaffold
import com.alialtinok.wisly.ui.theme.WislyColors
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onBack: () -> Unit,
    onNavigateToPrivacy: () -> Unit,
) {
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
                    color = WislyColors.OnSurfaceMuted,
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
                        .background(WislyColors.Surface, RoundedCornerShape(14.dp))
                        .border(1.dp, WislyColors.SurfaceBorder, RoundedCornerShape(14.dp))
                        .clickable(onClick = onNavigateToPrivacy)
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp),
                ) {
                    Icon(
                        imageVector = Icons.Filled.PrivacyTip,
                        contentDescription = null,
                        tint = WislyColors.Primary,
                    )
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = strings.privacyPolicy,
                            fontSize = 14.sp,
                            color = Color.White,
                            fontWeight = FontWeight.Medium,
                        )
                        Text(
                            text = strings.privacyPolicyDesc,
                            fontSize = 12.sp,
                            color = WislyColors.OnSurfaceMuted,
                        )
                    }
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                        contentDescription = null,
                        tint = WislyColors.OnSurfaceMuted,
                    )
                }
            }
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(WislyColors.Surface, RoundedCornerShape(14.dp))
                        .border(1.dp, WislyColors.SurfaceBorder, RoundedCornerShape(14.dp))
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
                        color = WislyColors.OnSurfaceMuted,
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
        color = WislyColors.OnSurfaceMuted,
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
    val border = if (isSelected) WislyColors.Primary.copy(alpha = 0.6f) else WislyColors.SurfaceBorder
    val bg = if (isSelected) WislyColors.Primary.copy(alpha = 0.10f) else WislyColors.Surface

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
                color = WislyColors.OnSurfaceMuted,
            )
        }
        if (isSelected) {
            Icon(
                imageVector = Icons.Filled.CheckCircle,
                contentDescription = null,
                tint = WislyColors.Primary,
            )
        }
    }
}
