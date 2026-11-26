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

	var body: some View {
		Picker(label, selection: $countryId) {
			Text(localize(key: "select_country"))
				.tag(nil as Int32?)
			ForEach(countries, id: \.id) { country in
				Text(localize(key: country.name))
					.tag(country.id as Int32?)
			}
		}
		.task {
			if let result = try? await ArtistClient().getCountries() {
				countries = (0..<result.size).map { result.get(index: $0)! }
			}
		}
	}
}
