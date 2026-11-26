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
		VStack(alignment: .leading) {
			Text(localize(key: "artist_name"))
			if let selectedArtist {
				Text(selectedArtist.name ?? "")
			}
			TextField(localize(key: "search_artist_dots"), text: $searchText)
				.autocorrectionDisabled()
				.onChange(of: searchText) { _, newValue in
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
				Button(artist.name ?? "") {
					selectedArtist = artist
					searchText = ""
					results = []
				}
			}
			Text(localize(key: "enter_3_plus_characters"))
				.font(.system(size: 16.0))
				.foregroundStyle(.secondary)
		}
	}
}
