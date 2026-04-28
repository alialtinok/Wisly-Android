package com.alialtinok.lexiup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.alialtinok.lexiup.ui.navigation.MainScreen
import com.alialtinok.lexiup.ui.screens.onboarding.OnboardingScreen
import com.alialtinok.lexiup.ui.theme.LexiUpTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            LexiUpTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val context = LocalContext.current
    val container = (context.applicationContext as LexiUpApplication).container
    val onboardingDone by container.userSettingsRepository.hasCompletedOnboarding
        .collectAsState(initial = null)

    when (onboardingDone) {
        null -> Unit
        true -> MainScreen()
        false -> OnboardingScreen()
    }
}
