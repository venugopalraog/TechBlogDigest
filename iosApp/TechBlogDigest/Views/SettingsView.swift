import SwiftUI

struct SettingsView: View {
    @ObservedObject var prefsStore: UserPrefsStore
    let onRefreshFeed: () -> Void

    @State private var selectedTags: Set<String> = []

    private let columns = [GridItem(.adaptive(minimum: 110), spacing: 8)]

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("Your interests").font(.headline)
            Text("Posts matching more of these rank higher in your feed.")
                .font(.caption)
                .foregroundStyle(.secondary)

            LazyVGrid(columns: columns, alignment: .leading, spacing: 8) {
                ForEach(candidateInterestTags, id: \.self) { tag in
                    TagChip(
                        label: tag,
                        isSelected: selectedTags.contains(tag),
                        onTap: {
                            if selectedTags.contains(tag) {
                                selectedTags.remove(tag)
                            } else {
                                selectedTags.insert(tag)
                            }
                        }
                    )
                }
            }

            Button("Save") { prefsStore.setInterestTags(selectedTags) }
                .buttonStyle(.borderedProminent)
                .frame(maxWidth: .infinity)

            Button("Refresh feed now", action: onRefreshFeed)
                .buttonStyle(.bordered)
                .frame(maxWidth: .infinity)

            Spacer()
        }
        .padding(24)
        .navigationTitle("Settings")
        .onAppear { selectedTags = prefsStore.interestTags }
    }
}
