//
//  InfoController.swift
//  Notely Capture
//
//  Created by Tosin Onikute on 07/06/2025.
//  Copyright © 2025 orgName. All rights reserved.
//

import Foundation
import shared
import SwiftUI


struct InfoScreenController : UIViewControllerRepresentable  {
    private var onNavigateBack:() -> Void
    
    init(onNavigateBack: @escaping () -> Void) {
        self.onNavigateBack = onNavigateBack
    }
    
    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {
     
    }

    func makeUIViewController(context: Context) -> some UIViewController {
        InfoControllerKt.InfoController(
            onNavigateBack: {
                onNavigateBack()
            }
         )
     }
}
