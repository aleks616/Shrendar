import SwiftUI
import SharedLogic
import GoogleSignIn

@main
struct iOSApp: App {
    var body: some Scene {
        WindowGroup {
            SignInView()
              .onOpenURL { url in
                        GIDSignIn.sharedInstance.handle(url)
                      }
              .onAppear {
                        GIDSignIn.sharedInstance.restorePreviousSignIn { user, error in
                          // Check if `user` exists; otherwise, do something with `error`
                        }
                      }
               /* .task {
                    GenreApi().getAll { genres, error in
                        if let error {
                            print("Unable to fetch genres: \(error)")
                        } else {
                            print(genres ?? [])
                        }
                    }
                }*/
        }
    }
}
