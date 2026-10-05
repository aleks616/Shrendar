//
//  DataTable.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 05/10/2026.
//
import SwiftUI

struct DataTable<Content: View>: View {
	let emptyKey: String
	let isEmpty: Bool
	let content: () -> Content
	@State var contentHeight: CGFloat = 100

	init(
		emptyKey: String,
		isEmpty: Bool,
		@ViewBuilder content: @escaping () -> Content
	) {
		self.emptyKey = emptyKey
		self.isEmpty = isEmpty
		self.content = content
	}

	var body: some View {
		Group {
			if isEmpty {
				VStack(spacing: 8) {
					Image(systemName: "tray")
						.foregroundStyle(.secondary)
					Text(localize(key: emptyKey))
						.font(.body)
						.foregroundStyle(.secondary)
				}
				.frame(maxWidth: .infinity, minHeight: 100)
			}
			else {
				ScrollView(.vertical, showsIndicators: true) {
					ScrollView(.horizontal, showsIndicators: true) {
						content()
							.background {
								GeometryReader { proxy in
									Color.clear.preference(
										key: DataTableHeightPreferenceKey.self,
										value: proxy.size.height
									)
								}
							}
					}
				}
				.frame(height: min(max(contentHeight, 1), 480))
			}
		}
		.background(Color.secondary.opacity(0.08))
		.clipShape(RoundedRectangle(cornerRadius: 12))
		.onPreferenceChange(DataTableHeightPreferenceKey.self) {
			contentHeight = $0
		}
	}
}
