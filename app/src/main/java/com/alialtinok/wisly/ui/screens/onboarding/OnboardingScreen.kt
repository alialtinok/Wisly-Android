package com.alialtinok.wisly.ui.screens.onboarding

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.alialtinok.wisly.WislyApplication
import com.alialtinok.wisly.ui.theme.WislyColors
import kotlinx.coroutines.launch

private const val PAGE_COUNT = 4

@Composable
fun OnboardingScreen() {
    val context = LocalContext.current
    val container = (context.applicationContext as WislyApplication).container
    val viewModel: OnboardingViewModel = viewModel(
        factory = OnboardingViewModel.Factory(container.userSettingsRepository),
    )
    val selectedLanguage by viewModel.selectedLanguage.collectAsState()
    val selectedLevel by viewModel.selectedLevel.collectAsState()

    val pagerState = rememberPagerState(pageCount = { PAGE_COUNT })
    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(WislyColors.Background),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
            ) { page ->
                when (page) {
                    0 -> WelcomePage()
                    1 -> FeaturesPage()
                    2 -> LevelPage(
                        selected = selectedLevel,
                        onSelect = viewModel::selectLevel,
                    )
                    3 -> LanguagePage(
                        selected = selectedLanguage,
                        onSelect = viewModel::selectLanguage,
                    )
                }
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp, bottom = 40.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(20.dp),
            ) {
                PageDots(currentPage = pagerState.currentPage)

                if (pagerState.currentPage < PAGE_COUNT - 1) {
                    PrimaryButton(
                        text = "Continue",
                        onClick = {
                            scope.launch {
                                pagerState.animateScrollToPage(pagerState.currentPage + 1)
                            }
                        },
                    )
                } else {
                    PrimaryButton(
                        text = "Start Learning",
                        trailingIcon = true,
                        onClick = viewModel::complete,
                    )
                }
            }
        }
    }
}

@Composable
private fun PageDots(currentPage: Int) {
    Row(
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        repeat(PAGE_COUNT) { i ->
            val width by animateDpAsState(
                targetValue = if (i == currentPage) 24.dp else 8.dp,
                animationSpec = tween(durationMillis = 300),
                label = "dotWidth",
            )
            Box(
                modifier = Modifier
                    .size(width = width, height = 8.dp)
                    .background(
                        color = if (i == currentPage) WislyColors.Primary else WislyColors.SurfaceBorder,
                        shape = CircleShape,
                    ),
            )
        }
    }
}

@Composable
private fun PrimaryButton(
    text: String,
    onClick: () -> Unit,
    trailingIcon: Boolean = false,
) {
    Box(
        modifier = Modifier
            .padding(horizontal = 24.dp)
            .fillMaxWidth()
            .height(56.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                brush = Brush.horizontalGradient(
                    colors = listOf(WislyColors.Primary, WislyColors.AccentPurple),
                ),
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = text,
                fontSize = 17.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White,
            )
            if (trailingIcon) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(18.dp),
                )
            }
        }
    }
}
