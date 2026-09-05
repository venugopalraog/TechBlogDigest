package com.engblog.shared.model

import kotlinx.serialization.Serializable

@Serializable
data class Post(
    val id: Int,
    val sourceId: String,
    val sourceName: String,
    val title: String,
    val link: String,
    /** ISO-8601 offset date-time, exactly as returned by the backend's /feed endpoint. */
    val publishedAt: String,
    val summary: String? = null,
    val tags: List<String> = emptyList(),
)

@Serializable
data class Source(
    val id: String,
    val name: String,
)

/** Local-only for now; syncing to the backend's /user/interests is a later phase. */
data class UserPrefs(
    val interestTags: Set<String> = emptySet(),
    val savedPostIds: Set<Int> = emptySet(),
)
