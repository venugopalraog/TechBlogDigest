package com.engblog.ingestion

import com.engblog.db.PostRepository
import com.engblog.model.Source
import com.engblog.summarize.ClaudeSummarizer
import com.rometools.rome.io.SyndFeedInput
import com.rometools.rome.io.XmlReader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.slf4j.LoggerFactory
import java.net.HttpURLConnection
import java.net.URI
import java.time.LocalDateTime
import java.time.ZoneId

private val logger = LoggerFactory.getLogger("FeedIngestionService")

// A generic Java UA gets rate-limited/blocked by some hosts (e.g. Reddit
// returns 429). Identify the bot honestly instead.
private const val USER_AGENT = "EngineeringBlogDigest/0.1 (feed ingestion bot)"

/**
 * Polls each configured source's RSS/Atom feed, and stores any post not
 * already in the database. Summarization is optional per-run so ingestion
 * can be tested without spending Claude API calls (pass a null summarizer).
 */
class FeedIngestionService(private val summarizer: ClaudeSummarizer?) {

    suspend fun ingestAll(sources: List<Source> = FEED_SOURCES): Int =
        sources.sumOf { source -> ingestOne(source) }

    suspend fun ingestOne(source: Source): Int = withContext(Dispatchers.IO) {
        var inserted = 0
        try {
            val connection = URI(source.feedUrl).toURL().openConnection() as HttpURLConnection
            connection.setRequestProperty("User-Agent", USER_AGENT)
            connection.connectTimeout = 10_000
            connection.readTimeout = 10_000

            val feed = connection.inputStream.use { stream ->
                SyndFeedInput().build(XmlReader(stream))
            }
            for (entry in feed.entries) {
                val link = entry.link ?: continue
                val title = entry.title ?: continue
                val publishedAt = (entry.publishedDate ?: entry.updatedDate)
                    ?.toInstant()
                    ?.atZone(ZoneId.of("UTC"))
                    ?.toLocalDateTime()
                    ?: LocalDateTime.now(ZoneId.of("UTC"))

                val excerpt = entry.description?.value
                    ?: entry.contents?.firstOrNull()?.value
                    ?: ""

                var summary: String? = null
                var tags: List<String> = emptyList()
                if (summarizer != null) {
                    runCatching { summarizer.summarize(title, excerpt.take(2000)) }
                        .onSuccess {
                            summary = it.summary
                            tags = it.tags
                        }
                        .onFailure { logger.warn("Summarization failed for '{}': {}", title, it.message) }
                }

                val wasInserted = PostRepository.insertIfNew(
                    sourceId = source.id,
                    title = title,
                    link = link,
                    publishedAt = publishedAt,
                    summary = summary,
                    tags = tags,
                )
                if (wasInserted) inserted++
            }
        } catch (e: Exception) {
            logger.warn("Failed to ingest feed '{}' ({}): {}", source.name, source.feedUrl, e.message)
        }
        inserted
    }
}
