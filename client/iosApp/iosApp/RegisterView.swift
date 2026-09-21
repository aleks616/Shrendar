//
//  RegisterView.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 16/09/2026.
//

import SwiftUI
import GoogleSignIn
import GoogleSignInSwift
import AuthenticationServices
import SharedLogic

struct RegisterView: View {
	@State private var email:String = ""
	@State private var login:String = ""
	@State private var password:String = ""
	@State private var confirmPassword:String = ""
	@State private var errorText:String? = ""
	
	var body: some View {
		VStack(){
			Text( localize(key: "create_account"))
				.font(.system(size: 28.0,weight:.bold))
			Text(localize(key: "sign_up_to_continue"))
				.font(.system(size: 20.0))
			Spacer()
			Form{
				TextField(
					localize(key: "email_address"),
					text: $email)
					.keyboardType(.emailAddress)
					.textContentType(.emailAddress)
					.font(.system(size: 24.0))
					.autocorrectionDisabled()
				
				TextField(
					localize(key: "login"),
					text: $login)
					.textContentType(.username)
					.font(.system(size: 24.0))
					.autocorrectionDisabled()
				
				SecureField(
					localize(key: "password"),
					text:$password)
					.textContentType(.password)
					.font(.system(size: 24.0))
				
				SecureField(
					localize(key: "re_enter_password"),
					text:$confirmPassword)
					.textContentType(.password)
					.font(.system(size: 24.0))
				
			}
			Text(errorText ?? "").foregroundStyle(.red)
			
			Button(action:createAccount){
				Text(localize(key: "sign_up"))
			}.buttonStyle(.glass)
				.disabled(email.isEmpty||login.isEmpty||password.isEmpty||confirmPassword.isEmpty)
			
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
			
			Text(localize(key: "special_sign_in_later"))
			Spacer()
			HStack{
				Text(localize(key: "already_have_account"))
				Button(localize(key: "sign_in")){}
			}
			
			
		}
		.padding(.top,15)
	}
	
	func createAccount(){
		Task{
			let registerValidator = RegisterValidator()
			do{
				let loginValid = try await registerValidator.validateLogin(login: login)
				if loginValid != nil {
					errorText = localize(key: loginValid!)
					return
				}
				
				let emailValid=try await registerValidator.validateEmail(email: email)
				if emailValid != nil{
					errorText = localize(key: emailValid!)
					return
				}
				
				if password != confirmPassword {
					errorText=localize(key: "passwords_dont_match")
					return
				}
				
				let passwordValid=registerValidator.isPasswordValid(password: password)
				if passwordValid != true {
					errorText=localize(key: "invalid_password")
				}
				errorText=""
			}
			catch let error{
				print(error)
				return
			}
			
			do{
				//let registerAccount = RegisterAccount()
				let lang=Locale.current.language.languageCode ?? "EN"
				let langCode:String=lang.identifier.uppercased()
				let registerRequest = RegisterRequest(login: login, displayName: login, email: email, password: password,language:langCode)
				let result = try await RegisterAccount().register(request: registerRequest)
				print(result)
				
			}
			catch let error{
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
	
	func localize(key:String) -> String{
		return LocalText().getStringDesc(resourceKey: key).localized()
	}
}

#Preview {
	RegisterView().preferredColorScheme(.dark)
}
