package com.engblog.android.ui.feed

import android.net.Uri
import androidx.browser.customtabs.CustomTabsIntent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.engblog.shared.model.Post
import org.koin.androidx.compose.koinViewModel
import java.time.Duration
import java.time.Instant
import java.time.format.DateTimeParseException

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(onOpenSettings: () -> Unit, viewModel: FeedViewModel = koinViewModel()) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Engineering Blog Digest") },
                actions = {
                    IconButton(onClick = viewModel::refresh) {
                        Icon(Icons.Default.Refresh, contentDescription = "Refresh feed")
                    }
                    IconButton(onClick = onOpenSettings) {
                        Icon(Icons.Default.Settings, contentDescription = "Settings")
                    }
                },
            )
        },
    ) { padding ->
        when (val state = uiState) {
            is FeedUiState.Loading -> Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator() }

            is FeedUiState.Error -> EmptyState(
                modifier = Modifier.fillMaxSize().padding(padding),
                icon = Icons.Default.Warning,
                message = "Couldn't load the feed: ${state.message}",
                actionLabel = "Try again",
                onAction = viewModel::refresh,
            )

            is FeedUiState.Success -> {
                PullToRefreshBox(
                    isRefreshing = state.isRefreshing,
                    onRefresh = viewModel::refresh,
                    modifier = Modifier.fillMaxSize().padding(padding),
                ) {
                    if (state.posts.isEmpty()) {
                        EmptyState(
                            modifier = Modifier.fillMaxSize(),
                            icon = Icons.Default.Info,
                            message = "No posts yet — run the backend's ingest task.",
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(state.posts, key = { it.id }) { post ->
                                PostCard(post) { link ->
                                    CustomTabsIntent.Builder().build().launchUrl(context, Uri.parse(link))
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun EmptyState(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Icon(
                icon,
                contentDescription = null,
                modifier = Modifier.padding(bottom = 12.dp),
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Text(
                text = message,
                style = MaterialTheme.typography.bodyMedium,
                textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                modifier = Modifier.padding(horizontal = 32.dp),
            )
            if (actionLabel != null && onAction != null) {
                TextButton(onClick = onAction) { Text(actionLabel) }
            }
        }
    }
}

@Composable
private fun PostCard(post: Post, onOpenArticle: (String) -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = post.sourceName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = relativeTime(post.publishedAt),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Text(
                text = post.title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(top = 4.dp, bottom = 8.dp),
            )
            Text(
                text = post.summary ?: "No summary yet — run ingestion with summarization enabled.",
                style = MaterialTheme.typography.bodyMedium,
            )
            if (post.tags.isNotEmpty()) {
                Row(
                    modifier = Modifier.padding(top = 8.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                ) {
                    post.tags.take(3).forEach { tag ->
                        AssistChip(onClick = {}, label = { Text(tag) })
                    }
                }
            }
            TextButton(
                onClick = { onOpenArticle(post.link) },
                modifier = Modifier.padding(top = 4.dp),
            ) {
                Text("Read full article")
            }
        }
    }
}

/** Best-effort "2h ago" / "3d ago" rendering; falls back to the raw value if it won't parse. */
private fun relativeTime(publishedAtIso: String): String {
    val published = try {
        Instant.parse(publishedAtIso)
    } catch (e: DateTimeParseException) {
        return publishedAtIso
    }
    val elapsed = Duration.between(published, Instant.now())
    return when {
        elapsed.toMinutes() < 1 -> "just now"
        elapsed.toHours() < 1 -> "${elapsed.toMinutes()}m ago"
        elapsed.toDays() < 1 -> "${elapsed.toHours()}h ago"
        elapsed.toDays() < 30 -> "${elapsed.toDays()}d ago"
        else -> "${elapsed.toDays() / 30}mo ago"
    }
}
