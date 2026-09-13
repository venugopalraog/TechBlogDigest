package com.techblogdigest.web

/** Duplicated from shared/commonMain/repository/FeedRanking.kt — same logic, see Post.kt. */
object FeedRanking {
    fun rank(posts: List<Post>, interestTags: Set<String>): List<Post> =
        posts.sortedWith(
            compareByDescending<Post> { matchCount(it, interestTags) }
                .thenByDescending { it.publishedAt }
        )

    private fun matchCount(post: Post, interestTags: Set<String>): Int =
        if (interestTags.isEmpty()) 0 else post.tags.count { it in interestTags }
}
