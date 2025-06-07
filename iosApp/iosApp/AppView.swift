//
//  AppView.swift
//  iosApp
//

import SwiftUI

struct AppView: View {
    private static let defaultNoteId = -1
    @State private var navigateToSecondView = false
    @State private var navigateToInfoView = false
    @State private var navigateToSettingsView = false
    @State private var selectedNoteIdState = defaultNoteId
    @State private var refreshKey = UUID()
    
    var body: some View {
        NavigationView {
            VStack {
                NoteListScreenView(
                    onFloatingButtonClicked: {
                        navigateToSecondView = true
                        selectedNoteIdState = AppView.defaultNoteId
                    },
                    onInfoClicked: {
                        navigateToInfoView = true
                    },
                    onSettingsClicked: {
                        navigateToSettingsView = true
                    },
                    onNoteClicked: { it in
                        selectedNoteIdState = it
                        navigateToSecondView = true
                    },
                    refreshKey: refreshKey
                )
                
                NavigationLink(
                    destination: SecondView(
                        navigateToSecondView: $navigateToSecondView,
                        selectedNoteId: selectedNoteIdState,
                        onNoteSaved: {
                            refreshKey = UUID()
                        }
                    ), isActive: $navigateToSecondView
                ) {
                    EmptyView()
                }
                
                NavigationLink(
                    destination: InfoView(
                        navigateToInfoView: $navigateToInfoView,
                        onNavigateBack: {
                            refreshKey = UUID()
                        }
                    ), isActive: $navigateToInfoView
                ) {
                    EmptyView()
                }
                
                NavigationLink(
                    destination: SettingsView(
                        navigateToSettingsView: $navigateToSettingsView,
                        onNavigateBack: {
                            refreshKey = UUID()
                        }
                    ), isActive: $navigateToSettingsView
                ) {
                    EmptyView()
                }

            }
        }
        .navigationViewStyle(.stack)
    }
}

struct SecondView: View {
    @Binding var navigateToSecondView: Bool
    @State var selectedNoteId: Int
    var onNoteSaved: () -> Void
    
    var body: some View {
        NoteDetailScreenController(
            onNoteSaveClicked: {
                
            },
            noteId: String(selectedNoteId),
            onNavigateBack: {
                navigateToSecondView = false
                onNoteSaved()
            }
        )
        .ignoresSafeArea(.keyboard)
        .navigationBarHidden(true) // set to false to show native back button
    }
}

struct InfoView: View {
    @Binding var navigateToInfoView: Bool
    var onNavigateBack: () -> Void
    
    var body: some View {
        InfoScreenController(
            onNavigateBack: {
                navigateToInfoView = false
                onNavigateBack()
            }
        )
        .ignoresSafeArea(.keyboard)
        .navigationBarHidden(true)
    }
}

struct SettingsView: View {
    @Binding var navigateToSettingsView: Bool
    var onNavigateBack: () -> Void
    
    var body: some View {
        SettingScreenController(
            onNavigateBack: {
                navigateToSettingsView = false
                onNavigateBack()
            }
        )
        .ignoresSafeArea(.keyboard)
        .navigationBarHidden(true)
    }
}

struct AppView_Previews: PreviewProvider {
    static var previews: some View {
        AppView()
    }
}
