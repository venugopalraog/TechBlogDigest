package com.engblog.android.ui.splash

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.engblog.android.data.UserPrefsStore
import kotlinx.coroutines.flow.first
import org.koin.compose.koinInject

@Composable
fun SplashScreen(
    onOnboardingNeeded: () -> Unit,
    onOnboardingComplete: () -> Unit,
) {
    val prefsStore: UserPrefsStore = koinInject()

    LaunchedEffect(Unit) {
        if (prefsStore.onboardingComplete.first()) {
            onOnboardingComplete()
        } else {
            onOnboardingNeeded()
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
