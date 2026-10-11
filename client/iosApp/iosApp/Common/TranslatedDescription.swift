//
//  TranslatedDescription.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 09/10/2026.
//

import SharedLogic
import SwiftUI

struct TranslatedDescription: View {
	let description: String

	@State private var displayedDescription: String

	init(description: String) {
		self.description = description
		_displayedDescription = State(initialValue: description)
	}

	var body: some View {
		ZStack(alignment: .bottomTrailing) {
			Text(displayedDescription)
				.frame(maxWidth: .infinity, alignment: .leading)
				.fixedSize(horizontal: false, vertical: true)
				.padding(.trailing, 44)
				.padding(16)

			Button(action: translate) {
				Image(systemName: "translate")
					.font(.title3)
			}
			.buttonStyle(.plain)
			.padding(8)
		}
		.overlay(
			RoundedRectangle(cornerRadius: 12)
				.stroke(Color.secondary, lineWidth: 1)
		)
		.padding(.top, 4)
	}

	private func translate() {
		let language = Locale.current.language.languageCode?.identifier ?? "en"

		Task { @MainActor in
			let request = TranslationRequestDto(
				text: description,
				targetLanguage: language
			)
			displayedDescription = try! await LlmClient().translate(
				request: request
			)
		}
	}
}
