package com.engblog.model

import kotlinx.serialization.Serializable

@Serializable
data class Source(
    val id: String,
    val name: String,
    val feedUrl: String,
    val siteUrl: String,
)

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
