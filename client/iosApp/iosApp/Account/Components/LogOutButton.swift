//
//  LogOutButton.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 25/09/2026.
//

import SwiftUI
import SharedLogic

struct LogOutButton: View {
  var body: some View {
    Button(action: { logout() }) {
      Text("log out")
    }
  }

  func logout() {
    Task {
      do {
			if let token = KeychainService.retrieveToken() {
				let result=try await AccountClient().logout(token)
				if result=="logged_out"{
					KeychainService.removeToken()
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
