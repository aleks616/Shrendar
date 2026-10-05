//
//  ContributionsTable.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 05/10/2026.
//
import SwiftUI
import SharedLogic

@ViewBuilder
	func ContributionsTable(_ user: UserProfileDto) -> some View {
		let rows = user.contributions ?? []

		DataTable(emptyKey: "no_contributions", isEmpty: rows.isEmpty) {
			Grid(alignment: .leading, horizontalSpacing: 14, verticalSpacing: 0) {
				GridRow {
					Text(localize(key: "action"))
					Text(localize(key: "date"))
					Text(localize(key: "table"))
					Text(localize(key: "column"))
					Text(localize(key: "confirmed"))
					Text(localize(key: "before"))
					Text(localize(key: "after"))
				}

				ForEach(Array(rows.enumerated()), id: \.offset) { _, item in
					GridRow {
						Text(
							item.action.map {
								String(describing: $0)
									.split(separator: ".")
									.last
									.map(String.init)?
									.lowercased()
									.capitalized ?? "-"
							} ?? "-"
						)
						Text(item.changedAt ?? "-")
						Text(item.changedTable ?? "-")
						Text(item.changedColumn ?? "-")
						Text(item.confirmed == true ? "✓" : "✕")
						Text(item.oldValue ?? "-")
						Text(item.newValue ?? "-")
					}
					.padding(.vertical, 8)
				}
			}
		}
	}
