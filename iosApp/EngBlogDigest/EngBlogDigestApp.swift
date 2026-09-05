import SwiftUI

// UNTESTED — written on Windows without Xcode. First things to check on a Mac:
// this file compiles, `import Shared` resolves once the framework is embedded,
// and AppContainer/FeedViewModel's calls into it match the generated header.

@main
struct EngBlogDigestApp: App {
    var body: some Scene {
        WindowGroup {
            RootView()
        }
    }
}
