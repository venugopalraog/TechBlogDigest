package com.engblog.shared.repository

import com.engblog.shared.db.AppDatabase
import com.engblog.shared.db.PostEntity
import com.engblog.shared.model.Post
import com.engblog.shared.network.ApiService
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.time.Duration.Companion.minutes

/**
 * Merges the backend API with the local SQLDelight cache: fresh-enough data is served
 * straight from cache (instant, works offline); stale data triggers a refresh first.
 * Ranking/filtering by interest tags always happens against the cache, client-side.
 */
class FeedRepository(
    private val api: ApiService,
    private val database: AppDatabase,
    private val clock: Clock = Clock.System,
) {
    private var lastRefreshedAt: Instant? = null
    private val cacheTtl = 15.minutes

    suspend fun getFeed(interestTags: Set<String> = emptySet(), forceRefresh: Boolean = false): List<Post> {
        val stale = lastRefreshedAt == null || clock.now() - lastRefreshedAt!! > cacheTtl
        if (forceRefresh || stale) {
            val cacheWasEmpty = cachedPosts().isEmpty()
            runCatching { refresh() }
                .onFailure { if (cacheWasEmpty) throw it } // no cached fallback to offer
        }
        return FeedRanking.rank(cachedPosts(), interestTags)
    }

    suspend fun refresh() {
        val fresh = api.getFeed()
        database.transaction {
            fresh.forEach { post ->
                database.postCacheQueries.insertOrReplace(
                    id = post.id.toLong(),
                    sourceId = post.sourceId,
                    sourceName = post.sourceName,
                    title = post.title,
                    link = post.link,
                    publishedAt = post.publishedAt,
                    summary = post.summary,
                    tags = post.tags.joinToString(","),
                )
            }
        }
        lastRefreshedAt = clock.now()
    }

    fun cachedPosts(): List<Post> =
        database.postCacheQueries.selectAll().executeAsList().map { it.toPost() }
}

private fun PostEntity.toPost() = Post(
    id = id.toInt(),
    sourceId = sourceId,
    sourceName = sourceName,
    title = title,
    link = link,
    publishedAt = publishedAt,
    summary = summary,
    tags = tags.split(",").filter { it.isNotBlank() },
)
