package com.engblog.summarize

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.header
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

@Serializable
data class PostSummary(val summary: String, val tags: List<String>)

/**
 * Calls the Claude API to produce a short summary + topic tags for one post.
 * Requires the ANTHROPIC_API_KEY environment variable.
 */
class ClaudeSummarizer(
    private val apiKey: String = System.getenv("ANTHROPIC_API_KEY")
        ?: error("ANTHROPIC_API_KEY environment variable is not set"),
    private val model: String = "claude-sonnet-5",
) {
    private val json = Json { ignoreUnknownKeys = true }

    private val client = HttpClient(CIO) {
        install(ContentNegotiation) { json(json) }
    }

    @Serializable
    private data class AnthropicRequest(
        val model: String,
        val max_tokens: Int,
        val messages: List<AnthropicMessage>,
    )

    @Serializable
    private data class AnthropicMessage(val role: String, val content: String)

    @Serializable
    private data class AnthropicResponse(val content: List<AnthropicContentBlock>)

    @Serializable
    private data class AnthropicContentBlock(val type: String, val text: String? = null)

    suspend fun summarize(title: String, excerpt: String): PostSummary {
        val prompt = """
            Summarize the following engineering blog post in 2-3 sentences,
            written for a software engineer skimming a feed. Then give 1-4
            short topic tags (e.g. "distributed systems", "ML infra",
            "frontend", "security").

            Respond with ONLY a JSON object of the form:
            {"summary": "...", "tags": ["...", "..."]}

            Title: $title
            Excerpt: $excerpt
        """.trimIndent()

        val response = client.post("https://api.anthropic.com/v1/messages") {
            header("x-api-key", apiKey)
            header("anthropic-version", "2023-06-01")
            contentType(ContentType.Application.Json)
            setBody(
                AnthropicRequest(
                    model = model,
                    max_tokens = 512,
                    messages = listOf(AnthropicMessage("user", prompt)),
                )
            )
        }.body<AnthropicResponse>()

        val text = response.content.firstOrNull { it.type == "text" }?.text
            ?: error("No text content in Claude response")

        return json.decodeFromString(PostSummary.serializer(), text)
    }

    fun close() = client.close()
}
