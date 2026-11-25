import SwiftUI
import FamilyControls
import SleepScrollCore

struct TonightView: View {
    @EnvironmentObject private var model: AppModel
    @State private var showPicker = false

    var body: some View {
        ScrollView {
            VStack(spacing: 16) {
                ScreenHeader(title: "Tonight", subtitle: "Ease into sleep, one gentle nudge at a time.")
                if !model.setupComplete { setupCard }
                if model.sleep.active { activeCard } else { startCard }
                scheduleCard
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

    // MARK: Setup

    private var setupCard: some View {
        NightCard {
            Text("Finish setup").font(.headline).foregroundStyle(.white)
            Text("sleep-scroll uses Screen Time to gently cover the apps you choose at bedtime. Apple never tells us which apps they are, and nothing leaves your phone.")
                .font(.subheadline).foregroundStyle(Night.muted)
            SetupRow(title: "Screen Time access", detail: "Lets us cover apps during wind-down",
                     done: model.screenTimeApproved) {
                Task { await model.requestScreenTime() }
            }
            SetupRow(title: "Choose apps", detail: "TikTok, Instagram, YouTube…",
                     done: model.hasSelection, actionLabel: "Choose") {
                showPicker = true
            }
            .disabled(!model.screenTimeApproved)
            SetupRow(title: "Notifications", detail: "The reminder and the wind-down",
                     done: model.notificationsAllowed) {
                Task { await model.requestNotifications() }
            }
        }
    }

    // MARK: Start

    private var startCard: some View {
        NightCard {
            VStack(spacing: 12) {
                Text("🌙").font(.system(size: 52))
                Text("Start a wind-down").font(.title2.bold()).foregroundStyle(.white)
                Text("Social apps stay open at first, then softly give way to rest over the next \(model.chosenDuration) minutes.")
                    .font(.subheadline).foregroundStyle(Night.text).multilineTextAlignment(.center)
                HStack(spacing: 8) {
                    ForEach(SleepLogic.durations, id: \.self) { minutes in
                        Button("\(minutes) min") { model.chosenDuration = minutes }
                            .font(.subheadline.weight(.semibold))
                            .frame(maxWidth: .infinity)
                            .padding(.vertical, 10)
                            .foregroundStyle(model.chosenDuration == minutes ? Night.onPrimary : Night.text)
                            .background {
                                if model.chosenDuration == minutes {
                                    RoundedRectangle(cornerRadius: 10).fill(Night.primary)
                                } else {
                                    RoundedRectangle(cornerRadius: 10).strokeBorder(Night.primary.opacity(0.6))
                                }
                            }
                    }
                }
                Text("\(model.settings.windDownStyle.label) wind-down · \(model.settings.mode.label) mode")
                    .font(.caption.weight(.semibold)).foregroundStyle(Night.muted)
                Button("Start sleep mode") { model.start() }
                    .buttonStyle(PillButtonStyle())
                    .disabled(!model.screenTimeApproved || !model.hasSelection)
                    .opacity(model.screenTimeApproved && model.hasSelection ? 1 : 0.5)
            }
            .frame(maxWidth: .infinity)
        }
    }

    // MARK: Active

    private var activeCard: some View {
        NightCard {
            VStack(spacing: 12) {
                Text("SLEEP MODE ON").font(.caption.weight(.bold)).foregroundStyle(Night.done)
                Text(SleepLogic.formatRemaining(model.remaining))
                    .font(.system(size: 64, weight: .bold, design: .rounded))
                    .monospacedDigit()
                    .foregroundStyle(Night.text)
                Text(phaseLine).font(.callout).foregroundStyle(.white)
                ProgressView(value: model.progress).tint(Night.primary)
                if model.phase >= 3 {
                    Button("Open wind-down") { model.showWindDown = true }
                        .buttonStyle(PillButtonStyle())
                }
                HStack(spacing: 12) {
                    if model.settings.mode == .normal {
                        Button("+\(SleepLogic.snoozeMinutes) min") { model.snooze() }
                            .buttonStyle(PillButtonStyle(filled: false))
                    }
                    Button("Stop") { model.stop() }
                        .buttonStyle(PillButtonStyle(filled: false))
                }
            }
            .frame(maxWidth: .infinity)
        }
    }

    private var phaseLine: String {
        switch model.phase {
        case 0: return "Plenty of time — enjoy your feed."
        case 1: return "Sleep mode starting soon."
        case 2: return "Time to wrap up."
        default: return "Time to sleep."
        }
    }

    // MARK: Schedule

    private var scheduleCard: some View {
        NightCard {
            Toggle(isOn: Binding(
                get: { model.schedule.enabled },
                set: { on in
                    var s = model.schedule
                    s.enabled = on
                    s.duration = model.chosenDuration
                    model.setSchedule(s)
                }
            )) {
                VStack(alignment: .leading, spacing: 2) {
                    Text("Every night").font(.headline).foregroundStyle(.white)
                    Text("Start sleep mode automatically at bedtime").font(.subheadline).foregroundStyle(Night.muted)
                }
            }
            .tint(Night.primary)
            .disabled(!model.screenTimeApproved || !model.hasSelection)

            if model.schedule.enabled {
                DatePicker("Bedtime", selection: Binding(
                    get: {
                        Calendar.current.date(from: DateComponents(hour: model.schedule.hour,
                                                                   minute: model.schedule.minute)) ?? Date()
                    },
                    set: { date in
                        var s = model.schedule
                        let c = Calendar.current.dateComponents([.hour, .minute], from: date)
                        s.hour = c.hour ?? 22
                        s.minute = c.minute ?? 30
                        model.setSchedule(s)
                    }
                ), displayedComponents: .hourAndMinute)
                .foregroundStyle(Night.text)
                Text("Starts at \(SleepLogic.formatClock(hour: model.schedule.hour, minute: model.schedule.minute)) for \(model.schedule.duration) min.")
                    .font(.caption).foregroundStyle(Night.muted)
            }
        }
    }
}

private struct SetupRow: View {
    let title: String
    let detail: String
    let done: Bool
    var actionLabel = "Allow"
    let action: () -> Void

    var body: some View {
        HStack {
            VStack(alignment: .leading, spacing: 2) {
                Text(title).foregroundStyle(.white)
                Text(detail).font(.caption).foregroundStyle(Night.muted)
            }
            Spacer()
            if done {
                Image(systemName: "checkmark.circle.fill").foregroundStyle(Night.done)
            } else {
                Button(actionLabel, action: action).foregroundStyle(Night.primary).fontWeight(.semibold)
            }
        }
        .padding(.vertical, 4)
    }
}
