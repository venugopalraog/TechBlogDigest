import Foundation

/// iOS counterpart to androidApp's UserPrefsStore (DataStore) — same pragmatic
/// choice of keeping prefs at the app layer rather than a shared expect/actual,
/// backed by UserDefaults instead. See androidApp/.../data/UserPrefsStore.kt.
final class UserPrefsStore: ObservableObject {
    private let defaults = UserDefaults.standard
    private let onboardingCompleteKey = "onboarding_complete"
    private let interestTagsKey = "interest_tags"

    @Published var onboardingComplete: Bool
    @Published var interestTags: Set<String>

    init() {
        onboardingComplete = defaults.bool(forKey: onboardingCompleteKey)
        interestTags = Set(defaults.stringArray(forKey: interestTagsKey) ?? [])
    }

    func setOnboardingComplete(_ complete: Bool) {
        onboardingComplete = complete
        defaults.set(complete, forKey: onboardingCompleteKey)
    }

    func setInterestTags(_ tags: Set<String>) {
        interestTags = tags
        defaults.set(Array(tags), forKey: interestTagsKey)
    }
}
