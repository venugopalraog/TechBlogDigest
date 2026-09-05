# iOS app — setup on a Mac

Everything in this directory was written on Windows, where Kotlin/Native can't
compile Apple targets and there's no Xcode to open or check anything against.
**None of this has been built or run.** Treat it as a first draft to verify,
not finished code — start with the checklist at the bottom.

There's deliberately no `.xcodeproj` here: hand-writing one blind (they're a
fragile, mostly-generated format) is more likely to produce something Xcode
can't even open than to save you time. Create the project in Xcode itself and
drop these Swift files in.

## 1. Create the Xcode project

1. Xcode → File → New → Project → iOS → App.
2. Product Name: `TechBlogDigest`. Interface: SwiftUI. Save it into this
   `iosApp/` directory (so `iosApp/TechBlogDigest.xcodeproj` sits next to the
   `TechBlogDigest/` source folder already here).
3. Delete the placeholder `ContentView.swift` and `TechBlogDigestApp.swift`
   Xcode generates, and add the real ones from `TechBlogDigest/` (and
   `TechBlogDigest/Views/`) to the target instead.

## 2. Link the shared KMP framework

This project uses the plain "regular framework" integration (no CocoaPods).
In the `TechBlogDigest` target:

1. **Build Phases** → **+** → **New Run Script Phase**, placed *before*
   Compile Sources, with:
   ```bash
   cd "$SRCROOT/../shared"
   ./gradlew :shared:embedAndSignAppleFrameworkForXcode
   ```
   (If that exact task name doesn't exist, run `./gradlew tasks` from
   `shared/` and look for the `embedAndSign...` task — the name is stable
   across recent Kotlin versions but worth confirming.)
2. **Build Settings** → **Framework Search Paths**, add (non-recursive):
   ```
   $(SRCROOT)/../shared/build/xcode-frameworks/$(CONFIGURATION)/$(SDK_NAME)
   ```
3. Build once (⌘B). If it fails, the framework build/embed step above is the
   first place to look.

## 3. First things to check (in order)

1. **Does `shared` even build for iOS?** From `shared/`, run
   `../gradlew linkDebugFrameworkIosSimulatorArm64` on the Mac directly —
   isolates Kotlin/Native compile errors from Xcode project issues.
2. **`shared/iosMain/db/DriverFactory.ios.kt`** — `NativeSqliteDriver`'s
   constructor signature, confirmed against whatever SQLDelight actually
   generates.
3. **`shared/iosMain/bridge/IosAppContainer.kt`** and
   **`AppContainer.swift`** — does `import Shared` resolve, and does
   `IosAppContainer(baseUrl:)` construct correctly from Swift?
4. **`FeedViewModel.swift`** — does `repository.getFeedForIos(interestTags:forceRefresh:)`
   actually import as `async throws` returning `[Post]`? This is the
   riskiest guess in the whole iOS side; if Xcode shows something different,
   fix the call site here rather than the Kotlin (the Kotlin side was written
   deliberately simple — plain `List`/`String` — specifically to make this
   bridge predictable).
5. Once it builds, run on the iOS Simulator — it talks to
   `http://127.0.0.1:8080` (the Mac's own localhost), so start the backend
   server first (`backend/`, see its README) the same way you would for
   Android, minus the `10.0.2.2` emulator-only address translation.

## Known gaps vs. the Android app

- Interest-tag persistence uses `UserDefaults` directly (`UserPrefsStore.swift`)
  rather than a shared expect/actual — same pragmatic choice made for Android's
  DataStore-backed version; unifying them into `shared` is a reasonable later
  cleanup once both platforms are proven out.
- `CANDIDATE_INTEREST_TAGS` is hardcoded in `OnboardingView.swift` rather than
  referenced from the shared Kotlin constant, to sidestep an unverifiable guess
  about how a Kotlin top-level property's name bridges to Swift. Keep the two
  lists in sync by hand until that's confirmed and wired up properly.
