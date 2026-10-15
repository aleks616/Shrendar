//
//  AlbumCardView.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 15/10/2026.
//
import SharedLogic
import SwiftUI

struct AlbumCardView: View {
	let album: AlbumByDateDto
	var body: some View {
		VStack(alignment: .leading, spacing: 12) {
			VStack(alignment: .leading, spacing: 4) {
				Text("\(album.yearsSince.map { String(describing: $0) } ?? "unknown") 🎂")
					.font(.system(size: 20).bold())
				Text(
					"\(album.band?.name ?? "unknown") - \(album.title ?? "unknown")"
				).font(
					.system(size: 18).bold()
				)
			}
			VStack(alignment: .leading, spacing: 8) {
				Text("\(localize(key: "release_date")): \(album.releaseDate.map { String(describing: $0) } ?? "unknown")")
				Text("\(localize(key: "album_type")): \(album.type.map { String(describing: $0) } ?? "unknown")")
				
				if let genre = album.genre {
					Text("\(localize(key: "main_genre")): \(genre.name)")
				}
				
				VStack(alignment: .center) {
					Group {
						if let artworkUrl = album.artworkUrl,
							let url = URL(string: artworkUrl) {
							AsyncImage(url: url) { image in
								image
									.resizable()
									.scaledToFill()
							} placeholder: {
								ProgressView()
							}
							.accessibilityLabel("album artwork")
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
