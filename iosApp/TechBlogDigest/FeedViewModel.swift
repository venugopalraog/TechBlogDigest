import Foundation
import Shared

enum FeedUiState {
    case loading
    case success([Post])
    case error(String)
}

/// UNTESTED — Kotlin `suspend fun`s are expected to import into Swift as `async throws`
/// (standard Kotlin/Native Objective-C interop since Swift 5.5), but the exact generated
/// signature — especially `getFeedForIos`'s `[String]`/`[Post]` bridging — needs
/// confirming against Xcode's generated header on a Mac.
@MainActor
final class FeedViewModel: ObservableObject {
    @Published var uiState: FeedUiState = .loading

    private let repository: FeedRepository
    private let prefsStore: UserPrefsStore

    init(repository: FeedRepository, prefsStore: UserPrefsStore) {
        self.repository = repository
        self.prefsStore = prefsStore
        Task { await load(forceRefresh: false) }
    }

    func refresh() {
        Task { await load(forceRefresh: true) }
    }

    private func load(forceRefresh: Bool) async {
        do {
            let tags = Array(prefsStore.interestTags)
            let posts = try await repository.getFeedForIos(interestTags: tags, forceRefresh: forceRefresh)
            uiState = .success(posts)
        } catch {
            uiState = .error(error.localizedDescription)
        }
    }
}
