//
//  IntroView.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 25/09/2026.
//

import SharedLogic
import SwiftUI

struct WelcomeView: View {
	@State private var showConfirmationPopup = false
	
	var body: some View {
		NavigationStack {
			VStack {
				LinearGradient(
					gradient: Gradient(colors: [
						Color.accentColor.opacity(0.8), Color.clear,
					]),
					startPoint: .top,
					endPoint: .bottom
				)
				.frame(height: 200)
				.edgesIgnoringSafeArea(.all)
				
				Text(localize(key: "welcome"))
					.font(.system(size: 28.0, weight: .bold))
				Text(localize(key: "sign_in_up"))
					.font(.system(size: 20.0, weight: .bold))
				Spacer()
				
				NavigationLink(destination: RegisterView()) {
					Text(localize(key: "create_account")).frame(maxWidth: .infinity)
				}
				.accessibilityIdentifier("welcome.createAccount")
				.padding()
				.background(Color.accentColor)
				.foregroundColor(.white)
				.glassEffect()
				.cornerRadius(25)
				.padding(.horizontal, 20)
				
				NavigationLink(destination: SignInView()) {
					Text(localize(key: "account_already")).frame(maxWidth: .infinity)
				}
				.accessibilityIdentifier("welcome.signIn")
				.padding()
				.background(Color.secondary)
				.foregroundColor(.white)
				.glassEffect()
				.cornerRadius(25)
				.padding(.horizontal, 20)
				
				HStack {
					Text(localize(key: "continue_as"))
					Button(localize(key: "guest_question")) {}
				}
				
			}
		}
	}
}

#Preview {
	WelcomeView()
}
