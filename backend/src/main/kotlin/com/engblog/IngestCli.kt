package com.engblog

import com.engblog.db.DatabaseFactory
import com.engblog.ingestion.FeedIngestionService
import com.engblog.summarize.ClaudeSummarizer
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory

private val logger = LoggerFactory.getLogger("IngestCli")

/**
 * Standalone entry point for Phase 1 manual testing:
 *   ./gradlew run -PmainClass=com.engblog.IngestCliKt --args="--summarize"
 *
 * Pulls every configured feed and stores new posts. Pass --summarize to
 * also call the Claude API for a summary + tags on each new post
 * (requires ANTHROPIC_API_KEY).
 */
fun main(args: Array<String>) {
    runBlocking {
        DatabaseFactory.init()

        val withSummary = "--summarize" in args
        val summarizer = if (withSummary) ClaudeSummarizer() else null

        val service = FeedIngestionService(summarizer)
        val inserted = service.ingestAll()

        logger.info("Ingestion complete: {} new post(s) stored{}", inserted, if (withSummary) " (summarized)" else "")
        summarizer?.close()
    }
}
