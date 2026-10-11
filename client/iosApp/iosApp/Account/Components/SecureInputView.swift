//
//  SecureInputView.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 25/09/2026.
//


// Source - https://stackoverflow.com/a/66402146
// Posted by Vahagn Gevorgyan, modified by community. See post 'Timeline' for change history
// Retrieved 2026-09-25, License - CC BY-SA 4.0

// Changes: Added accessibility labels and identifiers for the fields and visibility toggle.
import SwiftUI

struct SecureInputView: View {
	
	@Binding private var text: String
	@State private var isSecured: Bool = true
	private var title: String
	private var accessibilityIdentifier: String
	
	init(_ title: String, text: Binding<String>, accessibilityIdentifier: String) {
		self.title = title
		self._text = text
		self.accessibilityIdentifier = accessibilityIdentifier
	}
	
	var body: some View {
		ZStack(alignment: .trailing) {
			Group {
				if isSecured {
					SecureField(title, text: $text)
						.accessibilityLabel(title)
						.accessibilityIdentifier(accessibilityIdentifier)
				} else {
					TextField(title, text: $text)
						.accessibilityLabel(title)
						.accessibilityIdentifier(accessibilityIdentifier)
				}
			}.padding(.trailing, 32)
			
			Button(action: {
				isSecured.toggle()
			}) {
				Image(systemName: self.isSecured ? "eye.slash" : "eye")
					.accentColor(.gray)
			}
			.accessibilityLabel(isSecured ? "Show password" : "Hide password")
			.accessibilityIdentifier("\(accessibilityIdentifier).visibilityToggle")
		}
	}
}
