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
			Text("Create account")
				.font(.system(size: 28.0,weight:.bold))
			Text("Sign Up to Continue")
				.font(.system(size: 20.0))
			Spacer()
			Form{
				TextField("email address", text: $email)
					.keyboardType(.emailAddress)
					.textContentType(.emailAddress)
					.font(.system(size: 24.0))
					.autocorrectionDisabled()
				
				TextField("login",text: $login)
					.textContentType(.username)
					.font(.system(size: 24.0))
					.autocorrectionDisabled()
				
				SecureField("password",text:$password)
					.textContentType(.password)
					.font(.system(size: 24.0))
				
				SecureField("re-enter password",text:$confirmPassword)
					.textContentType(.password)
					.font(.system(size: 24.0))
				
			}
			.frame(maxHeight: 800)
			Text(errorText ?? "").foregroundStyle(.red)
			
			Button(action:createAccount){
				Text("Sign Up")
			}.buttonStyle(.glass)
				.disabled(email.isEmpty||login.isEmpty||password.isEmpty||confirmPassword.isEmpty)
			
			LabelledDivider(label: "or")
			GoogleSignInButton(
				scheme: .light,
				state: .normal,
				action: {handleGoogleSignInButton()})
			.frame(width: 280, height: 45)
			
			SignInWithAppleButton(.continue){
				request in request.requestedScopes=[.email]
			} onCompletion: { result in
				switch result {
					case .success(let authorization):
						print("Authorization successful: \(authorization)")
					case .failure(let error):
						print("Authorization failed: \(error.localizedDescription)")
				}
			}.frame(width: 280, height: 45)
			
			Spacer()
			HStack{
				Text("Already have an account?")
				Button("Sign in"){}
			}
			
			
		}
		.padding(.top,15)
	}
	
	func createAccount(){
		Task{
			let registerValidator=RegisterValidator()
			do{
				let loginValid=try await registerValidator.validateLogin(login: login)
				if loginValid != nil {
					errorText = loginValid
					return
				}
				
				let emailValid=try await registerValidator.validateEmail(email: email)
				if emailValid != nil{
					errorText=emailValid
					return
				}
				
				if password != confirmPassword {
					errorText="Passwords don't match"
					return
				}
				
				let passwordValid=registerValidator.isPasswordValid(password: password)
				if passwordValid != true {
					errorText="Password has to contain at least one lowercase letter, one uppercase letter, one symbol and be between 8 and 32 characters long."
				}
			}
			catch let error{
				print(error)
				return
			}
			
			do{
				let registerAccount = RegisterAccount()
				let registerRequest = RegisterRequest(login: login, displayName: login, email: email, password: password)
				let result = try await registerAccount.register(request: registerRequest)
				print(result)
				
			}
			catch let error{
				print(error)
				return
			}
		}
	}
	
	func handleGoogleSignInButton() {
		guard let rootViewController = UIApplication.shared.rootViewController else {
			// Handle error
			return
		}
		
		GIDSignIn.sharedInstance.signIn(withPresenting: rootViewController) { signInResult, error in
			guard error == nil else { print(error ?? "none");return}
			guard let signInResult = signInResult else { return }
			
			let user = signInResult.user
			//let emailAddress = user.profile?.email
			signInResult.user.refreshTokensIfNeeded { user, error in
				guard error == nil else { return }
				guard let user = user else { return }
				
				let idToken = user.idToken
				print(idToken)
				// Send ID token to backend (example below).
			}
		}
	}
}

#Preview {
	RegisterView().preferredColorScheme(.dark)
}
