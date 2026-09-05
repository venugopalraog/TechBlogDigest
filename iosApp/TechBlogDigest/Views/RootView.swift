import SwiftUI

private enum Screen {
    case splash, onboarding, feed
}

struct RootView: View {
    @StateObject private var prefsStore = AppContainer.shared.prefsStore
    @State private var screen: Screen = .splash

    var body: some View {
        Group {
            switch screen {
            case .splash:
                SplashView()
                    .onAppear {
                        screen = prefsStore.onboardingComplete ? .feed : .onboarding
                    }
            case .onboarding:
                OnboardingView(prefsStore: prefsStore) { screen = .feed }
            case .feed:
                NavigationStack {
                    FeedView(prefsStore: prefsStore)
                }
            }
        }
    }
}
