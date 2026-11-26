//
//  GenrePicker.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 25/11/2026.
//

import SharedLogic
import SwiftUI

struct GenrePicker: View {
	@Binding var genreId: Int32?
	@State private var genres: [Genre] = []
	@State private var searchText: String = ""
	@FocusState private var focused: Bool

	var selectedGenreName: String? {
		genres.first(where: { $0.id == genreId })?.name
	}

	var filteredGenres: [Genre] {
		searchText.isEmpty || searchText == selectedGenreName
			? genres : genres.filter { $0.name.localizedCaseInsensitiveContains(searchText) }
	}

	var body: some View {
		VStack(alignment: .leading, spacing: 6) {
			Text(localize(key: "main_subgenre"))
			VStack(alignment: .leading) {
				TextField(localize(key: "search_genre_dots"), text: $searchText)
					.autocorrectionDisabled()
					.focused($focused)
					.padding(12)
				if focused {
					ScrollView {
						VStack(alignment: .leading) {
							ForEach(filteredGenres, id: \.id) { genre in
								Divider()
								Button {
									genreId = genre.id
									searchText = genre.name
									focused = false
								} label: {
									Text(genre.name)
										.frame(maxWidth: .infinity, alignment: .leading)
										.padding(12)
										.contentShape(Rectangle())
								}
								.buttonStyle(.plain)
							}
						}
					}
					.frame(maxHeight: 250)
				}
			}
			.background(Color.secondary.opacity(0.15))
			.cornerRadius(10)
			Text(localize(key: "select_main_subgenre"))
				.font(.system(size: 16.0))
				.foregroundStyle(.secondary)
		}
		.task {
			if let result = try? await GenreClient().getAll() {
				genres = (0..<result.size).map { result.get(index: $0)! }
			}
		}
	}
}
