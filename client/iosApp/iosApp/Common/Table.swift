//
//  Table.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 08/10/2026.
//

import Foundation
import SwiftUI

struct Table<Row>: View {
	typealias Column = (title: String, value: (Row) -> String?)

	let columns: [Column]
	let data: [Row]
	let emptyText: String

	init(
		columns: [Column],
		data: [Row],
		emptyText: String
	) {
		self.columns = columns
		self.data = data
		self.emptyText = emptyText
	}

	var body: some View {
		let columnCount = max(columns.count, 1)

		ScrollView(.horizontal, showsIndicators: true) {
			ScrollView(.vertical, showsIndicators: true) {
				Grid(alignment: .leading, horizontalSpacing: 0, verticalSpacing: 0)
				{
					GridRow {
						ForEach(columns.indices, id: \.self) { index in
							Text(columns[index].title)
								.font(.subheadline.weight(.semibold))
								.foregroundStyle(.white)
								.lineLimit(1)
								.fixedSize(horizontal: true, vertical: false)
								.padding(.horizontal, 8)
								.padding(.vertical, 8)
								.frame(minHeight: 36, alignment: .leading)
								.overlay(alignment: .leading) {
									if index > columns.startIndex {
										Rectangle()
											.fill(Color.white.opacity(0.35))
											.frame(width: 1)
											.padding(.vertical, 8)
									}
								}
						}
					}

					if data.isEmpty {
						GridRow {
							VStack(spacing: 8) {
								Image(systemName: "tray")
									.foregroundStyle(.secondary)
								Text(emptyText)
									.font(.body)
									.foregroundStyle(.secondary)
							}
							.frame(minWidth: 100, minHeight: 100)
							.frame(maxWidth: .infinity)
							.padding(.vertical, 24)
							.gridCellColumns(columnCount)
						}
					} else {
						ForEach(Array(data.enumerated()), id: \.offset) { _, item in
							GridRow {
								ForEach(columns.indices, id: \.self) { index in
									Text(columns[index].value(item) ?? "-")
										.lineLimit(nil)
										.fixedSize(horizontal: true, vertical: false)
										.padding(.horizontal, 8)
										.padding(.vertical, 10)
								}
							}
							GridRow {
								Divider()
									.gridCellColumns(columnCount)
									.frame(maxWidth: .infinity)
							}
						}
					}
				}
				.frame(
					minWidth: UIScreen.main.bounds.width - 24,
					maxWidth: .infinity,
					alignment: .leading
				)
				.background(alignment: .top) {
					RoundedRectangle(cornerRadius: 16)
						.fill(Color("BackgroundVariantColor"))
						.frame(height: 36)
				}
			}
			.frame(maxHeight: 500)
		}
		.clipShape(RoundedRectangle(cornerRadius: 12))
	}
}
