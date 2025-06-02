import SwiftUI

@main
struct iOSApp: App {
    
    var body: some Scene {
        WindowGroup {
            AppView()
                .onAppear {
                    applyThemeFromUserDefaults()
                }
                .onChange(of: UserDefaults.standard.string(forKey: "theme")) { _ in
                    applyThemeFromUserDefaults()
                }
        }
    }
    
    func applyThemeFromUserDefaults() {
        let theme = UserDefaults.standard.string(forKey: "theme") ?? "SYSTEM"
        let style: UIUserInterfaceStyle = {
            switch theme.uppercased() {
            case "DARK": return .dark
            case "LIGHT": return .light
            default: return .unspecified
            }
        }()
        
        UIApplication.shared.connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap { $0.windows }
            .forEach { $0.overrideUserInterfaceStyle = style }
    }
}
