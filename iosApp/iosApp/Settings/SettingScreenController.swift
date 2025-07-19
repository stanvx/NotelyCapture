//
//  SettingScreenController.swift
//  Notely Capture
//
//  Created by Tosin Onikute on 07/06/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import Foundation
import shared
import SwiftUI


struct SettingScreenController : UIViewControllerRepresentable  {
    private var onNavigateBack:() -> Void
    
    init(onNavigateBack: @escaping () -> Void) {
        self.onNavigateBack = onNavigateBack
    }
    
    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {
     
    }

    func makeUIViewController(context: Context) -> some UIViewController {
        SettingsControllerKt.SettingsController(
            onNavigateBack: {
                onNavigateBack()
            }
         )
     }
}
