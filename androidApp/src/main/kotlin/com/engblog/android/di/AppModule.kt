package com.engblog.android.di

import com.engblog.android.BuildConfig
import com.engblog.android.data.UserPrefsStore
import com.engblog.android.ui.feed.FeedViewModel
import com.engblog.android.ui.onboarding.OnboardingViewModel
import com.engblog.android.ui.settings.SettingsViewModel
import com.engblog.shared.db.AppDatabase
import com.engblog.shared.db.DriverFactory
import com.engblog.shared.network.ApiService
import com.engblog.shared.network.KtorApiService
import com.engblog.shared.repository.FeedRepository
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single<ApiService> { KtorApiService(baseUrl = BuildConfig.API_BASE_URL) }
    single { DriverFactory(androidContext()) }
    single { AppDatabase(get<DriverFactory>().createDriver()) }
    single { FeedRepository(api = get(), database = get()) }
    single { UserPrefsStore(androidContext()) }

    viewModel { FeedViewModel(repository = get(), prefsStore = get()) }
    viewModel { OnboardingViewModel(prefsStore = get()) }
    viewModel { SettingsViewModel(repository = get(), prefsStore = get()) }
}
