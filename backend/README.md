# Backend — feed ingestion + summarization service

Phase 1 skeleton: polls RSS/Atom feeds from ~11 engineering blogs, optionally
summarizes new posts with the Claude API, stores them in Postgres, and
serves them over a small REST API.

## Run locally

1. Start Postgres:
   ```bash
   docker compose up -d
   ```
2. Copy `.env.example` to `.env` and fill in `ANTHROPIC_API_KEY` if you want
   summarization. Export the vars into your shell (or use a tool like
   `direnv`) — Gradle doesn't load `.env` automatically.
3. Start the API server:
   ```bash
   ./gradlew run
   ```
   Serves on `http://localhost:8080`. Try `GET /health` and `GET /feed`.

4. Pull posts from all configured feeds (run this once there's something in
   Postgres to see through `/feed`):
   ```bash
   ./gradlew ingest              # store posts, no summaries
   ./gradlew ingest -Psummarize  # also call Claude for summary + tags
   ```

## Endpoints

- `GET /health` — liveness check
- `GET /feed?tags=<tag>&limit=<n>` — recent posts, optionally filtered by tag substring
- `GET /post/{id}` — a single post

## Structure

- `ingestion/` — feed source list + RSS/Atom polling (Rome)
- `summarize/` — Claude API client for summary + tag generation
- `db/` — Exposed table definitions, connection setup, repository
- `Routing.kt`, `Application.kt` — Ktor server wiring
- `IngestCli.kt` — standalone runner for manual ingestion (`./gradlew ingest`)

## Known gap

Uber Engineering has no working RSS/Atom feed as of this writing (checked
`eng.uber.com/feed/` and `uber.com/blog/engineering/rss/`, both fail) — it's
left out of `FeedSources.kt` until a working feed URL is found, so there
are 11 sources instead of 12.
