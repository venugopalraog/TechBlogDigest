package com.engblog.android.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.engblog.android.ui.components.InterestTagChips
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(onBack: () -> Unit, viewModel: SettingsViewModel = koinViewModel()) {
    val savedTags by viewModel.interestTags.collectAsState()
    var selectedTags by remember { mutableStateOf<Set<String>>(emptySet()) }

    LaunchedEffect(savedTags) { selectedTags = savedTags }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Settings") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
            )
        },
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
            Text("Your interests", style = MaterialTheme.typography.titleMedium)
            Text(
                "Posts matching more of these rank higher in your feed.",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(top = 4.dp, bottom = 16.dp),
            )
            InterestTagChips(
                selectedTags = selectedTags,
                onToggle = { tag -> selectedTags = if (tag in selectedTags) selectedTags - tag else selectedTags + tag },
            )
            Button(
                onClick = { viewModel.save(selectedTags) },
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            ) {
                Text("Save")
            }
            OutlinedButton(
                onClick = viewModel::refreshFeedNow,
                modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            ) {
                Text("Refresh feed now")
            }
            // Saved posts and notification preferences are out of scope for v1
            // (the backend doesn't have a saved-posts endpoint yet; notifications
            // are explicitly a v2 feature per docs/architecture.md).
        }
    }
}
