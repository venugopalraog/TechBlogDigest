package com.engblog.android.ui.onboarding

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.engblog.android.ui.components.InterestTagChips
import org.koin.androidx.compose.koinViewModel

@Composable
fun OnboardingScreen(onFinished: () -> Unit, viewModel: OnboardingViewModel = koinViewModel()) {
    val selectedTags by viewModel.selectedTags.collectAsState()

    Scaffold { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp)) {
            Text(
                text = "What do you want more of in your feed?",
                style = androidx.compose.material3.MaterialTheme.typography.headlineSmall,
            )
            Text(
                text = "Pick a few topics — you can change these anytime in Settings.",
                style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 8.dp, bottom = 24.dp),
            )
            InterestTagChips(selectedTags = selectedTags, onToggle = viewModel::toggleTag)
            Button(
                onClick = { viewModel.finishOnboarding(onFinished) },
                modifier = Modifier.fillMaxWidth().padding(top = 24.dp),
            ) {
                Text(if (selectedTags.isEmpty()) "Skip for now" else "Continue")
            }
        }
    }
}
