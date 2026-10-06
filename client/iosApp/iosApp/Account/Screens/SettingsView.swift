//
//  Settings.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 06/10/2026.
//

import Foundation
import SharedLogic
import SwiftUI

struct SettingsView: View {
	let calendar = Calendar.current
	let lang = Locale.current.language.languageCode ?? "EN"

	@Environment(\.dismiss) private var dismiss

	@State private var username = ""
	@State private var email = ""
	@State private var birthdate: Foundation.Date?
	@State private var originalUsername = ""
	@State private var originalEmail = ""
	@State private var originalBirthdate: Foundation.Date?

	@State private var isLoading = true
	@State private var isSaving = false
	@State private var errorText = ""

	@State private var showingDeleteSheet = false
	@State private var deleteEmail = ""
	@State private var deleteLogin = ""
	@State private var deletePassword = ""

	var minimumBirthdate: Foundation.Date {
		calendar.startOfDay(
			for: calendar.date(
				byAdding: .year,
				value: -120,
				to: Foundation.Date()
			)!
		)
	}

	var maximumBirthdate: Foundation.Date {
		calendar.startOfDay(
			for: calendar.date(byAdding: .year, value: -13, to: Foundation.Date())!
		)
	}

	var isUsernameInvalid: Bool {
		!username.isEmpty && !(4...50).contains(username.count)
	}

	var isEmailInvalid: Bool {
		!email.isEmpty && !email.contains("@")
	}

	var isBirthdateInvalid: Bool {
		guard let birthdate else { return false }
		return birthdate < minimumBirthdate || birthdate > maximumBirthdate
	}

	var hasBirthdateChanged: Bool {
		guard let birthdate else {
			return originalBirthdate != nil
		}
		guard let originalBirthdate else { return true }
		return calendar.dateComponents(
			[.year, .month, .day],
			from: birthdate
		)
			!= calendar.dateComponents(
				[.year, .month, .day],
				from: originalBirthdate
			)
	}

	var body: some View {
		NavigationStack {
			Group {
				if isLoading {
					ProgressView()
						.controlSize(.large)
						.frame(maxWidth: .infinity, maxHeight: .infinity)
				} else {
					VStack {
						Form {
							TextField("Username", text: $username)
								.textContentType(.username)
								.autocorrectionDisabled()
								.accessibilityIdentifier("settings.username")

							if isUsernameInvalid {
								Text(
									"Username length must be between 4 and 50 characters"
								)
								.font(.footnote)
								.foregroundStyle(.red)
							}

							TextField("Email", text: $email)
								.textContentType(.emailAddress)
								.keyboardType(.emailAddress)
								.disabled(true)
								.accessibilityIdentifier("settings.email")

							if isEmailInvalid {
								Text(localize(key: "invalid_email"))
									.font(.footnote)
									.foregroundStyle(.red)
							}

							DatePicker(
								localize(key: "date"),
								selection: Binding(
									get: { birthdate ?? maximumBirthdate },
									set: { birthdate = $0 }
								),
								in: minimumBirthdate...maximumBirthdate,
								displayedComponents: .date
							)
							.accessibilityIdentifier("settings.birthdate")
						}

						if !errorText.isEmpty {
							Text(errorText)
								.foregroundStyle(.red)
						}

						Button(action: saveChanges) {
							Text("Save changes").frame(maxWidth: .infinity)
						}
						.disabled(
							isSaving
								|| isUsernameInvalid
								|| isEmailInvalid
								|| isBirthdateInvalid
						)
						.accessibilityIdentifier("settings.save")
						.padding()
						.background(Color.accentColor)
						.foregroundColor(.white)
						.glassEffect()
						.cornerRadius(25)
						.padding(.horizontal, 20)

						Spacer()

						NavigationLink(destination: RequestPasswordResetView()) {
							Text(localize(key: "change_password"))
								.frame(maxWidth: .infinity)
						}
						.accessibilityIdentifier("settings.changePassword")
						.padding()
						.background(Color.secondary)
						.foregroundColor(.white)
						.glassEffect()
						.cornerRadius(25)
						.padding(.horizontal, 20)

						Spacer()

						Button {
							showingDeleteSheet = true
						} label: {
							Text("Delete account").frame(maxWidth: .infinity)
						}
						.accessibilityIdentifier("settings.deleteAccount")
						.padding()
						.background(Color.red)
						.foregroundColor(.white)
						.glassEffect()
						.cornerRadius(25)
						.padding(.horizontal, 20)
					}
				}
			}
			.navigationTitle("Settings")
			.sheet(isPresented: $showingDeleteSheet) {
				NavigationStack {
					Form {
						TextField("Email", text: $deleteEmail)
							.textContentType(.emailAddress)
							.keyboardType(.emailAddress)
							.autocorrectionDisabled()
							.accessibilityIdentifier("settings.delete.email")

						TextField(localize(key: "login"), text: $deleteLogin)
							.textContentType(.username)
							.autocorrectionDisabled()
							.accessibilityIdentifier("settings.delete.login")

						SecureField(localize(key: "password"), text: $deletePassword)
							.textContentType(.password)
							.accessibilityIdentifier("settings.delete.password")
					}
					.toolbar {
						ToolbarItem(placement: .cancellationAction) {
							Button("Cancel") {
								showingDeleteSheet = false
							}
						}
						ToolbarItem(placement: .confirmationAction) {
							Button("Delete account") {
								deleteAccount()
							}
							.disabled(deleteLogin.isEmpty || deletePassword.isEmpty)
						}
					}
				}
				.presentationDetents([.large])
			}
			.task {
				await loadUserData()
			}
		}
	}

	func loadUserData() async {
		guard isLoading else { return }
		guard let token = KeychainService.retrieveToken(), !token.isEmpty else {
			errorText = localize(key: "something_wrong")
			isLoading = false
			return
		}

		do {
			let user = try await AccountClient().getUserData(token: token)
			guard user.login != nil else {
				errorText = localize(key: "something_wrong")
				isLoading = false
				return
			}

			username = user.username ?? ""
			email = user.email ?? ""
			birthdate = foundationDate(from: user.birthDate)
			originalUsername = username
			originalEmail = email
			originalBirthdate = birthdate
			errorText = ""
		} catch {
			errorText = error.localizedDescription
		}
		isLoading = false
	}

	func saveChanges() {
		guard let token = KeychainService.retrieveToken(), !token.isEmpty else {
			errorText = localize(key: "something_wrong")
			return
		}

		isSaving = true
		errorText = ""

		Task {
			do {
				if username != originalUsername {
					let result = try await AccountClient().updateUsername(
						token: token,
						newUsername: username
					)
					guard result == "username_changed" else {
						errorText = localize(key: result)
						isSaving = false
						return
					}
					originalUsername = username
				}

				if email != originalEmail {
					let result = try await AccountClient().updateEmail(
						token: token,
						newEmail: email
					)
					guard result == "email_changed" else {
						errorText = localize(key: result)
						isSaving = false
						return
					}
					originalEmail = email
				}

				if hasBirthdateChanged, let birthdate {
					let result = try await AccountClient().addBirthday(
						token: token,
						birthday: kotlinDate(from: birthdate)
					)
					guard result == "birthday_added" else {
						errorText = localize(key: result)
						isSaving = false
						return
					}
					originalBirthdate = birthdate
				}

				isSaving = false
			} catch {
				errorText = error.localizedDescription
				isSaving = false
			}
		}
	}

	func deleteAccount() {
		guard let token = KeychainService.retrieveToken(), !token.isEmpty else {
			errorText = localize(key: "something_wrong")
			showingDeleteSheet = false
			return
		}

		let request = LoginRequestDto(
			login: deleteLogin.isEmpty ? nil : deleteLogin,
			email: deleteEmail.isEmpty ? nil : deleteEmail,
			password: deletePassword
		)

		Task {
			do {
                showingDeleteSheet = false
				let langCode: String = lang.identifier.uppercased()
				let result = try await AccountClient().deleteAccount(
					token: token,
					request: request,
					lang: langCode
				)
				if result == "confirmed" {
					KeychainService.removeToken()
					showingDeleteSheet = false
					dismiss()
				} else {
					errorText = localize(key: result)
					showingDeleteSheet = false
				}
			} catch {
				errorText = error.localizedDescription
				showingDeleteSheet = false
			}
		}
	}

	func foundationDate(from date: SharedLogic.Date?) -> Foundation.Date?
	{
		guard let date else { return nil }
		return calendar.date(
			from: DateComponents(
				year: Int(date.year),
				month: Int(date.month),
				day: Int(date.day)
			)
		)
	}

	func kotlinDate(from date: Foundation.Date) -> SharedLogic.Date {
		let components = calendar.dateComponents(
			[.year, .month, .day],
			from: date
		)
		return SharedLogic.Date(
			year: Int32(components.year!),
			month: Int32(components.month!),
			day: Int32(components.day!)
		)
	}
}

#Preview {
	SettingsView()
}
