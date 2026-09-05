package com.engblog.shared.repository

import com.engblog.shared.db.AppDatabase
import com.engblog.shared.db.DriverFactory
import com.engblog.shared.model.Post
import com.engblog.shared.network.ApiService
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

private class FakeApiService(var posts: List<Post> = emptyList()) : ApiService {
    var callCount = 0
    override suspend fun getFeed(tag: String?, limit: Int): List<Post> {
        callCount++
        return posts
    }
    override suspend fun getPost(id: Int): Post = posts.first { it.id == id }
}

private class FakeClock(private val now: Instant) : Clock {
    override fun now(): Instant = now
}

private fun testPost(id: Int, tags: List<String> = emptyList()) = Post(
    id = id,
    sourceId = "netflix",
    sourceName = "Netflix Tech Blog",
    title = "post $id",
    link = "https://netflixtechblog.com/$id",
    publishedAt = "2026-01-0${id}T00:00:00Z",
    tags = tags,
)

class FeedRepositoryTest {
    private fun newDatabase(): AppDatabase = AppDatabase(DriverFactory().createDriver())

    @Test
    fun refreshPopulatesCacheFromApi() = runTest {
        val api = FakeApiService(listOf(testPost(1), testPost(2)))
        val repository = FeedRepository(api, newDatabase(), clock = FakeClock(Instant.parse("2026-01-01T00:00:00Z")))

        val feed = repository.getFeed()

        assertEquals(2, feed.size)
        assertEquals(1, api.callCount)
    }

    @Test
    fun secondCallWithinTtlServesFromCacheWithoutHittingApiAgain() = runTest {
        val api = FakeApiService(listOf(testPost(1)))
        val repository = FeedRepository(api, newDatabase(), clock = FakeClock(Instant.parse("2026-01-01T00:00:00Z")))

        repository.getFeed()
        repository.getFeed()

        assertEquals(1, api.callCount)
    }

    @Test
    fun changingInterestTagsReRanksCachedDataWithoutRefetching() = runTest {
        val api = FakeApiService(listOf(testPost(1, listOf("frontend")), testPost(2, listOf("ml infra"))))
        val repository = FeedRepository(api, newDatabase(), clock = FakeClock(Instant.parse("2026-01-01T00:00:00Z")))
        repository.getFeed()

        val rankedForMlInfra = repository.getFeed(interestTags = setOf("ml infra"))

        assertEquals(2, rankedForMlInfra.first().id)
        assertEquals(1, api.callCount)
    }

    @Test
    fun staleCacheFailingRefreshStillReturnsLastKnownPosts() = runTest {
        val api = object : ApiService {
            var shouldFail = false
            var posts = listOf(testPost(1))
            override suspend fun getFeed(tag: String?, limit: Int): List<Post> {
                if (shouldFail) error("network down")
                return posts
            }
            override suspend fun getPost(id: Int): Post = posts.first { it.id == id }
        }
        val repository = FeedRepository(api, newDatabase(), clock = FakeClock(Instant.parse("2026-01-01T00:00:00Z")))
        repository.getFeed() // primes the cache

        api.shouldFail = true
        val feed = repository.getFeed(forceRefresh = true)

        assertTrue(feed.isNotEmpty(), "expected the stale cache to still be served when refresh fails")
    }
}
