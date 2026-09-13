package com.techblogdigest.web

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp

private val Indigo = Color(0xFF3F51B5)
private val LightColors = lightColorScheme(primary = Indigo)

sealed interface FeedState {
    data object Loading : FeedState
    data class Success(val posts: List<Post>) : FeedState
    data class Error(val message: String) : FeedState
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun App() {
    var state by remember { mutableStateOf<FeedState>(FeedState.Loading) }
    var selectedTags by remember { mutableStateOf<Set<String>>(emptySet()) }
    var refreshKey by remember { mutableStateOf(0) }

    LaunchedEffect(refreshKey) {
        state = FeedState.Loading
        state = runCatching { fetchFeed() }
            .fold(
                onSuccess = { FeedState.Success(it) },
                onFailure = { FeedState.Error(it.message ?: "Couldn't reach the backend") },
            )
    }

    MaterialTheme(colorScheme = LightColors) {
        Scaffold(
            topBar = {
                TopAppBar(title = { Text("Tech Blog Digest") })
            },
        ) { padding ->
            Column(modifier = Modifier.fillMaxSize().padding(padding)) {
                FlowRow(modifier = Modifier.padding(16.dp)) {
                    CANDIDATE_INTEREST_TAGS.forEach { tag ->
                        FilterChip(
                            modifier = Modifier.padding(end = 8.dp, bottom = 8.dp),
                            selected = tag in selectedTags,
                            onClick = {
                                selectedTags = if (tag in selectedTags) selectedTags - tag else selectedTags + tag
                            },
                            label = { Text(tag) },
                        )
                    }
                }

                when (val s = state) {
                    is FeedState.Loading -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    is FeedState.Error -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text("Couldn't load the feed: ${s.message}")
                            TextButton(onClick = { refreshKey++ }) { Text("Try again") }
                        }
                    }
                    is FeedState.Success -> {
                        val ranked = remember(s.posts, selectedTags) { FeedRanking.rank(s.posts, selectedTags) }
                        LazyColumn(
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp),
                        ) {
                            items(ranked, key = { it.id }) { post -> PostCard(post) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PostCard(post: Post) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(post.sourceName, style = MaterialTheme.typography.labelMedium, color = MaterialTheme.colorScheme.primary)
            Text(post.title, style = MaterialTheme.typography.titleMedium, modifier = Modifier.padding(top = 4.dp, bottom = 8.dp))
            Text(
                post.summary ?: "No summary yet — run ingestion with summarization enabled.",
                style = MaterialTheme.typography.bodyMedium,
            )
            if (post.tags.isNotEmpty()) {
                Row(modifier = Modifier.padding(top = 8.dp), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    post.tags.take(3).forEach { tag -> AssistChip(onClick = {}, label = { Text(tag) }) }
                }
            }
            TextButton(
                onClick = { kotlinx.browser.window.open(post.link, "_blank") },
                modifier = Modifier.padding(top = 4.dp),
            ) { Text("Read full article") }
        }
    }
}
