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
	@State private var linkedArtistId: Int?

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
					HStack {
						Button(action: {
							selectedArtist = artist
							searchText = artist.name ?? ""
							results = []
						}) {
							HStack {
								Text(artist.country.map { "\(artist.name ?? "") (\(localize(key: $0)))" } ?? artist.name ?? "")
								Spacer()
							}
							.contentShape(Rectangle())
						}
						.buttonStyle(.plain)

						Button(action: {
							linkedArtistId = Int(artist.id)
						}) {
							Image(systemName: "arrow.up.right.square")
						}
						.buttonStyle(.glassProminent)
					}.padding(8)
				}
			}
			.background(Color.secondary.opacity(0.15))
			.cornerRadius(10)
			.background(
				NavigationLink(
					destination: ArtistDataView(artistId: linkedArtistId ?? 0),
					isActive: Binding(
						get: { linkedArtistId != nil },
						set: { if !$0 { linkedArtistId = nil } }
					)
				) {
					EmptyView()
				}
					.hidden()
			)
			Text(localize(key: "enter_3_plus_characters"))
				.font(.system(size: 16.0))
				.foregroundStyle(.secondary)
		}
	}
}
