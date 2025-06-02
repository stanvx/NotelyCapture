import UIKit

class SceneDelegate: UIResponder, UIWindowSceneDelegate {

    var window: UIWindow?

    func scene(_ scene: UIScene,
               willConnectTo session: UISceneSession,
               options connectionOptions: UIScene.ConnectionOptions) {

        guard let windowScene = scene as? UIWindowScene else { return }

        let window = UIWindow(windowScene: windowScene)

        let themeString = UserDefaults.standard.string(forKey: "theme") ?? "SYSTEM"
        let style: UIUserInterfaceStyle

        switch themeString.uppercased() {
        case "LIGHT": style = .light
        case "DARK": style = .dark
        default: style = .unspecified
        }

        print("Applying theme from UserDefaults: \(themeString) -> style: \(style.rawValue)")
        window.overrideUserInterfaceStyle = style

        self.window = window
        window.makeKeyAndVisible()
    }
}
