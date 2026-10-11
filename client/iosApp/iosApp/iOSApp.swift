import GoogleSignIn
import SharedLogic
import SwiftUI

@main
struct iOSApp: App {
	@State private var resetURL: URL?

	private var testView: String? {
		ProcessInfo.processInfo.arguments
			.first(where: { $0.hasPrefix("--test-view=") })?
			.replacingOccurrences(of: "--test-view=", with: "")
	}
	
	var body: some Scene {
		WindowGroup {
			Group {
				if resetURL != nil {
					PasswordResetView(resetURL: resetURL)
				}
				else if let testView {
					switch testView {
					case "register":
						RegisterView()
					case "signIn":
						SignInView()
					case "requestPasswordReset":
						RequestPasswordResetView()
					case "passwordReset":
						PasswordResetView(
							resetURL: URL(
								string: "shrendar://reset-password?code=123456&account=alice%40example.com"
							)
						)
					case "settings":
						SettingsView()
					case "profile":
						UserProfileView(login: "")
					case "artist":
						ArtistDataView(artistId: 144)
					case "band":
						BandDataView(bandId: 21)
					case "album":
						AlbumDataView(albumId: 255)
					case "event":
						EventDataView(eventId: 2)
					default:
						WelcomeView()
					}
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
