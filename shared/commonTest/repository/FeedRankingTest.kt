package com.engblog.shared.repository

import com.engblog.shared.model.Post
import kotlin.test.Test
import kotlin.test.assertEquals

class FeedRankingTest {
    private fun post(id: Int, tags: List<String>, publishedAt: String) = Post(
        id = id,
        sourceId = "s",
        sourceName = "S",
        title = "post $id",
        link = "https://example.com/$id",
        publishedAt = publishedAt,
        tags = tags,
    )

    @Test
    fun postsMatchingMoreInterestTagsRankHigher() {
        val singleMatch = post(1, listOf("frontend"), "2026-01-01T00:00:00Z")
        val doubleMatch = post(2, listOf("distributed systems", "ml infra"), "2026-01-01T00:00:00Z")
        val noMatch = post(3, emptyList(), "2026-01-02T00:00:00Z")

        val ranked = FeedRanking.rank(
            listOf(singleMatch, doubleMatch, noMatch),
            interestTags = setOf("distributed systems", "ml infra"),
        )

        // doubleMatch wins on tag matches; among the zero-match tie, noMatch is newer so it
        // outranks singleMatch.
        assertEquals(listOf(2, 3, 1), ranked.map { it.id })
    }

    @Test
    fun withNoInterestTagsFallsBackToRecency() {
        val older = post(1, listOf("frontend"), "2026-01-01T00:00:00Z")
        val newer = post(2, listOf("backend"), "2026-01-02T00:00:00Z")

        val ranked = FeedRanking.rank(listOf(older, newer), interestTags = emptySet())

        assertEquals(listOf(2, 1), ranked.map { it.id })
    }

    @Test
    fun tiesOnMatchCountBreakByRecency() {
        val olderMatch = post(1, listOf("frontend"), "2026-01-01T00:00:00Z")
        val newerMatch = post(2, listOf("frontend"), "2026-01-03T00:00:00Z")

        val ranked = FeedRanking.rank(listOf(olderMatch, newerMatch), interestTags = setOf("frontend"))

        assertEquals(listOf(2, 1), ranked.map { it.id })
    }
}
