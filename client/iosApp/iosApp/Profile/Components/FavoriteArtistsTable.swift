//
//  FavoriteArtistsTable.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 05/10/2026.
//
import SharedLogic
import SwiftUI

@ViewBuilder
func FavoriteArtistsTable(
	favoriteArtists: [FavoriteArtistDto]?,
	isOwnProfile: Bool,
	favoriteIds: Set<String>,
	onToggle: @escaping (FavoriteArtistDto) -> Void
) -> some View {
	DataTable(
		emptyKey: "no_favorite_artists",
		isEmpty: favoriteArtists?.isEmpty ?? true
	) {
		Grid(alignment: .leading, horizontalSpacing: 14, verticalSpacing: 0) {
			GridRow {
				if isOwnProfile {
					Text(localize(key: "toggle"))
				}
				Text(localize(key: "artist_name"))
				Text(localize(key: "bands"))
			}

			ForEach(Array((favoriteArtists ?? []).enumerated()), id: \.offset) {
				_,
				item in
				let currentBands =
					item.bands?.filter { $0.current == true }
					.compactMap { $0.bandName } ?? []
				let pastBands =
					item.bands?.filter { $0.current == false }
					.compactMap { $0.bandName } ?? []

				GridRow {
					if isOwnProfile {
						ToggleFavoriteButton(
							isSelected: favoriteIds.contains(
								item.id.map { String(describing: $0) } ?? ""
							),
							action: { onToggle(item) }
						)
						.frame(maxWidth: .infinity, alignment: .center)
					}
					Text(item.name ?? "-")
					VStack(alignment: .leading, spacing: 2) {
						if !currentBands.isEmpty {
							Text(currentBands.joined(separator: ", "))
						}
						if !pastBands.isEmpty {
							Text(
								"\(localize(key: "past")): \(pastBands.joined(separator: ", "))"
							)
							.foregroundStyle(.secondary)
						}
						if currentBands.isEmpty && pastBands.isEmpty {
							Text("-")
						}
					}
				}
				.padding(.vertical, 8)
			}
		}
	}
}
