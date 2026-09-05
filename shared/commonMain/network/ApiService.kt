package com.engblog.shared.network

import com.engblog.shared.model.Post
import io.ktor.client.HttpClient
import io.ktor.client.HttpClientConfig
import io.ktor.client.call.body
import io.ktor.client.engine.HttpClientEngine
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.get
import io.ktor.client.request.parameter
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

interface ApiService {
    suspend fun getFeed(tag: String? = null, limit: Int = 50): List<Post>
    suspend fun getPost(id: Int): Post
}

private val configureJson: HttpClientConfig<*>.() -> Unit = {
    install(ContentNegotiation) {
        json(Json { ignoreUnknownKeys = true })
    }
}

/**
 * Talks to the backend's REST API (see backend/src/main/kotlin/com/engblog/Routing.kt).
 * The engine is resolved per-platform from whichever ktor-client-* engine dependency is
 * on that source set's classpath (CIO on JVM, OkHttp on Android, Darwin on iOS) unless
 * one is passed explicitly (e.g. a fake engine in tests).
 */
class KtorApiService(
    private val baseUrl: String,
    engine: HttpClientEngine? = null,
) : ApiService {
    private val client: HttpClient =
        if (engine != null) HttpClient(engine, configureJson) else HttpClient(configureJson)

    override suspend fun getFeed(tag: String?, limit: Int): List<Post> =
        client.get("$baseUrl/feed") {
            tag?.let { parameter("tags", it) }
            parameter("limit", limit)
        }.body()

    override suspend fun getPost(id: Int): Post =
        client.get("$baseUrl/post/$id").body()
}
