package com.engblog.android.ui.nav

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.engblog.android.ui.feed.FeedScreen
import com.engblog.android.ui.onboarding.OnboardingScreen
import com.engblog.android.ui.settings.SettingsScreen
import com.engblog.android.ui.splash.SplashScreen

private object Routes {
    const val SPLASH = "splash"
    const val ONBOARDING = "onboarding"
    const val FEED = "feed"
    const val SETTINGS = "settings"
}

@Composable
fun EngBlogNavGraph(navController: NavHostController = rememberNavController()) {
    NavHost(navController = navController, startDestination = Routes.SPLASH) {
        composable(Routes.SPLASH) {
            SplashScreen(
                onOnboardingNeeded = {
                    navController.navigate(Routes.ONBOARDING) { popUpTo(Routes.SPLASH) { inclusive = true } }
                },
                onOnboardingComplete = {
                    navController.navigate(Routes.FEED) { popUpTo(Routes.SPLASH) { inclusive = true } }
                },
            )
        }
        composable(Routes.ONBOARDING) {
            OnboardingScreen(
                onFinished = {
                    navController.navigate(Routes.FEED) { popUpTo(Routes.ONBOARDING) { inclusive = true } }
                },
            )
        }
        composable(Routes.FEED) {
            FeedScreen(onOpenSettings = { navController.navigate(Routes.SETTINGS) })
        }
        composable(Routes.SETTINGS) {
            SettingsScreen(onBack = { navController.popBackStack() })
        }
    }
}
