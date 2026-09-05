import Foundation
import Shared

/// UNTESTED — `import Shared` and the `IosAppContainer`/`FeedRepository` calls below
/// need checking against the real generated header once this is built on a Mac.
/// Simulator reaches the host machine's backend via localhost directly (unlike the
/// Android emulator, which needs 10.0.2.2); a physical device needs the Mac's LAN IP.
final class AppContainer {
    static let shared = AppContainer(baseUrl: "http://127.0.0.1:8080")

    let container: IosAppContainer
    let prefsStore = UserPrefsStore()

    private init(baseUrl: String) {
        container = IosAppContainer(baseUrl: baseUrl)
    }
}
