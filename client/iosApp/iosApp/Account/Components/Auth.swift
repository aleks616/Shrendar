//
//  Auth.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 04/10/2026.
//

import UIKit
import Swift
import SharedLogic
import AuthenticationServices
import GoogleSignIn
import GoogleSignInSwift


func handleGoogleSignInButton(authModel: AuthenticationModel) {
		guard let rootViewController = UIApplication.shared.rootViewController
		else {
			// Handle error
			return
		}

		GIDSignIn.sharedInstance.signIn(withPresenting: rootViewController) {
			signInResult,
			error in
			guard error == nil else {
				print(error ?? "none")
				return
			}
			guard let signInResult = signInResult else { return }
			//let emailAddress = user.profile?.email
			signInResult.user.refreshTokensIfNeeded { user, error in
				guard error == nil else { return }
				guard let user = user else { return }

				let idToken = user.idToken
				Task{
					do{
						let result = try await AccountClient().authWithGoogle(googleToken: idToken?.tokenString ?? "")
						let authToken =
						(try JSONSerialization.jsonObject(with: Data(result.utf8))
						 as! [String: Any])["token"] as! String
						KeychainService.saveToken(authToken)
						let userData = try await AccountApi().getUserData(token: authToken)
						UserDefaults.standard.set(userData.login, forKey: "login")
						authModel.isAuthenticated = true
					}
					catch let error{
						print(error)
						return
					}
				}
			}
		}
	}
