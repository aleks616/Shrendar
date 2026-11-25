//
//  ImageField.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 25/11/2026.
//

import SwiftUI

struct ImageField: View {
	let placeholder: String
	let description: String
	@Binding var imageUrl: String

	var body: some View {
		VStack(alignment: .leading, spacing: 6) {
			Text(localize(key: "image_url"))
			TextField(placeholder, text: $imageUrl)
				.keyboardType(.URL)
				.autocorrectionDisabled()
				.textInputAutocapitalization(.never)
				.padding(12)
				.background(Color.secondary.opacity(0.15))
				.cornerRadius(10)
				.onChange(of: imageUrl) { _, newValue in
					imageUrl = String(newValue.prefix(255))
				}
			Text(description)
				.font(.system(size: 16.0))
				.foregroundStyle(.secondary)
			if let url = URL(string: imageUrl), !imageUrl.isEmpty {
				AsyncImage(url: url) { phase in
					if let image = phase.image {
						image
							.resizable()
							.scaledToFit()
							.frame(maxHeight: 200)
					} else if phase.error != nil {
						Text(localize(key: "invalid_image_url"))
							.foregroundStyle(.red)
					} else {
						ProgressView()
					}
				}
			} else {
				Rectangle()
					.fill(Color.secondary.opacity(0.15))
					.overlay(Rectangle().stroke(Color.secondary, lineWidth: 2))
					.frame(width: 200, height: 200)
			}
		}
	}
}
