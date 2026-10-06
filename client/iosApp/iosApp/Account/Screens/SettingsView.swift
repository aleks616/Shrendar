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

	@State private var userData: UserDto?
	@State private var username = ""
	@State private var email = ""
	@State private var birthdate: Foundation.Date?

	@State private var isLoading = true
	@State private var errorText = ""

	@State private var modalOpen = false
	@State private var modalEmail = ""
	@State private var login = ""
	@State private var password = ""

	var minDate: Foundation.Date {
		calendar.startOfDay(
			for: calendar.date(
				byAdding: .year,
				value: -120,
				to: Foundation.Date()
			)!
		)
	}

	var maxDate: Foundation.Date {
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
		return birthdate < minDate || birthdate > maxDate
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
							TextField(localize(key: "username"), text: $username)
								.textContentType(.username)
								.autocorrectionDisabled()
								.accessibilityIdentifier("settings.username")

							if isUsernameInvalid {
								Text(
									localize(key: "username_invalid")
								)
								.font(.footnote)
								.foregroundStyle(.red)
							}

							TextField(localize(key: "email"), text: $email)
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
									get: { birthdate ?? maxDate },
									set: { birthdate = $0 }
								),
								in: minDate...maxDate,
								displayedComponents: .date
							)
							.accessibilityIdentifier("settings.birthdate")
						}

						if !errorText.isEmpty {
							Text(errorText)
								.foregroundStyle(.red)
						}

						Button(action: submitChanges) {
							Text(localize(key:"save_changes")).frame(maxWidth: .infinity)
						}
						.disabled(
							isUsernameInvalid
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
							modalOpen = true
						} label: {
							Text(localize(key:"delete_account")).frame(maxWidth: .infinity)
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
			.navigationTitle(localize(key: "settings"))
			.sheet(isPresented: $modalOpen) {
				NavigationStack {
					Form {
						TextField(localize(key: "email"), text: $modalEmail)
							.textContentType(.emailAddress)
							.keyboardType(.emailAddress)
							.autocorrectionDisabled()
							.accessibilityIdentifier("settings.delete.email")

						TextField(localize(key: "login"), text: $login)
							.textContentType(.username)
							.autocorrectionDisabled()
							.accessibilityIdentifier("settings.delete.login")

						SecureField(localize(key: "password"), text: $password)
							.textContentType(.password)
							.accessibilityIdentifier("settings.delete.password")
					}
					.toolbar {
						ToolbarItem(placement: .cancellationAction) {
							Button(localize(key: "cancel")) {
								modalOpen = false
							}
						}
						ToolbarItem(placement: .confirmationAction) {
							Button(localize(key: "delete_account")) {
								deleteAccount()
							}
							.disabled(login.isEmpty || password.isEmpty)
						}
					}
				}
				.presentationDetents([.large])
			}
			.task {
				await getUserData()
			}
		}
	}

	func getUserData() async {
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

			userData = user
			username = user.username ?? ""
			email = user.email ?? ""
			birthdate = foundationDate(from: user.birthDate)
			errorText = ""
		} catch {
			errorText = error.localizedDescription
		}
		isLoading = false
	}

	func updateUsername(token: String) async throws {
		let result = try await AccountClient().updateUsername(
			token: token,
			newUsername: username
		)
		if result != "username_changed" {
			errorText = localize(key: result)
		}
	}

	func updateEmail(token: String) async throws {
		let result = try await AccountClient().updateEmail(
			token: token,
			newEmail: email
		)
		if result != "email_changed" {
			errorText = localize(key: result)
		}
	}

	func updateBirthdate(token: String, birthdate: Foundation.Date) async throws {
		let result = try await AccountClient().addBirthday(
			token: token,
			birthday: kotlinDate(from: birthdate)
		)
		if result != "birthday_added" {
			errorText = localize(key: result)
		}
	}

	func submitChanges() {
		guard let token = KeychainService.retrieveToken(), !token.isEmpty else {
			errorText = localize(key: "something_wrong")
			return
		}

		errorText = ""

		Task {
			do {
				if username != userData?.username {
					try await updateUsername(token: token)
				}

				if email != userData?.email {
					try await updateEmail(token: token)
				}

				if birthdate != foundationDate(from: userData?.birthDate),
					let birthdate
				{
					try await updateBirthdate(token: token, birthdate: birthdate)
				}
			} catch {
				errorText = error.localizedDescription
			}
		}
	}

	func deleteAccount() {
		guard let token = KeychainService.retrieveToken(), !token.isEmpty else {
			errorText = localize(key: "something_wrong")
			modalOpen = false
			return
		}

		let request = LoginRequestDto(
			login: login.isEmpty ? nil : login,
			email: modalEmail.isEmpty ? nil : modalEmail,
			password: password
		)
		modalEmail = ""
		login = ""
		password = ""

		Task {
			do {
				modalOpen = false
				let langCode: String = lang.identifier.uppercased()
				let result = try await AccountClient().deleteAccount(
					token: token,
					request: request,
					lang: langCode
				)
				if result == "confirmed" {
					KeychainService.removeToken()
					dismiss()
				} else {
					errorText = localize(key: result)
				}
			} catch {
				errorText = error.localizedDescription
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
