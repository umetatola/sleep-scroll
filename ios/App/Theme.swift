import SwiftUI

/// Same night palette as the Android app and the extension.
enum Night {
    static let background = Color(red: 0.043, green: 0.043, blue: 0.118)   // #0B0B1E
    static let card = Color(red: 0.086, green: 0.082, blue: 0.231)         // #16153B
    static let primary = Color(red: 0.506, green: 0.549, blue: 0.973)      // #818CF8
    static let onPrimary = Color(red: 0.118, green: 0.106, blue: 0.294)    // #1E1B4B
    static let text = Color(red: 0.878, green: 0.906, blue: 1.0)           // #E0E7FF
    static let muted = Color(red: 0.647, green: 0.706, blue: 0.988)        // #A5B4FC
    static let done = Color(red: 0.431, green: 0.906, blue: 0.718)         // #6EE7B7
    static let partial = Color(red: 0.988, green: 0.827, blue: 0.302)      // #FCD34D
    static let empty = Color(red: 0.149, green: 0.141, blue: 0.369)
}

struct NightCard<Content: View>: View {
    @ViewBuilder var content: Content

    var body: some View {
        VStack(alignment: .leading, spacing: 10) { content }
            .frame(maxWidth: .infinity, alignment: .leading)
            .padding(18)
            .background(Night.card, in: RoundedRectangle(cornerRadius: 24, style: .continuous))
    }
}

struct ScreenHeader: View {
    let title: String
    let subtitle: String

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(title).font(.largeTitle.bold()).foregroundStyle(.white)
            Text(subtitle).font(.subheadline).foregroundStyle(Night.muted)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(.top, 8)
    }
}

struct PillButtonStyle: ButtonStyle {
    var filled = true

    func makeBody(configuration: Configuration) -> some View {
        configuration.label
            .font(.headline)
            .frame(maxWidth: .infinity)
            .padding(.vertical, 14)
            .foregroundStyle(filled ? Night.onPrimary : Night.text)
            .background {
                if filled {
                    Capsule().fill(Night.primary)
                } else {
                    Capsule().strokeBorder(Night.primary.opacity(0.7), lineWidth: 1)
                }
            }
            .opacity(configuration.isPressed ? 0.75 : 1)
    }
}
