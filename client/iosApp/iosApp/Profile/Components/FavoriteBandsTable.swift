//
//  FavoriteBandsTable.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 05/10/2026.
//
import SharedLogic
import SwiftUI

@ViewBuilder
func FavoriteBandsTable(
	favoriteBands: [FavoriteBandDto]?,
	isOwnProfile: Bool,
	favoriteIds: Set<String>,
	onToggle: @escaping (FavoriteBandDto) -> Void
) -> some View {
	DataTable(
		emptyKey: "no_favorite_bands",
		isEmpty: favoriteBands?.isEmpty ?? true
	) {
		Grid(alignment: .leading, horizontalSpacing: 14, verticalSpacing: 0) {
			GridRow {
				if isOwnProfile {
					Text(localize(key: "toggle"))
				}
				Text(localize(key: "band_name"))
				Text(localize(key: "country"))
				Text(localize(key: "active"))
			}

			ForEach(Array((favoriteBands ?? []).enumerated()), id: \.offset) {
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
					Text(item.country.map { localize(key: $0) } ?? "-")
					Text(item.activeYears ?? "-")
				}
				.padding(.vertical, 8)
			}
		}
	}
}
