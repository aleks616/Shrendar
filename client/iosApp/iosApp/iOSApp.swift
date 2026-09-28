import GoogleSignIn
import SharedLogic
import SwiftUI

@main
struct iOSApp: App {
	@State private var resetURL: URL?
	
	var body: some Scene {
		WindowGroup {
			Group {
				if resetURL != nil {
					PasswordResetView(resetURL: resetURL)
				}
				else {
					WelcomeView()
				}
			}
			.onOpenURL { url in
				if url.scheme == "shrendar",
					url.host == "reset-password" {
					resetURL = url
				}
				else {
					GIDSignIn.sharedInstance.handle(url)
				}
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
