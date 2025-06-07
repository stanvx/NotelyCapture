//
//  ComposeView.swift
//  iosApp
//

import Foundation
import shared
import SwiftUI

struct NoteListScreen: UIViewControllerRepresentable {
private var selectedTabTitle:String
    private var onFloatingButtonClicked: () -> Void
    private var onNoteClickedFun:(Int) -> Void
    private var onFilterTabClickedFun:(String) -> Void
    private var onNoteDeleteClickedFun:(Int) -> Void
    private var onInfoClickedFun:() -> Void
    private var onSettingsClickedFun:() -> Void

    init(
        selectedTabTitle: String = "",
        onFloatingButtonClicked: @escaping () -> Void,
        onNoteClicked:@escaping (Int) -> Void,
        onFilterTabClicked:@escaping (String) -> Void,
        onNoteDeleteClicked:@escaping (Int) -> Void,
        onInfoClicked:@escaping () -> Void,
        onSettingsClicked:@escaping () -> Void
    ) {
    self.selectedTabTitle = selectedTabTitle
        self.onFloatingButtonClicked = onFloatingButtonClicked
        self.onNoteClickedFun = onNoteClicked
        self.onFilterTabClickedFun = onFilterTabClicked
        self.onNoteDeleteClickedFun = onNoteDeleteClicked
        self.onInfoClickedFun = onInfoClicked
        self.onSettingsClickedFun = onSettingsClicked
    }
  
    func updateUIViewController(_ uiViewController: UIViewControllerType, context: Context) {
    
    }
    
    func makeUIViewController(context: Context) -> some UIViewController {
        NoteListControllerKt.NoteListController(
        selectedTabTitle:selectedTabTitle,
            onFloatingActionButtonClicked: {
                onFloatingButtonClicked()
            },
            onNoteClicked: { it in
                onNoteClickedFun(Int(it))
            },
            onFilterTabClicked: { it in
                onFilterTabClickedFun(String(it))
            },
            onInfoClicked: {
                onInfoClickedFun()
            },
            onSettingsClicked: {
                onSettingsClickedFun()
            }
        )
    }
}
