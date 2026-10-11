//
//  ResetPasswordView.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 27/09/2026.
//

import SharedLogic
import SwiftUI

struct RequestPasswordResetView: View {
	let lang = Locale.current.language.languageCode ?? "EN"
	private enum Field: Int, CaseIterable {
		case login
	}
	@State private var login: String = ""
	@State private var code: String = ""
	@State private var errorText: String? = ""

	@FocusState private var focusedField: Field?
	@State private var codeSent: Bool = false
	@State private var timerOn: Bool = false
	@State private var resendCountdown: Int = 60

	let timer: Timer.TimerPublisher = Timer.publish(
		every: 1.0,
		on: .main,
		in: .common
	)

	var body: some View {
		NavigationStack {
			VStack {
				Text(localize(key: "forgot_password_question"))
					.font(.system(size: 28.0, weight: .bold))
				Form {
					TextField(
						localize(key: "login_email"),
						text: $login
					)
					.keyboardType(.emailAddress)
					.textContentType(.emailAddress)
					.accessibilityLabel(localize(key: "login_email"))
					.accessibilityIdentifier("requestPasswordReset.login")
					.font(.system(size: 24.0))
					.focused($focusedField, equals: .login)
					.autocorrectionDisabled()
				}
				.toolbar {
					ToolbarItem(placement: .keyboard) {
						Button(localize(key: "done")) {
							focusedField = nil
						}
					}
				}
				Button(action: requestPasswordReset) {
					Text(localize(key: "reset_password")).frame(maxWidth: .infinity)
				}
				.accessibilityIdentifier("requestPasswordReset.submit")
				.disabled(login.isEmpty || timerOn)
				.padding()
				.background(Color.accentColor)
				.foregroundColor(.white)
				.glassEffect()
				.cornerRadius(25)
				.padding(.horizontal, 20)

				Text(errorText ?? "").foregroundStyle(.red)

				if codeSent {
					Text(localize(key: "password_link_sent"))
					if timerOn {
						Text("\(localize(key: "resend_code_in")) \(resendCountdown)")
					}
					Button(action: requestPasswordReset) {
						Text(localize(key: "resend_code"))
					}
					.disabled(resendCountdown > 0)
				}
				Spacer()

			}
			.padding(.top, 15)
			.onReceive(
				timer.autoconnect(),
				perform: { _ in
					if timerOn {
						if resendCountdown > 0 {
							resendCountdown = resendCountdown - 1
						} else {
							timerOn = false
						}
					}
				}
			)
		}
	}

	func requestPasswordReset() {
		Task {
			do {
				let langCode: String = lang.identifier.uppercased()
				let result = try await AccountClient().requestPasswordReset(
					accountKey: login,
					language: langCode
				)
				if result != "password_link_sent" {
					if result == "too_many_user_requests" {
						resendCountdown = 240
						timerOn = true
					}
					errorText = localize(key: result)
					return
				}
				errorText = ""
				codeSent = true
				timerOn = true
				resendCountdown = 60
				return
			} catch let error {
				print(error)
				return
			}
		}
	}
}

#Preview {
	RequestPasswordResetView()
}
