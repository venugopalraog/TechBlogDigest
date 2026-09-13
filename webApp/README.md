# Web app — Compose Multiplatform for Web (Kotlin/Wasm)

Renders the feed in a real browser using actual Compose UI (not Compose HTML) —
the same `Card`/`LazyColumn`/Material3 APIs as `androidApp`, compiled to
WebAssembly via Kotlin/Wasm and Skia.

Unlike the iOS app, this one has actually been built and run — verified in a
real browser against the live backend (chip selection, live data fetch, and
`Read full article` all confirmed working).

## Run it

1. Start the backend (`backend/`, see its README) — it now installs a CORS
   plugin (`Application.kt`) so a browser on a different origin can call it.
   That plugin is wide open (`anyHost()`) for local dev; narrow it before any
   public deployment.
2. `./gradlew :webApp:wasmJsBrowserDevelopmentRun` from the repo root.
3. Open the URL it prints (typically `http://localhost:8081`).

## Why this isn't a real `:shared` dependency

The plan was to reuse `shared`'s `Post` model, `ApiService`, and
`FeedRepository` directly. That broke on `FeedRepository`'s SQLDelight-backed
cache: the SQLDelight version this project pins (2.0.2) has **no wasmJs
runtime artifact at all** — `app.cash.sqldelight:runtime:2.0.2` simply doesn't
publish a wasmJs variant, so `shared` can't add a wasmJs target without either
dropping SQLDelight from code every wasmJs consumer would transitively need,
or upgrading to SQLDelight 2.1.0+ (which needs an async query API refactor —
`executeAsList()` → `awaitAsList()` — plus a browser-side driver built on
`sql.js` and a Web Worker). Real, but a bigger lift than fit this pass.

So for now `webApp` is standalone: `Post.kt`, `FeedRanking.kt`, and a small
`Api.kt` are **duplicated** from `shared/commonMain`, not imported. Keep them
in sync by hand. `shared/build.gradle.kts` has a comment at the same spot
explaining why no wasmJs target was added there.

## Known gaps vs. the Android app

- **No local cache / offline support** — `webApp` calls the API directly on
  every load; there's no equivalent of `FeedRepository`'s stale-cache
  fallback. Fine for a first pass, not for production.
- **No onboarding or settings screens** — just the feed, with interest chips
  that re-rank in memory (nothing persists across a reload; no
  `localStorage` wiring yet).
- **`index.html` was hand-written**, not auto-generated — the Kotlin Gradle
  plugin didn't produce one for this dev-server setup. If you change the
  module name, check `build/js/packages/*/webpack.config.js` for the actual
  bundled output filename (`config.output.filename`) and update the
  `<script>` tag in `src/wasmJsMain/resources/index.html` to match — it's
  the *bundled* `webApp.js` webpack produces, not the raw compiler-emitted
  `.mjs`, which has unresolved bare npm imports (`@js-joda/core` etc.) that
  only resolve through webpack's bundling pass.
