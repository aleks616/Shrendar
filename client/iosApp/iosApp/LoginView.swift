//
//  LoginView.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 24/09/2026.
//

import SharedLogic
import SwiftUI

struct SignInView: View {
	let lang = Locale.current.language.languageCode ?? "EN"
	@State private var login: String = ""
	@State private var password: String = ""
	@State private var errorText: String? = ""

	var body: some View {
		VStack {
			Text(localize(key: "sign_in"))
				.font(.system(size: 28.0, weight: .bold))
			Spacer()
			Form {
				TextField(
					localize(key: "login_email"),
					text: $login
				)
				.keyboardType(.emailAddress)
				.textContentType(.emailAddress)
				.font(.system(size: 24.0))
				.autocorrectionDisabled()

				SecureField(
					localize(key: "password"),
					text: $password
				)
				.textContentType(.password)
				.font(.system(size: 24.0))
			}

			Text(errorText ?? "").foregroundStyle(.red)

			Button(action: signIn) {
				Text(localize(key: "sign_in"))
			}.buttonStyle(.glass)
				.frame(minHeight:50)
				.disabled(login.isEmpty || password.isEmpty)
		}.padding(.top, 15)
	}

	func signIn() {
		Task {
			do {
				let isEmail = login.contains("@")
				let rLogin:String?=if isEmail {nil} else {login}
				let rEmail:String?=if isEmail {login} else {nil}
				let loginRequest = LoginRequestDto(
					login: rLogin,
					email: rEmail,
					password: password
				)
				let result = try await AccountClient().login(request: loginRequest)
				do{
					let token = (try JSONSerialization.jsonObject(with: Data(result.utf8)) as! [String: Any])["token"] as! String
					let query: [String: Any] = [
						kSecClass as String: kSecClassGenericPassword,
						kSecAttrAccount as String: "userAccount",
						kSecAttrAccessible as String: kSecAttrAccessibleWhenUnlocked,
						kSecUseDataProtectionKeychain as String: true,
						kSecValueData as String: token
					]
					let status = SecItemAdd(query as CFDictionary, nil)
					guard status == errSecSuccess else {
						//print(error)
						return
					}
					errorText=""
					print("success")
					return
				}
				catch _ {
					errorText=localize(key: result)
					return
				}

			}
			catch let error {
				print(error)
				return
			}
		}
	}

}

#Preview {
	SignInView()
}
