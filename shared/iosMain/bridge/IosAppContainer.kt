package com.engblog.shared.ios

import com.engblog.shared.db.AppDatabase
import com.engblog.shared.db.DriverFactory
import com.engblog.shared.model.Post
import com.engblog.shared.network.ApiService
import com.engblog.shared.network.KtorApiService
import com.engblog.shared.repository.FeedRepository

/**
 * UNTESTED — written on Windows, so this hasn't been compiled or run against a real
 * Xcode-generated Objective-C header. It's a thin, Swift-friendly facade over the
 * shared module (plain List/String params instead of Set, which bridges less
 * predictably to Swift) precisely to reduce what needs checking on a Mac to this
 * one file plus DriverFactory.ios.kt.
 */
class IosAppContainer(baseUrl: String) {
    private val api: ApiService = KtorApiService(baseUrl = baseUrl)
    private val database = AppDatabase(DriverFactory().createDriver())
    val feedRepository = FeedRepository(api = api, database = database)
}

suspend fun FeedRepository.getFeedForIos(interestTags: List<String>, forceRefresh: Boolean): List<Post> =
    getFeed(interestTags.toSet(), forceRefresh)
