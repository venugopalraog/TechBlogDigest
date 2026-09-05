package com.engblog.shared.model

/**
 * Mirrors the topic tags the backend prompts Claude to choose from (see
 * backend's ClaudeSummarizer). Posts only carry real tags once ingestion has
 * run with summarization enabled — until then, onboarding/ranking by tag is
 * effectively a no-op and the feed just falls back to recency.
 */
val CANDIDATE_INTEREST_TAGS = listOf(
    "distributed systems",
    "ml infra",
    "frontend",
    "backend",
    "mobile",
    "infrastructure",
    "security",
    "data",
    "devtools",
    "performance",
)
