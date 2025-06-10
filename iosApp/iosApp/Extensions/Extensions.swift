//
//  Extensions.swift
//  Notely Voice
//
//  Created by Tosin Onikute on 07/06/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import Foundation
import shared
import SwiftUI

extension UIApplication {
    var keyWindowCompat: UIWindow? {
        if #available(iOS 15.0, *) {
            return UIApplication.shared.connectedScenes
                .compactMap { $0 as? UIWindowScene }
                .flatMap { $0.windows }
                .first { $0.isKeyWindow }
        } else if #available(iOS 13.0, *) {
            return UIApplication.shared.windows.first { $0.isKeyWindow }
        } else {
            return UIApplication.shared.keyWindow
        }
    }
    
    func setStatusBarBackgroundColor(_ color: UIColor) {
        guard let keyWindow = keyWindowCompat else { return }
        
        let statusBarHeight = keyWindow.safeAreaInsets.top
        let statusBarFrame = CGRect(x: 0, y: 0, width: keyWindow.frame.width, height: statusBarHeight)
        
        let statusBarView = UIView(frame: statusBarFrame)
        statusBarView.backgroundColor = color
        statusBarView.tag = 12345 // Unique tag to identify our status bar view
        statusBarView.layer.zPosition = 999999 // Ensure it's on top
        
        // Remove any existing status bar view
        keyWindow.subviews.filter { $0.tag == 12345 }.forEach { $0.removeFromSuperview() }
        
        // Add the new status bar view
        keyWindow.addSubview(statusBarView)
    }
    
    func removeStatusBarBackgroundColor() {
        guard let keyWindow = keyWindowCompat else { return }
        keyWindow.subviews.filter { $0.tag == 12345 }.forEach { $0.removeFromSuperview() }
    }
}

extension UIColor {
    convenience init(hex: UInt32, alpha: CGFloat = 1.0) {
        let red = CGFloat((hex & 0xFF0000) >> 16) / 255.0
        let green = CGFloat((hex & 0x00FF00) >> 8) / 255.0
        let blue = CGFloat(hex & 0x0000FF) / 255.0
        self.init(red: red, green: green, blue: blue, alpha: alpha)
    }
}
