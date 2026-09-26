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
        .accessibilityLabel(localize(key: "login_email"))
        .accessibilityIdentifier("signIn.login")
        .font(.system(size: 24.0))
        .autocorrectionDisabled()

        SecureInputView(
          localize(key: "password"),
          text: $password,
          accessibilityIdentifier: "signIn.password"
        )
        .textContentType(.password)
        .font(.system(size: 24.0))
      }

      Text(errorText ?? "").foregroundStyle(.red)

      Button(action: signIn) {
        Text(localize(key: "sign_in")).frame(maxWidth: .infinity)
      }
      .accessibilityIdentifier("signIn.submit")
      .disabled(login.isEmpty || password.isEmpty)
      .padding()
      .background(Color.accentColor)
      .foregroundColor(.white)
      .glassEffect()
      .cornerRadius(25)
      .padding(.horizontal, 20)
    }.padding(.top, 15)
  }

  func signIn() {
    Task {
      do {
        let isEmail = login.contains("@")
        let rLogin: String? = if isEmail { nil } else { login }
        let rEmail: String? = if isEmail { login } else { nil }
        let loginRequest = LoginRequestDto(
          login: rLogin,
          email: rEmail,
          password: password
        )
        let result = try await AccountClient().login(request: loginRequest)
        do {
          let authToken =
            (try JSONSerialization.jsonObject(with: Data(result.utf8))
            as! [String: Any])["token"] as! String
          KeychainService.saveToken(authToken)
          errorText = ""
          print("success")
          return
        } catch _ {
          errorText = localize(key: result)
          return
        }

      } catch let error {
        print(error)
        return
      }
    }
  }

}

#Preview {
  SignInView()
}
