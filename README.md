# Engineering Blog Digest

Cross-platform (Android + iOS, Kotlin Multiplatform) app that aggregates
posts from major engineering blogs, summarizes them via AI, and shows a
feed personalized to user interests.

See `docs/architecture.md` for the full architecture, screen flow, and
MVP roadmap.

## Structure
- `shared/` — KMP shared module (models, networking, local db, repository)
- `androidApp/` — Android app (Compose UI)
- `iosApp/` — iOS app (SwiftUI or Compose Multiplatform)
- `backend/` — feed ingestion + summarization service
- `docs/` — architecture & planning docs
