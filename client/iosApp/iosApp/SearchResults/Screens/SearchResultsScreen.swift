//
//  SearchResultsScreen.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 22/10/2026.
//

import SharedLogic
import SwiftUI

extension View {
	@ViewBuilder
	func filterButtonStyle(_ isSelected: Bool) -> some View {
		if isSelected {
			buttonStyle(.glassProminent)
		} else {
			buttonStyle(.glass)
		}
	}
}

struct SearchResultsScreen: View {
	@State private var data: SearchResults?
	@State private var isLoading = false
	let filterOptions = ["all", "band", "album", "artist", "user", "event"]
	@State private var filter = "all"
	@State private var showingItems: SearchResults?
	@State private var searchQuery = ""

	func loadData() {
		Task {
			isLoading = true
			do {
				data = try await SearchClient().search(query: searchQuery)
				showingItems = data
			} catch {
				data = nil
			}
			isLoading = false
		}
	}

	var body: some View {
		NavigationStack {
			VStack {
				HStack {
					Image(systemName: "magnifyingglass")
					TextField(localize(key: "searchbar"), text: $searchQuery)
						.onSubmit {
							loadData()
						}
				}
				.padding()
				.background {
					RoundedRectangle(cornerRadius: 10)
						.foregroundStyle(.ultraThinMaterial)
				}
				Group {
					if isLoading {
						ProgressView()
							.controlSize(.large)
							.frame(maxWidth: .infinity, maxHeight: .infinity)
					} else if let data {
						Text(localize(key: "filter"))
						HStack {
							Button(localize(key: "filter_all")) {
								showingItems = data
								filter = "all"
							}.filterButtonStyle(filter == "all")
								.frame(maxWidth: .infinity)
							Button(localize(key: "filter_band")) {
								showingItems = SearchResults(
									items: data.items?.filter { $0.type == "band" }
								)
								filter = "band"
							}.filterButtonStyle(filter == "band")
								.frame(maxWidth: .infinity)
							Button(localize(key: "filter_album")) {
								showingItems = SearchResults(
									items: data.items?.filter { $0.type == "album" }
								)
								filter = "album"
							}.filterButtonStyle(filter == "album")
								.frame(maxWidth: .infinity)
						}.frame(maxWidth: .infinity)

						HStack {
							Button(localize(key: "filter_artist")) {
								showingItems = SearchResults(
									items: data.items?.filter { $0.type == "artist" }
								)
								filter = "artist"
							}.filterButtonStyle(filter == "artist")
								.frame(maxWidth: .infinity)
							Button(localize(key: "filter_user")) {
								showingItems = SearchResults(
									items: data.items?.filter { $0.type == "user" }
								)
								filter = "user"
							}.filterButtonStyle(filter == "user")
								.frame(maxWidth: .infinity)
							Button(localize(key: "filter_event")) {
								showingItems = SearchResults(
									items: data.items?.filter { $0.type == "event" }
								)
								filter = "event"
							}.filterButtonStyle(filter == "event")
								.frame(maxWidth: .infinity)
						}.frame(maxWidth: .infinity)

						ScrollView {
							VStack(alignment: .leading, spacing: 16) {
								if showingItems?.items?.count == 0 {
									Text(localize(key: "no_results"))
								}
								ForEach(
									Array((showingItems?.items ?? []).enumerated()),
									id: \.offset
								) { _, item in
									HStack {
										HStack(spacing: 12) {
											VStack {
												if let imageURL = item.imageUrl,
													let url = URL(string: imageURL)
												{
													AsyncImage(url: url) { image in
														image
															.resizable()
															.aspectRatio(1, contentMode: .fill)
															.clipShape(Circle())

													} placeholder: {
														ProgressView()
													}
													.frame(maxWidth: 48)
													.accessibilityLabel("photo")
												} else {
													Image(systemName: "photo")
														.resizable()
														.aspectRatio(1, contentMode: .fill)
														.clipShape(Circle())
														.accessibilityLabel("no image")
														.frame(maxWidth: 48)
												}
											}.frame(maxWidth: 54).padding(8)
											VStack(alignment: .leading, spacing: 4) {
												Text(item.name).font(.system(size: 22.0))
												Text(item.info ?? "-").font(
													.system(size: 16.0)
												)
												Text("\(item.description_)").font(
													.system(size: 12.0)
												).foregroundStyle(.secondary)
											}
										}
										.frame(maxWidth: .infinity, alignment: .leading)
										.padding(12)
										.background(.backgroundVariant)
										.cornerRadius(8)
										.shadow(radius: 2)

									}
								}
							}
						}
					}
					else {
						Spacer()
					}
				}
			}.padding(10)
		}
	}
}

#Preview {
	SearchResultsScreen()
}
