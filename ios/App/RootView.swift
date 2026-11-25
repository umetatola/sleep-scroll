import SwiftUI

struct RootView: View {
    @EnvironmentObject private var model: AppModel

    var body: some View {
        TabView {
            TonightView()
                .tabItem { Label("Tonight", systemImage: "moon.fill") }
            ProgressScreen()
                .tabItem { Label("Progress", systemImage: "chart.bar.fill") }
            SettingsScreen()
                .tabItem { Label("Settings", systemImage: "gearshape.fill") }
        }
        .tint(Night.primary)
        .fullScreenCover(isPresented: $model.showWindDown) {
            WindDownView()
                .environmentObject(model)
        }
    }
}
