//
//  FavoriteGenresTable.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 05/10/2026.
//
import SharedLogic
import SwiftUI

@ViewBuilder
func FavoriteGenresTable(
	favoriteGenres: [FavoriteGenreDto]?,
	isOwnProfile: Bool,
	favoriteIds: Set<String>,
	onToggle: @escaping (FavoriteGenreDto) -> Void
) -> some View {
	DataTable(
		emptyKey: "no_favorite_genres",
		isEmpty: favoriteGenres?.isEmpty ?? true
	) {
		Grid(alignment: .leading, horizontalSpacing: 14, verticalSpacing: 0) {
			GridRow {
				if isOwnProfile {
					Text(localize(key: "toggle"))
				}
				Text(localize(key: "genre"))
			}

			ForEach(Array((favoriteGenres ?? []).enumerated()), id: \.offset) {
				_,
				item in
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
				}
				.padding(.vertical, 8)
			}
		}
	}
}
