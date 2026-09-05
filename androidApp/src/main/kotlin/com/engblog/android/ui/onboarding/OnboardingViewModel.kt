package com.engblog.android.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engblog.android.data.UserPrefsStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class OnboardingViewModel(private val prefsStore: UserPrefsStore) : ViewModel() {
    private val _selectedTags = MutableStateFlow<Set<String>>(emptySet())
    val selectedTags: StateFlow<Set<String>> = _selectedTags.asStateFlow()

    fun toggleTag(tag: String) {
        _selectedTags.value = if (tag in _selectedTags.value) {
            _selectedTags.value - tag
        } else {
            _selectedTags.value + tag
        }
    }

    fun finishOnboarding(onDone: () -> Unit) {
        viewModelScope.launch {
            prefsStore.setInterestTags(_selectedTags.value)
            prefsStore.setOnboardingComplete(true)
            onDone()
        }
    }
}
