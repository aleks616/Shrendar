//
//  PasswordResetView.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 28/09/2026.
//

import SwiftUI
import SharedLogic

struct PasswordResetView: View {
	private enum Field: Int, CaseIterable {
		case password, confirmPassword
	}
	var resetURL:URL?
	@State private var password: String = ""
	@State private var confirmPassword: String = ""
	@State private var errorText: String? = ""
	@FocusState private var focusedField: Field?

	var body: some View {
		NavigationStack {
			VStack {
				Text(localize(key: "create_new_password"))
					.font(.system(size: 28.0, weight: .bold))
				Form {
					SecureInputView(
						localize(key: "password"),
						text: $password,
						accessibilityIdentifier: "register.password"
					)
					.textContentType(.password)
					.accessibilityIdentifier("resetPassword.password")
					.focused($focusedField, equals: .password)
					.font(.system(size: 24.0))

					SecureInputView(
						localize(key: "re_enter_password"),
						text: $confirmPassword,
						accessibilityIdentifier: "register.confirmPassword"
					)
					.textContentType(.password)
					.accessibilityIdentifier("resetPassword.confirmPassword")
					.focused($focusedField, equals: .confirmPassword)
					.font(.system(size: 24.0))
				}

				Text(errorText ?? "").foregroundStyle(.red)

				Button(action: createPassword) {
					Text(localize(key: "change_password")).frame(maxWidth: .infinity)
				}
				.accessibilityIdentifier("register.submit")
				.disabled(
					password.isEmpty
						|| confirmPassword.isEmpty
				)
				.padding()
				.background(Color.accentColor)
				.foregroundColor(.white)
				.glassEffect()
				.cornerRadius(25)
				.padding(.horizontal, 20)

			}.padding(.top, 15)
		}
	}

	func createPassword() {
		Task {
			do{
				let components:NSURLComponents? = NSURLComponents(url: resetURL!, resolvingAgainstBaseURL: true)
				let queryItems = components!.queryItems
				
				let code = queryItems?.first(where: { $0.name == "code" })?.value
				let email = queryItems?.first(where: { $0.name == "account" })?.value
				
				if code == nil || email == nil {
					errorText = "invalid_parameters" //todo
					return
				}
				
				if password != confirmPassword{
					errorText = localize(key: "passwords_dont_match")
					return
				}

				let registerValidator = RegisterValidator()
				let passwordValid = registerValidator.isPasswordValid(
					password: password
				)
				if passwordValid != true {
					errorText = localize(key: "invalid_password")
					return
				}
				let resetPasswordRequest = ResetPasswordDto(
					email: email!,
					newPassword: password,
					code: code!,
					language: "EN"
				)
				
				let result = try await AccountClient().resetPassword(request: resetPasswordRequest)
				print(result)
			}
			catch let error {
				print(error)
				return
			}
		}
	}

}

#Preview {
	PasswordResetView(resetURL: URL(string: "https://shrendarclient.shares.zrok.io/reset-password?code=123456&account=user@example.com"))
}
