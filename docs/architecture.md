# Tech Blog Digest — Architecture & Plan

## 1. Product summary

A cross-platform (Android + iOS) app that aggregates posts from ~12 major
engineering blogs (Google Research, Meta, AWS, Microsoft, Netflix, Figma,
Reddit, Spotify, Slack, Uber, OpenAI, Cloudflare, etc.), generates short
AI summaries tagged by topic, and lets users browse a feed personalized to
their interests. Tapping a card opens the original article in an in-app
browser tab — the app never rehosts full article content.

**Non-goals for v1:** commenting, offline article caching of full text,
multi-language support, notifications (candidate for v2).

---

## 2. High-level architecture

```
┌─────────────────────────────┐         ┌────────────────────────────┐
│   Blog RSS/Atom feeds        │         │      Claude API             │
│   (Netflix, Uber, AWS, …)    │         │  (summarize + tag posts)    │
└──────────────┬───────────────┘         └───────────────┬────────────┘
               │  poll on schedule                        │  called by
               ▼                                           ▼
        ┌─────────────────────────────────────────────────────┐
        │                  Backend service                     │
        │  - Feed ingestion (cron/worker)                      │
        │  - Summarization + tagging pipeline                  │
        │  - REST/GraphQL API                                  │
        │  - Postgres (posts, sources, tags)                   │
        └──────────────────────────┬────────────────────────────┘
                                    │  HTTPS (Ktor client)
                    ┌───────────────┴───────────────┐
                    ▼                                ▼
          ┌──────────────────┐             ┌──────────────────┐
          │  Android app      │             │   iOS app         │
          │  (KMP shared +    │             │  (KMP shared +    │
          │   Compose UI)     │             │   SwiftUI/Compose)│
          └──────────────────┘             └──────────────────┘
```

Key principle: **summarization happens once, server-side**, not per user
per device. The app is a thin client over your backend's API — it never
calls the LLM directly and never ships an API key on-device.

---

## 3. Backend

- **Runtime:** Ktor server (Kotlin) — lets you share DTOs/models with the
  KMP client, or Node/Python if you prefer a different ecosystem.
- **Feed ingestion:** scheduled job (every 15–30 min) pulls each blog's
  RSS/Atom feed. Most of the 12 blogs publish one; the few that don't
  (e.g. Google Research) may need a light scraper — check ToS first.
- **Summarization pipeline:** for each new post, call the Claude API with
  the post's title + excerpt, asking for a 2–3 sentence summary and a
  small set of topic tags (e.g. "distributed systems", "ML infra",
  "frontend"). Store only the summary + tags + link — never the full
  article text (copyright).
- **Database (Postgres):**
  - `sources` (blog name, feed url, icon)
  - `posts` (id, source_id, title, link, published_at, summary, tags[])
  - `users` (id, interest_tags[], saved_post_ids[])
- **API:** `GET /feed?tags=...&cursor=...`, `GET /post/{id}`,
  `POST /user/interests`, `POST /user/saved/{postId}`.

---

## 4. KMP app structure

```
/shared
  /commonMain
    /model        → Post, Source, Tag, UserPrefs data classes
    /network      → Ktor client, API service interface
    /db           → SQLDelight schema + generated queries (local cache)
    /repository   → FeedRepository: merges API + cache, applies ranking
  /androidMain    → platform actuals (Context, DataStore, etc.)
  /iosMain        → platform actuals (NSUserDefaults, etc.)

/androidApp       → Compose UI, navigation, DI (Koin)
/iosApp           → SwiftUI (or shared Compose Multiplatform UI)

/backend          → feed ingestion + summarization + REST API (separate repo optional)
```

**Libraries:**
- Ktor client — networking (shared)
- kotlinx.serialization — JSON parsing
- SQLDelight — local cache of feed + read/saved state, works offline
- Koin (or manual DI) — dependency injection across platforms
- Compose Multiplatform (recommended) or Compose (Android) + SwiftUI (iOS)

---

## 5. Data flow (client side)

1. On launch, `FeedRepository` checks local SQLDelight cache for a
   fresh-enough feed; if stale, calls `GET /feed`.
2. Results merge with user's `interest_tags` (stored locally + synced to
   backend) to rank/filter the list client-side (cheap, instant re-sort
   when the user changes interests — no network round trip needed).
3. Cache is written back to SQLDelight so the feed works offline on
   next open (showing last-fetched data).
4. Tapping a card opens `CustomTabsIntent` (Android) or
   `SFSafariViewController` (iOS) with the original article URL —
   handled by a small `expect/actual` platform function in KMP.

---

## 6. Screen flow

See the diagram above. In short:

1. **Splash** → checks if onboarding is complete.
2. **Onboarding** → user picks interest tags from the 12 sources /
   topic categories (multi-select chips).
3. **Home feed** → personalized, scrollable list of summary cards
   (source icon, title, 2–3 sentence summary, tags, "Read full article").
4. **Post summary** (optional detail screen, or expand-in-place) →
   full summary + "Open original" button.
5. **Settings** → edit interest tags, manage saved posts, notification
   toggle (v2).

---

## 7. MVP roadmap

**Phase 1 — Backend skeleton (1–2 wks)**
- Feed ingestion for 12 sources, Postgres schema, manual summarization
  test via Claude API, basic REST endpoint returning raw feed.

**Phase 2 — Shared KMP module (1–2 wks)**
- Ktor client + models, SQLDelight cache, FeedRepository with
  ranking/filtering logic. Unit-testable without any UI.

**Phase 3 — Android UI (1–2 wks)**
- Compose screens: splash, onboarding, home feed, settings. Wire to
  shared repository.

**Phase 4 — iOS UI (1–2 wks)**
- Reuse shared module; either SwiftUI screens or share Compose
  Multiplatform UI directly (faster, less native polish).

**Phase 5 — Polish & launch (1 wk)**
- Pull-to-refresh, empty/error states, analytics, App Store/Play
  Store listings.

---

## 8. Open questions to decide early

- Compose Multiplatform UI (one codebase) vs native SwiftUI (better iOS
  feel, more work)?
- Should tag personalization be purely explicit (user picks tags) or
  also implicit (learn from taps/saves over time)?
- Push notifications for new posts matching interests — v1 or v2?
