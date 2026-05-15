package com.alialtinok.wisly.ui.screens.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.alialtinok.wisly.data.model.NativeLanguage
import com.alialtinok.wisly.data.repository.UserSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class OnboardingViewModel(
    private val settings: UserSettingsRepository,
) : ViewModel() {

    val selectedLanguage: StateFlow<NativeLanguage?> = settings.nativeLanguage.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = null,
    )

    fun selectLanguage(language: NativeLanguage) {
        viewModelScope.launch { settings.setNativeLanguage(language) }
    }

    fun complete() {
        viewModelScope.launch { settings.setOnboardingCompleted(true) }
    }

    class Factory(private val settings: UserSettingsRepository) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T =
            OnboardingViewModel(settings) as T
    }
}
