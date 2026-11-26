//
//  ArtistPicker.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 25/11/2026.
//

import SharedLogic
import SwiftUI

struct ArtistPicker: View {
	@Binding var selectedArtist: ArtistMemberAddDto?
	@State private var searchText: String = ""
	@State private var results: [ArtistMemberAddDto] = []

	var body: some View {
		VStack(alignment: .leading, spacing: 6) {
			Text(localize(key: "artist_name"))
			VStack(alignment: .leading) {
				TextField(localize(key: "search_artist_dots"), text: $searchText)
					.autocorrectionDisabled()
					.padding(12)
					.onChange(of: searchText) { _, newValue in
						if newValue == selectedArtist?.name {
							return
						}
						if newValue.count >= 3 {
							Task {
								if let artists = try? await ArtistClient().getArtistDataForMemberAdd(name: newValue) {
									results = artists
								}
							}
						} else {
							results = []
						}
					}
				ForEach(results, id: \.id) { artist in
					Divider()
					Button {
						selectedArtist = artist
						searchText = artist.name ?? ""
						results = []
					} label: {
						HStack {
							Text(artist.country.map { "\(artist.name ?? "") (\(localize(key: $0)))" } ?? artist.name ?? "")
							Spacer()
						}
						.padding(12)
						.contentShape(Rectangle())
					}
					.buttonStyle(.plain)
				}
			}
			.background(Color.secondary.opacity(0.15))
			.cornerRadius(10)
			Text(localize(key: "enter_3_plus_characters"))
				.font(.system(size: 16.0))
				.foregroundStyle(.secondary)
		}
	}
}
