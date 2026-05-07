package com.alialtinok.lexiup

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import com.alialtinok.lexiup.data.model.NativeLanguage
import com.alialtinok.lexiup.i18n.ProvideAppStrings
import com.alialtinok.lexiup.ui.navigation.MainScreen
import com.alialtinok.lexiup.ui.screens.onboarding.OnboardingScreen
import com.alialtinok.lexiup.ui.theme.WislyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WislyTheme {
                AppRoot()
            }
        }
    }
}

@Composable
private fun AppRoot() {
    val context = LocalContext.current
    val container = (context.applicationContext as WislyApplication).container
    val onboardingDone by container.userSettingsRepository.hasCompletedOnboarding
        .collectAsState(initial = null)
    val nativeLanguage by container.userSettingsRepository.nativeLanguage
        .collectAsState(initial = NativeLanguage.Default)

    ProvideAppStrings(language = nativeLanguage) {
        when (onboardingDone) {
            null -> Unit
            true -> MainScreen()
            false -> OnboardingScreen()
        }
    }
}
