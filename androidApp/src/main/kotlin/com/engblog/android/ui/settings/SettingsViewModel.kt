package com.engblog.android.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engblog.android.data.UserPrefsStore
import com.engblog.shared.repository.FeedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val repository: FeedRepository,
    private val prefsStore: UserPrefsStore,
) : ViewModel() {
    val interestTags: StateFlow<Set<String>> =
        prefsStore.interestTags.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptySet())

    private val _saved = MutableStateFlow(false)
    val saved: StateFlow<Boolean> = _saved

    fun save(tags: Set<String>) {
        viewModelScope.launch {
            prefsStore.setInterestTags(tags)
            _saved.value = true
        }
    }

    /** Call once the "saved" confirmation has been shown, so it doesn't re-fire. */
    fun consumeSavedEvent() {
        _saved.value = false
    }

    fun refreshFeedNow() {
        viewModelScope.launch {
            runCatching { repository.refresh() }
        }
    }
}
