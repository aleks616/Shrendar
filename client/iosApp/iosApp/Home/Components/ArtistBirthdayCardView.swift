//
//  ArtistBirthdayCardView.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 15/10/2026.
//

import SwiftUI
import SharedLogic

struct ArtistBirthdayCardView: View {
	let artist: ArtistAnniversaryDto
	
	var body: some View {
		VStack(alignment: .leading, spacing: 12) {
			VStack(alignment: .leading, spacing: 4) {
				Text("\(artist.yearsSince.map { String(describing: $0) } ?? "unknown") 🎂")
					.font(.system(size: 20).bold())
				Text(
					"\(artist.name ?? "unknown")"
				).font(
					.system(size: 18).bold()
				)
			}
			VStack(alignment: .leading, spacing: 8) {
				Text("\(localize(key: "birthday")): \(artist.anniversaryDate.map { String(describing: $0) } ?? "unknown")")

				if let country = artist.country {
					Text("\(localize(key: "country")): \(localize(key: country))")
				}
				
				VStack(alignment: .center) {
					Group {
						if let imageUrl = artist.imageUrl,
							let url = URL(string: imageUrl) {
							AsyncImage(url: url) { image in
								image
									.resizable()
									.scaledToFill()
							} placeholder: {
								ProgressView()
							}
							.accessibilityLabel("artist photo")
						} else {
							Image(systemName: "photo")
								.resizable()
								.scaledToFill()
								.accessibilityLabel("no image")
						}
					}
					.frame(width: 288, height: 288)
					.clipped()
				}
				.frame(maxWidth: .infinity)
				
			}
			
		}
		.padding(.horizontal, 16)
		.padding(.vertical, 16)
		.background(Color.gray.opacity(0.1))
		.clipShape(RoundedRectangle(cornerRadius: 12))
		.shadow(radius: 2)
	}
}
