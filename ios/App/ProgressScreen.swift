import SwiftUI
import SleepScrollCore

struct ProgressScreen: View {
    @EnvironmentObject private var model: AppModel

    private static let weekday: DateFormatter = {
        let f = DateFormatter()
        f.setLocalizedDateFormatFromTemplate("EEE")
        return f
    }()

    private static let day: DateFormatter = {
        let f = DateFormatter()
        f.setLocalizedDateFormatFromTemplate("EEE, MMM d")
        return f
    }()

    var body: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 16) {
                ScreenHeader(title: "Progress", subtitle: "Consistency beats perfection.")

                HStack(spacing: 12) {
                    Stat(value: "\(SleepLogic.streak(model.sessions, today: Date()))", label: "day streak")
                    Stat(value: "\(model.sessions.count)", label: "sessions")
                    Stat(value: "\(SleepLogic.averageDuration(model.sessions))m", label: "avg length")
                }

                Text("LAST 7 DAYS").font(.caption.weight(.bold)).foregroundStyle(Night.muted)
                NightCard {
                    HStack {
                        ForEach(SleepLogic.lastSevenDays(model.sessions, today: Date())) { entry in
                            VStack(spacing: 8) {
                                Circle().fill(color(entry.status)).frame(width: 30, height: 30)
                                Text(Self.weekday.string(from: entry.date))
                                    .font(.caption).foregroundStyle(Night.muted)
                            }
                            .frame(maxWidth: .infinity)
                        }
                    }
                    HStack(spacing: 16) {
                        Legend(color: Night.done, label: "Completed")
                        Legend(color: Night.partial, label: "Stopped early")
                    }
                }

                Text("RECENT SESSIONS").font(.caption.weight(.bold)).foregroundStyle(Night.muted)
                if model.sessions.isEmpty {
                    NightCard {
                        Text("No sessions yet. Your first wind-down will show up here.")
                            .foregroundStyle(Night.muted)
                    }
                } else {
                    ForEach(Array(model.sessions.suffix(10).reversed())) { s in
                        NightCard {
                            HStack {
                                VStack(alignment: .leading, spacing: 2) {
                                    Text(Self.day.string(from: s.startTime)).font(.headline).foregroundStyle(.white)
                                    Text(s.startTime, style: .time).font(.caption).foregroundStyle(Night.muted)
                                }
                                Spacer()
                                VStack(alignment: .trailing, spacing: 2) {
                                    Text("\(s.duration) min").foregroundStyle(.white)
                                    Text(s.completed ? "✓ completed" : "stopped early")
                                        .font(.caption)
                                        .foregroundStyle(s.completed ? Night.done : Night.partial)
                                }
                            }
                        }
                    }
                }
            }
            .padding(.horizontal, 16)
            .padding(.bottom, 24)
        }
        .background(Night.background.ignoresSafeArea())
    }

    private func color(_ status: DayStatus) -> Color {
        switch status {
        case .done: return Night.done
        case .partial: return Night.partial
        case .none: return Night.empty
        }
    }
}

private struct Stat: View {
    let value: String
    let label: String

    var body: some View {
        VStack(alignment: .leading, spacing: 4) {
            Text(value).font(.title.bold()).foregroundStyle(Night.text)
            Text(label).font(.caption).foregroundStyle(Night.muted)
        }
        .frame(maxWidth: .infinity, alignment: .leading)
        .padding(16)
        .background(Night.card, in: RoundedRectangle(cornerRadius: 20, style: .continuous))
    }
}

private struct Legend: View {
    let color: Color
    let label: String

    var body: some View {
        HStack(spacing: 6) {
            Circle().fill(color).frame(width: 10, height: 10)
            Text(label).font(.caption).foregroundStyle(Night.muted)
        }
    }
}
