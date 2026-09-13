package com.techblogdigest.web

import kotlinx.serialization.Serializable

/**
 * Deliberately duplicated from shared/commonMain/model/Post.kt rather than shared
 * as a real KMP dependency — see webApp/README.md for why (SQLDelight has no wasmJs
 * runtime in the version this project pins). Keep the two in sync by hand.
 */
@Serializable
data class Post(
    val id: Int,
    val sourceId: String,
    val sourceName: String,
    val title: String,
    val link: String,
    val publishedAt: String,
    val summary: String? = null,
    val tags: List<String> = emptyList(),
)

val CANDIDATE_INTEREST_TAGS = listOf(
    "distributed systems", "ml infra", "frontend", "backend", "mobile",
    "infrastructure", "security", "data", "devtools", "performance",
)
