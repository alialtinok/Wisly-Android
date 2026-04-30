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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.alialtinok.lexiup.LexiUpApplication
import com.alialtinok.lexiup.data.model.PhrasalVerb
import com.alialtinok.lexiup.i18n.LocalAppStrings
import com.alialtinok.lexiup.ui.screens.my.components.SubScreenScaffold
import com.alialtinok.lexiup.ui.theme.LexiColors

@Composable
fun PhrasalVerbsListScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val container = remember { (context.applicationContext as LexiUpApplication).container }
    val repo = container.wordRepository

    val verbs = remember { repo.allPhrasalVerbs }
    var expandedId by remember { mutableStateOf<Int?>(null) }
    val s = LocalAppStrings.current

    SubScreenScaffold(
        title = s.contentPhrasal,
        onBack = onBack,
        actions = {
            CountBadge(count = verbs.size, color = LexiColors.AccentPurple)
        },
    ) {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp),
        ) {
            items(verbs, key = { it.id }) { verb ->
                PhrasalVerbItemCard(
                    verb = verb,
                    isExpanded = expandedId == verb.id,
                    onToggleExpand = {
                        expandedId = if (expandedId == verb.id) null else verb.id
                    },
                )
            }
        }
    }
}

@Composable
private fun PhrasalVerbItemCard(
    verb: PhrasalVerb,
    isExpanded: Boolean,
    onToggleExpand: () -> Unit,
) {
    val accent = LexiColors.AccentPurple
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
            text = verb.fullVerb,
            fontSize = 17.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
        Text(
            text = verb.turkish,
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
                text = verb.example,
                fontSize = 13.sp,
                color = Color.White.copy(alpha = 0.7f),
                lineHeight = 18.sp,
            )
            Text(
                text = verb.exampleTr,
                fontSize = 13.sp,
                color = LexiColors.OnSurfaceMuted,
                lineHeight = 18.sp,
            )
        }
    }
}

@Composable
internal fun CountBadge(count: Int, color: Color) {
    Box(
        modifier = Modifier
            .background(color.copy(alpha = 0.12f), RoundedCornerShape(8.dp))
            .border(1.dp, color.copy(alpha = 0.30f), RoundedCornerShape(8.dp))
            .padding(horizontal = 10.dp, vertical = 6.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = count.toString(),
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = color,
        )
    }
}
