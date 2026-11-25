import SwiftUI
import FamilyControls
import SleepScrollCore

struct SettingsScreen: View {
    @EnvironmentObject private var model: AppModel
    @State private var showPicker = false

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                ScreenHeader(title: "Settings", subtitle: "Make the wind-down yours.")

                Text("WIND-DOWN STYLE").font(.caption.weight(.bold)).foregroundStyle(Night.muted)
                NightCard {
                    ForEach(WindDownStyle.allCases) { style in
                        Choice(title: "\(style.emoji)  \(style.label)", detail: style.blurb,
                               selected: model.settings.windDownStyle == style) {
                            var s = model.settings
                            s.windDownStyle = style
                            model.setSettings(s)
                        }
                    }
                }

                Text("INTENSITY").font(.caption.weight(.bold)).foregroundStyle(Night.muted)
                NightCard {
                    ForEach(IntensityMode.allCases) { mode in
                        Choice(title: mode.label, detail: mode.blurb, selected: model.settings.mode == mode) {
                            var s = model.settings
                            s.mode = mode
                            model.setSettings(s)
                        }
                    }
                }

                Text("APPS").font(.caption.weight(.bold)).foregroundStyle(Night.muted)
                NightCard {
                    let s = model.selection
                    Text(summary(s)).foregroundStyle(.white)
                    Text("iOS keeps the list private — sleep-scroll only sees anonymous tokens.")
                        .font(.caption).foregroundStyle(Night.muted)
                    Button("Choose apps") { showPicker = true }
                        .buttonStyle(PillButtonStyle(filled: false))
                        .disabled(!model.screenTimeApproved)
                }

                Text("Everything stays on this phone. No account, no analytics, no network.")
                    .font(.caption).foregroundStyle(Night.muted)
                    .padding(.top, 8)
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 24)
        }
        .background(Night.background.ignoresSafeArea())
        .familyActivityPicker(isPresented: $showPicker, selection: Binding(
            get: { model.selection },
            set: { model.setSelection($0) }
        ))
    }

    private func summary(_ s: FamilyActivitySelection) -> String {
        var parts: [String] = []
        if !s.applicationTokens.isEmpty { parts.append("\(s.applicationTokens.count) apps") }
        if !s.categoryTokens.isEmpty { parts.append("\(s.categoryTokens.count) categories") }
        if !s.webDomainTokens.isEmpty { parts.append("\(s.webDomainTokens.count) websites") }
        return parts.isEmpty ? "No apps chosen yet" : parts.joined(separator: " · ")
    }
}

private struct Choice: View {
    let title: String
    let detail: String
    let selected: Bool
    let action: () -> Void

    var body: some View {
        Button(action: action) {
            HStack(alignment: .top) {
                VStack(alignment: .leading, spacing: 2) {
                    Text(title).foregroundStyle(.white)
                    Text(detail).font(.caption).foregroundStyle(Night.muted)
                }
                Spacer()
                Image(systemName: selected ? "largecircle.fill.circle" : "circle")
                    .foregroundStyle(selected ? Night.primary : Night.muted)
            }
            .padding(.vertical, 4)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
    }
}
