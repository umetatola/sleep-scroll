import SwiftUI
import SleepScrollCore

/// The full wind-down ritual. On Android this is an overlay on top of the social app; iOS can't do
/// that, so it lives here and the phase-3 notification and shield point the user to it.
struct WindDownView: View {
    @EnvironmentObject private var model: AppModel

    var body: some View {
        ZStack {
            Color(red: 0.024, green: 0.024, blue: 0.071).ignoresSafeArea()
            VStack(spacing: 20) {
                Spacer()
                Text(model.settings.windDownStyle == .silence ? "🌑" : "🌙").font(.system(size: 64))
                Text("Time to sleep").font(.largeTitle.bold()).foregroundStyle(.white)
                if model.sleep.active {
                    Text("Closing in \(SleepLogic.formatRemaining(model.remaining))")
                        .font(.callout).monospacedDigit().foregroundStyle(Night.muted)
                }
                content.padding(.top, 12)
                Spacer()
                Button("Done for tonight") { model.showWindDown = false }
                    .buttonStyle(PillButtonStyle())
                    .padding(.horizontal, 48)
                    .padding(.bottom, 24)
            }
            .padding(.horizontal, 24)
        }
    }

    @ViewBuilder private var content: some View {
        switch model.settings.windDownStyle {
        case .breathing: BreathingRing()
        case .reflection: RotatingPrompt(prompts: WindDownContent.reflectionPrompts)
        case .journaling:
            VStack(spacing: 10) {
                Text("TONIGHT'S QUESTION").font(.caption.weight(.bold)).foregroundStyle(Night.primary)
                Text(WindDownContent.journalPrompt(weekday: Calendar.current.component(.weekday, from: Date())))
                    .font(.title3).foregroundStyle(Night.text).multilineTextAlignment(.center)
            }
        case .sounds:
            Text("Put on something calm — rain, a soft playlist, a slow podcast — and let your eyes close.")
                .font(.title3).foregroundStyle(Night.text).multilineTextAlignment(.center)
        case .silence:
            EmptyView()
        }
    }
}

/// 4-4-4-4 box breathing: the ring grows on the in-breath and shrinks on the out-breath.
private struct BreathingRing: View {
    @State private var step = 0
    private let timer = Timer.publish(every: 4, on: .main, in: .common).autoconnect()

    var body: some View {
        VStack(spacing: 28) {
            Circle()
                .fill(Night.primary.opacity(0.12))
                .overlay(Circle().strokeBorder(Night.muted, lineWidth: 3))
                .frame(width: 180, height: 180)
                .scaleEffect(step <= 1 ? 1.0 : 0.7) // in, hold → full; out, hold → small
                .animation(.easeInOut(duration: 4), value: step)
            Text(WindDownContent.breathSteps[step]).font(.title3).foregroundStyle(Night.text)
        }
        .onReceive(timer) { _ in step = (step + 1) % WindDownContent.breathSteps.count }
    }
}

private struct RotatingPrompt: View {
    let prompts: [String]
    @State private var index = 0
    private let timer = Timer.publish(every: 7, on: .main, in: .common).autoconnect()

    var body: some View {
        Text(prompts[index])
            .font(.title3).foregroundStyle(Night.text).multilineTextAlignment(.center)
            .id(index)
            .transition(.opacity)
            .onReceive(timer) { _ in
                withAnimation(.easeInOut(duration: 0.8)) { index = (index + 1) % prompts.count }
            }
    }
}
