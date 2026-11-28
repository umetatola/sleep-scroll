// swift-tools-version:5.9
import PackageDescription

// Pure Swift (Foundation only) so it builds and tests with `swift test` on macOS,
// and is shared by the app and its Screen Time extensions.
let package = Package(
    name: "SleepScrollCore",
    platforms: [.iOS(.v16), .macOS(.v13)],
    products: [
        .library(name: "SleepScrollCore", targets: ["SleepScrollCore"]),
    ],
    targets: [
        .target(name: "SleepScrollCore"),
        .testTarget(name: "SleepScrollCoreTests", dependencies: ["SleepScrollCore"]),
    ]
)
