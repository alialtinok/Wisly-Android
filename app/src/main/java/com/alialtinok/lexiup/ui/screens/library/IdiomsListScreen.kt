package com.alialtinok.lexiup.ui.screens.library

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
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alialtinok.lexiup.WislyApplication
import com.alialtinok.lexiup.data.model.Idiom
import com.alialtinok.lexiup.i18n.LocalAppStrings
import com.alialtinok.lexiup.ui.screens.my.components.SubScreenScaffold
import com.alialtinok.lexiup.ui.theme.LexiColors

@Composable
fun IdiomsListScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as WislyApplication).container }
    val repo = container.wordRepository

    val idioms = remember { repo.allIdioms }
    var expandedId by remember { mutableStateOf<Int?>(null) }
    val s = LocalAppStrings.current

    SubScreenScaffold(
        title = s.contentIdioms,
        onBack = onBack,
        actions = {
            CountBadge(count = idioms.size, color = LexiColors.AccentAmber)
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
) {
    val accent = LexiColors.AccentAmber
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(LexiColors.Surface, RoundedCornerShape(14.dp))
            .border(1.dp, LexiColors.SurfaceBorder, RoundedCornerShape(14.dp))
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
            text = idiom.turkish,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            color = accent,
        )
        if (isExpanded) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(LexiColors.SurfaceBorder)
                    .padding(top = 1.dp),
            )
            Text(
                text = idiom.example,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f),
                lineHeight = 18.sp,
            )
            Text(
                text = idiom.exampleTr,
                fontSize = 13.sp,
                color = LexiColors.OnSurfaceMuted,
                lineHeight = 18.sp,
            )
        }
    }
}
