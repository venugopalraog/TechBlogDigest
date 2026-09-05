import SwiftUI
import SafariServices
import Shared

struct FeedView: View {
    @ObservedObject var prefsStore: UserPrefsStore
    @StateObject private var viewModel: FeedViewModel
    @State private var articleUrl: IdentifiableUrl?

    init(prefsStore: UserPrefsStore) {
        self.prefsStore = prefsStore
        _viewModel = StateObject(
            wrappedValue: FeedViewModel(
                repository: AppContainer.shared.container.feedRepository,
                prefsStore: prefsStore
            )
        )
    }

    var body: some View {
        content
            .navigationTitle("Engineering Blog Digest")
            .toolbar {
                ToolbarItem(placement: .primaryAction) {
                    NavigationLink("Settings") {
                        SettingsView(prefsStore: prefsStore, onRefreshFeed: viewModel.refresh)
                    }
                }
                ToolbarItem(placement: .navigationBarLeading) {
                    Button(action: viewModel.refresh) {
                        Image(systemName: "arrow.clockwise")
                    }
                }
            }
            .sheet(item: $articleUrl) { item in
                SafariView(url: item.url)
            }
    }

    @ViewBuilder
    private var content: some View {
        switch viewModel.uiState {
        case .loading:
            ProgressView()
        case .error(let message):
            VStack(spacing: 12) {
                Text("Couldn't load the feed: \(message)")
                Button("Try again", action: viewModel.refresh)
            }
        case .success(let posts):
            if posts.isEmpty {
                Text("No posts yet — run the backend's ingest task.")
            } else {
                List(posts, id: \.id) { post in
                    PostRow(post: post) { link in
                        if let url = URL(string: link) {
                            articleUrl = IdentifiableUrl(url: url)
                        }
                    }
                }
                .listStyle(.plain)
            }
        }
    }
}

private struct PostRow: View {
    let post: Post
    let onOpenArticle: (String) -> Void

    var body: some View {
        VStack(alignment: .leading, spacing: 6) {
            Text(post.sourceName)
                .font(.caption).bold()
                .foregroundStyle(.tint)
            Text(post.title)
                .font(.headline)
            Text(post.summary ?? "No summary yet — run ingestion with summarization enabled.")
                .font(.subheadline)
                .foregroundStyle(.secondary)
            if !post.tags.isEmpty {
                Text(post.tags.prefix(3).joined(separator: " · "))
                    .font(.caption)
                    .foregroundStyle(.secondary)
            }
            Button("Read full article") { onOpenArticle(post.link) }
                .font(.subheadline)
        }
        .padding(.vertical, 4)
    }
}

private struct IdentifiableUrl: Identifiable {
    let url: URL
    var id: String { url.absoluteString }
}

private struct SafariView: UIViewControllerRepresentable {
    let url: URL

    func makeUIViewController(context: Context) -> SFSafariViewController {
        SFSafariViewController(url: url)
    }

    func updateUIViewController(_ uiViewController: SFSafariViewController, context: Context) {}
}
