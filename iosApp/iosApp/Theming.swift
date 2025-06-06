import UIKit

enum ThemeMode: String {
    case light = "LIGHT"
    case dark = "DARK"
    case system = "SYSTEM"

    static func fromDefaults() -> ThemeMode {
        let raw = UserDefaults.standard.string(forKey: "theme")?.uppercased() ?? "SYSTEM"
        return ThemeMode(rawValue: raw) ?? .system
    }

    func apply(to window: UIWindow?) {
        guard let window = window else { return }
        switch self {
        case .dark:
            window.overrideUserInterfaceStyle = .dark
        case .light:
            window.overrideUserInterfaceStyle = .light
        case .system:
            window.overrideUserInterfaceStyle = .unspecified
        }
    }
}
