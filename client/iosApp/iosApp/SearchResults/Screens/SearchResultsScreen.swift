//
//  SearchResultsScreen.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 22/10/2026.
//

import SharedLogic
import SwiftUI
import WrappingHStack

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
				filter = "all"
			} catch {
				data = nil
			}
			isLoading = false
		}
	}

	@ViewBuilder
	func getDestination(url: String, name: String) -> some View {
		let itemType = url.split(separator: "/")[0]
		let itemId = url.split(separator: "/")[1]

		if itemType == "album" {
			AlbumDataView(albumId: Int(itemId)!)
		} else if itemType == "artist" {
			ArtistDataView(artistId: Int(itemId)!)
		} else if itemType == "band" {
			BandDataView(bandId: Int(itemId)!)
		} else if itemType == "event" {
			EventDataView(eventId: Int(itemId)!)
		} else if itemType == "u" {
			UserProfileView(login: String(itemId))
		} else {
			EmptyView()
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

						WrappingHStack(alignment: .leading) {
							ForEach(filterOptions, id: \.self) { option in
								Button(localize(key: "filter_\(option)")) {
									if option != "all" {
										showingItems = SearchResults(
											items: data.items?.filter { $0.type == option }
										)
									} else {
										showingItems = data
									}
									filter = option
								}.filterButtonStyle(filter == option).foregroundStyle(.primary)
							}
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
												SquareImageView(urlParam: item.imageUrl)
											}.frame(maxWidth: 54).padding(8)
											VStack(alignment: .leading, spacing: 4) {
												NavigationLink(
													destination: getDestination(
														url: item.link ?? "",
														name: item.name
													)
												) {
													Text(item.name).font(
														.system(size: 22.0)
													)
												}
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
					} else {
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
