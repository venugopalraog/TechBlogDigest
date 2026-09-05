package com.engblog.android.ui.feed

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.engblog.android.data.UserPrefsStore
import com.engblog.shared.model.Post
import com.engblog.shared.repository.FeedRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

sealed interface FeedUiState {
    data object Loading : FeedUiState
    data class Success(val posts: List<Post>, val isRefreshing: Boolean = false) : FeedUiState
    data class Error(val message: String) : FeedUiState
}

class FeedViewModel(
    private val repository: FeedRepository,
    private val prefsStore: UserPrefsStore,
) : ViewModel() {
    private val _uiState = MutableStateFlow<FeedUiState>(FeedUiState.Loading)
    val uiState: StateFlow<FeedUiState> = _uiState.asStateFlow()

    init {
        load(forceRefresh = false)
    }

    fun refresh() = load(forceRefresh = true)

    private fun load(forceRefresh: Boolean) {
        viewModelScope.launch {
            val current = _uiState.value
            if (current is FeedUiState.Success) {
                _uiState.value = current.copy(isRefreshing = true)
            }
            val interestTags = prefsStore.interestTags.first()
            runCatching { repository.getFeed(interestTags, forceRefresh) }
                .onSuccess { posts -> _uiState.value = FeedUiState.Success(posts) }
                .onFailure { error ->
                    _uiState.value = FeedUiState.Error(error.message ?: "Couldn't load the feed")
                }
        }
    }
}
