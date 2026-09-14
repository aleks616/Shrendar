import SwiftUI
import SharedLogic

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
                .task {
                    GenreApi().getAll { genres, error in
                        if let error {
                            print("Unable to fetch genres: \(error)")
                        } else {
                            print(genres ?? [])
                        }
                    }
                }
        }
    }
}