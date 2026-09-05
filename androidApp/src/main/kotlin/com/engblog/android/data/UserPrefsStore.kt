package com.engblog.android.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "engblog_prefs")

private val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
private val INTEREST_TAGS = stringSetPreferencesKey("interest_tags")

/**
 * Android-only for now (DataStore). Promote this to a shared expect/actual
 * (NSUserDefaults on iOS) when Phase 4 adds the iOS app.
 */
class UserPrefsStore(private val context: Context) {
    val onboardingComplete: Flow<Boolean> =
        context.dataStore.data.map { it[ONBOARDING_COMPLETE] ?: false }

    val interestTags: Flow<Set<String>> =
        context.dataStore.data.map { it[INTEREST_TAGS] ?: emptySet() }

    suspend fun setOnboardingComplete(complete: Boolean) {
        context.dataStore.edit { it[ONBOARDING_COMPLETE] = complete }
    }

    suspend fun setInterestTags(tags: Set<String>) {
        context.dataStore.edit { it[INTEREST_TAGS] = tags }
    }
}
