import SharedLogic
//
//  ContributionsTable.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 05/10/2026.
//
import SwiftUI

@ViewBuilder
func ContributionsTable(_ user: UserProfileDto) -> some View {
	let rows = user.contributions ?? []

	Table(
		columns: [
			(
				localize(key: "action"),
				{ $0.action }
			),
			(localize(key: "date"), { $0.changedAt }),
			(localize(key: "table"), { $0.changedTable }),
			(localize(key: "column"), { $0.changedColumn }),
			(
				localize(key: "confirmed"),
				{ $0.confirmed.map { $0 as! Bool ? "✓" : "✕" } }
			),
			(localize(key: "before"), { $0.oldValue }),
			(localize(key: "after"), { $0.newValue }),
		],
		data: rows,
		emptyText: localize(key: "no_contributions")
	)
}
