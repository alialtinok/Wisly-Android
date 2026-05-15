package com.alialtinok.wisly.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alialtinok.wisly.WislyApplication
import com.alialtinok.wisly.data.model.Idiom
import com.alialtinok.wisly.data.model.NativeLanguage
import com.alialtinok.wisly.data.repository.TranslationRepository
import com.alialtinok.wisly.i18n.LocalAppStrings
import com.alialtinok.wisly.ui.screens.my.components.SubScreenScaffold
import com.alialtinok.wisly.ui.theme.WislyColors

@Composable
fun IdiomsListScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as WislyApplication).container }
    val repo = container.wordRepository
    val translationRepo = container.translationRepository
    val nativeLanguage by container.userSettingsRepository.nativeLanguage.collectAsState(initial = null)

    val idioms = remember { repo.allIdioms }
    var expandedId by remember { mutableStateOf<Int?>(null) }
    val s = LocalAppStrings.current

    SubScreenScaffold(
        title = s.contentIdioms,
        onBack = onBack,
        actions = {
            CountBadge(count = idioms.size, color = WislyColors.AccentAmber)
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(idioms, key = { it.id }) { idiom ->
                IdiomItemCard(
                    idiom = idiom,
                    isExpanded = expandedId == idiom.id,
                    onToggleExpand = {
                        expandedId = if (expandedId == idiom.id) null else idiom.id
                    },
                    nativeLanguage = nativeLanguage,
                    translationRepo = translationRepo,
                )
            }
        }
    }
}

@Composable
private fun IdiomItemCard(
    idiom: Idiom,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
    nativeLanguage: NativeLanguage?,
    translationRepo: TranslationRepository,
) {
    var translation by remember(idiom.id, nativeLanguage) { mutableStateOf(idiom.turkish) }

    LaunchedEffect(idiom.id, nativeLanguage) {
        val lang = nativeLanguage
        translation = if (lang == null || lang.id == "tr") {
            idiom.turkish
        } else {
            translationRepo.fetch(idiom.idiom, lang.translationCode) ?: idiom.turkish
        }
    }

    val showExampleTr = nativeLanguage == null || nativeLanguage.id == "tr"
    val accent = WislyColors.AccentAmber

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(WislyColors.Surface, RoundedCornerShape(14.dp))
            .border(1.dp, WislyColors.SurfaceBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onToggleExpand)
            .padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text(
            text = idiom.idiom,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Text(
            text = translation,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = accent,
        )
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(WislyColors.SurfaceBorder)
                    .padding(top = 1.dp),
            )
            Text(
                text = idiom.example,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f),
                lineHeight = 18.sp,
            )
            if (showExampleTr && idiom.exampleTr.isNotBlank()) {
                Text(
                    text = idiom.exampleTr,
                    fontSize = 13.sp,
                    color = WislyColors.OnSurfaceMuted,
                    lineHeight = 18.sp,
                )
            }
        }
    }
}
