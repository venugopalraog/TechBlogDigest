import SwiftUI

// Keep in sync with shared/commonMain/model/InterestTags.kt's CANDIDATE_INTEREST_TAGS.
// Hardcoded here rather than referencing the Kotlin top-level property directly —
// its exact bridged Swift name (something like InterestTagsKt.candidateInterestTags)
// needs confirming against the real generated header on a Mac.
// Not `private` — SettingsView.swift also reads this list.
let candidateInterestTags = [
    "distributed systems", "ml infra", "frontend", "backend", "mobile",
    "infrastructure", "security", "data", "devtools", "performance",
]

struct OnboardingView: View {
    @ObservedObject var prefsStore: UserPrefsStore
    let onFinished: () -> Void

    @State private var selectedTags: Set<String> = []

    private let columns = [GridItem(.adaptive(minimum: 110), spacing: 8)]

    var body: some View {
        VStack(alignment: .leading, spacing: 16) {
            Text("What do you want more of in your feed?")
                .font(.title2).bold()
            Text("Pick a few topics — you can change these anytime in Settings.")
                .font(.subheadline)
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

            Spacer()

            Button(selectedTags.isEmpty ? "Skip for now" : "Continue") {
                prefsStore.setInterestTags(selectedTags)
                prefsStore.setOnboardingComplete(true)
                onFinished()
            }
            .buttonStyle(.borderedProminent)
            .frame(maxWidth: .infinity)
        }
        .padding(24)
    }
}

struct TagChip: View {
    let label: String
    let isSelected: Bool
    let onTap: () -> Void

    var body: some View {
        Text(label)
            .padding(.horizontal, 12)
            .padding(.vertical, 8)
            .background(isSelected ? Color.accentColor.opacity(0.2) : Color.clear)
            .overlay(
                RoundedRectangle(cornerRadius: 16)
                    .stroke(isSelected ? Color.accentColor : Color.gray.opacity(0.5))
            )
            .clipShape(RoundedRectangle(cornerRadius: 16))
            .onTapGesture(perform: onTap)
    }
}
