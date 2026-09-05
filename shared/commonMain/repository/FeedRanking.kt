package com.engblog.shared.repository

import com.engblog.shared.model.Post

/**
 * Pure, platform-independent ranking so changing interest tags re-sorts the
 * already-cached feed instantly with no network round trip.
 */
object FeedRanking {
    fun rank(posts: List<Post>, interestTags: Set<String>): List<Post> =
        posts.sortedWith(
            compareByDescending<Post> { matchCount(it, interestTags) }
                .thenByDescending { it.publishedAt }
        )

    private fun matchCount(post: Post, interestTags: Set<String>): Int =
        if (interestTags.isEmpty()) 0 else post.tags.count { it in interestTags }
}
