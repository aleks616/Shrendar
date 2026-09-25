//
//  RegisterView.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 16/09/2026.
//

import AuthenticationServices
import GoogleSignIn
import GoogleSignInSwift
import SharedLogic
import SwiftUI
import SwiftUIOTPEntry

struct RegisterView: View {
	let lang = Locale.current.language.languageCode ?? "EN"
	private enum Field: Int, CaseIterable {
		case email, login, password, confirmPassword
	}
	@State private var email: String = ""
	@State private var login: String = ""
	@State private var password: String = ""
	@State private var confirmPassword: String = ""
	@State private var code: String = ""

	@FocusState private var focusedField: Field?
	@State var isDismissKeyboard: Bool = false

	@State private var errorText: String? = ""
	@State private var codeSent: Bool = false
	@State private var timerOn: Bool = false
	@State private var resendCountdown: Int = 60
	@State private var confirmed: Bool = false

	let timer: Timer.TimerPublisher = Timer.publish(
		every: 1.0,
		on: .main,
		in: .common
	)

	let model: ModelUISwiftUIOTPEntry = .init(
		font: .systemFont(ofSize: 20),
		textAccessibilityForEmptyBox: "Empty box",
		textAccessibilityPosition: "Position",
		count: 6,
		spacing: 8,
		colorFocused: .red,
		colorEmpty: .gray,
		colorFill: .green,
		size: 50
	)

	var body: some View {
		NavigationStack {
			VStack {
				Text(localize(key: "create_account"))
					.font(.system(size: 28.0, weight: .bold))
				Text(localize(key: "sign_up_to_continue"))
					.font(.system(size: 20.0))
				Spacer()
				Form {
					TextField(
						localize(key: "email_address"),
						text: $email
					)
					.keyboardType(.emailAddress)
					.textContentType(.emailAddress)
					.font(.system(size: 24.0))
					.focused($focusedField, equals: .email)
					.autocorrectionDisabled()

					TextField(
						localize(key: "login"),
						text: $login
					)
					.textContentType(.username)
					.font(.system(size: 24.0))
					.focused($focusedField, equals: .login)
					.autocorrectionDisabled()

					SecureInputView(
						localize(key: "password"),
						text: $password
					)
					.textContentType(.password)
					.focused($focusedField, equals: .password)
					.font(.system(size: 24.0))

					SecureInputView(
						localize(key: "re_enter_password"),
						text: $confirmPassword
					)
					.textContentType(.password)
					.focused($focusedField, equals: .confirmPassword)
					.font(.system(size: 24.0))

				}.toolbar {
					ToolbarItem(placement: .keyboard) {
						Button(localize(key: "done")) {
							focusedField = nil
						}
					}
				}

				Text(errorText ?? "").foregroundStyle(.red)

				Button(action: validateFields) {
					Text(localize(key: "sign_up")).frame(maxWidth: .infinity)
				}
				.disabled(
					email.isEmpty || login.isEmpty || password.isEmpty
						|| confirmPassword.isEmpty
						|| (resendCountdown > 0 && codeSent) || confirmed
				)
				.padding()
				.background(Color.accentColor)
				.foregroundColor(.white)
				.glassEffect()
				.cornerRadius(25)
				.padding(.horizontal, 20)

				if codeSent {
					Text(localize(key: "verification_code_sent"))
					if timerOn {
						Text("\(localize(key: "resend_code_in")) \(resendCountdown)")
					}
					Button(action: validateFields) {
						Text(localize(key: "resend_code"))
					}
					.disabled(resendCountdown > 0)

					ViewSwiftUIOTPEntry(
						model: model,
						number: $code,
						isDismissKeyboard: $isDismissKeyboard
					)
					Button(action: confirmAccount) {
						Text(localize(key: "confirm_account"))
					}
					.buttonStyle(.glass)
					.disabled(code.count < 6)
				}
				if confirmed {
					Text(localize(key: "account_created"))
				}

				LabelledDivider(label: localize(key: "or"))
				//			GoogleSignInButton(
				//				scheme: .light,
				//				state: .normal,
				//				action: {handleGoogleSignInButton()})
				//			.frame(width: 280, height: 45)
				//
				//			SignInWithAppleButton(.continue){
				//				request in request.requestedScopes=[.email]
				//			} onCompletion: { result in
				//				switch result {
				//					case .success(let authorization):
				//						print("\(localize(key: "authorization_successful")): \(authorization)")
				//					case .failure(let error):
				//						print("\(localize(key: "authorization_failed"))): \(error.localizedDescription)")
				//				}
				//			}.frame(width: 280, height: 45)

				//Text(localize(key: "special_sign_in_later"))
				//Spacer()
				HStack {
					Text(localize(key: "already_have_account"))
					NavigationLink(destination: SignInView()) {
						Text(localize(key: "sign_in"))
					}
				}

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

	func validateFields() {
		Task {
			let registerValidator = RegisterValidator()
			do {
				let loginValid = try await registerValidator.validateLogin(
					login: login
				)
				if loginValid != nil {
					errorText = localize(key: loginValid!)
					return
				}

				let emailValid = try await registerValidator.validateEmail(
					email: email
				)
				if emailValid != nil {
					errorText = localize(key: emailValid!)
					return
				}

				if password != confirmPassword {
					errorText = localize(key: "passwords_dont_match")
					return
				}

				let passwordValid = registerValidator.isPasswordValid(
					password: password
				)
				if passwordValid != true {
					errorText = localize(key: "invalid_password")
				}
				errorText = ""
			} catch let error {
				print(error)
				return
			}
			createAccount()
		}
	}

	func createAccount() {
		Task {
			do {
				let langCode: String = lang.identifier.uppercased()
				let registerRequest = RegisterRequestDto(
					login: login,
					displayName: login,
					email: email,
					password: password,
					language: langCode
				)
				let result = try await RegisterClient().register(
					request: registerRequest
				)
				if result == "verification_code_sent" {
					errorText = ""
					codeSent = true
					timerOn = true
				} else if result == "something_wrong" {
					errorText = localize(key: "something_wrong")
				}

			} catch let error {
				print(error)
				return
			}
		}
	}

	func confirmAccount() {
		Task {
			do {
				let langCode: String = lang.identifier.uppercased()
				let registerRequest = RegisterRequestDto(
					login: login,
					displayName: login,
					email: email,
					password: password,
					language: langCode
				)
				let confirmationResult = try await RegisterClient().registerConfirm(
					request: registerRequest,
					code: code
				)
				print(confirmationResult)
				if confirmationResult == "account_created" {
					confirmed = true
					timerOn = false
					codeSent = false
				} else {
					errorText = localize(key: confirmationResult)
				}

			} catch let error {
				print(error)
				return
			}
		}
	}

	//	func handleGoogleSignInButton() {
	//		guard let rootViewController = UIApplication.shared.rootViewController else {
	//			// Handle error
	//			return
	//		}
	//
	//		GIDSignIn.sharedInstance.signIn(withPresenting: rootViewController) { signInResult, error in
	//			guard error == nil else { print(error ?? "none");return}
	//			guard let signInResult = signInResult else { return }
	//
	//			let user = signInResult.user
	//			//let emailAddress = user.profile?.email
	//			signInResult.user.refreshTokensIfNeeded { user, error in
	//				guard error == nil else { return }
	//				guard let user = user else { return }
	//
	//				let idToken = user.idToken
	//				print(idToken)
	//				// Send ID token to backend (example below).
	//			}
	//		}
	//	}

}

#Preview {
	RegisterView().preferredColorScheme(.dark)
}
