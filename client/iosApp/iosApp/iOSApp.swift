import GoogleSignIn
import SharedLogic
import SwiftUI

@Observable
class AuthenticationModel {
	var isAuthenticated: Bool = UserDefaults.standard.string(forKey: "login") != nil
}

@main
struct iOSApp: App {
	@State private var resetURL: URL?
	@State private var authModel = AuthenticationModel()

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
						UserProfileView(login: "admin")
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
				else{
					TabView{
						HomepageView()
							.tabItem{
								Label(localize(key:"home"), systemImage: "house.fill")
							}
						SearchResultsScreen()
							.tabItem{
								Label(localize(key:"search"), systemImage: "magnifyingglass")
							}
						if authModel.isAuthenticated {
							let login=UserDefaults.standard.string(forKey: "login")
							UserProfileView(login:login!)
								.tabItem{
									Label(localize(key:"profile"), systemImage: "person.crop.circle")
								}
//							NotificationsView()
//								.tabItem{
//									Label("Inbox", systemImage: "bell.fill")
//								}
							SettingsView()
								.tabItem{
									Label(localize(key:"settings"), systemImage: "gearshape.fill")
								}
						}
						else {
							WelcomeView()
								.tabItem{
									Label(localize(key:"sign_in"), systemImage: "person.crop.circle")
								}
						}
								
					}.tint(.accentColor)
				}
			}
			.environment(authModel)
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
		}
	}
}
