//
//  CountryPicker.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 25/11/2026.
//

import SharedLogic
import SwiftUI

struct CountryPicker: View {
	let label: String
	@Binding var countryId: Int32?
	@State private var countries: [CountryDto] = []
	@State private var searchText: String = ""
	@FocusState private var focused: Bool

	var selectedCountryName: String? {
		countries.first(where: { $0.id == countryId }).map { localize(key: $0.name) }
	}

	var filteredCountries: [CountryDto] {
		searchText.isEmpty || searchText == selectedCountryName
			? countries : countries.filter { localize(key: $0.name).localizedCaseInsensitiveContains(searchText) }
	}

	var body: some View {
		VStack(alignment: .leading, spacing: 6) {
			Text(label)
			VStack(alignment: .leading) {
				TextField(localize(key: "search_country"), text: $searchText)
					.autocorrectionDisabled()
					.focused($focused)
					.padding(12)
				if focused {
					ScrollView {
						VStack(alignment: .leading) {
							ForEach(filteredCountries, id: \.id) { country in
								Divider()
								Button {
									countryId = country.id
									searchText = localize(key: country.name)
									focused = false
								} label: {
									Text(localize(key: country.name))
										.frame(maxWidth: .infinity, alignment: .leading)
										.padding(12)
										.contentShape(Rectangle())
								}
								.buttonStyle(.plain)
							}
							if filteredCountries.isEmpty {
								Divider()
								Text(localize(key: "no_results"))
									.padding(12)
							}
						}
					}
					.frame(maxHeight: 250)
				}
			}
			.background(Color.secondary.opacity(0.15))
			.cornerRadius(10)
		}
		.task {
			if let result = try? await ArtistClient().getCountries() {
				countries = (0..<result.size).map { result.get(index: $0)! }
			}
		}
	}
}
