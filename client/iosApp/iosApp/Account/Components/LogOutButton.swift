//
//  LogOutButton.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 25/09/2026.
//

import SwiftUI
import SharedLogic

struct LogOutButton: View {
	@Environment(AuthenticationModel.self) private var authModel
   var body: some View {
    Button(action: { logout() }) {
		 Text(localize(key:"logout")).frame(maxWidth: .infinity)
    }
	 .accessibilityIdentifier("settings.logout")
	 .padding()
	 .background(Color.red)
	 .foregroundColor(.white)
	 .glassEffect()
	 .cornerRadius(25)
	 .padding(.horizontal, 20)
  }

  func logout() {
    Task {
      do {
			if let token = KeychainService.retrieveToken() {
				let result=try await AccountClient().logout(token: token)
				if result=="logged_out"{
					KeychainService.removeToken()
					UserDefaults.standard.removeObject(forKey: "login")
					authModel.isAuthenticated = false
					return
				}
				print(result)
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
  LogOutButton()
}
