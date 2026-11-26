//
//  BandAddView.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 25/11/2026.
//

import SharedLogic
import SwiftUI

struct BandAddView: View {
	@State private var name: String = ""
	@State private var formedYear: Int? = nil
	@State private var status: String = "unknown"
	@State private var disbandedYear: Int? = nil
	@State private var countryId: Int32? = nil
	@State private var description: String = ""
	@State private var imageUrl: String = ""
	@State private var resultText: String = ""

	var body: some View {
		NavigationStack {
			Group {
				Form {
					Section {
						Text(localize(key: "enter_band_data"))
							.font(.system(size: 24.0, weight: .bold))
					}

					TextField(localize(key: "band_name"), text: $name)

					TextField(localize(key: "formed_year"), value: $formedYear, format: .number.grouping(.never))
						.keyboardType(.numberPad)

					BandStatusPicker(status: $status)
						.onChange(of: status) { _, newValue in
							if newValue != "disbanded" {
								disbandedYear = nil
							}
						}

					if status == "disbanded" {
						TextField(localize(key: "disbanded_year"), value: $disbandedYear, format: .number.grouping(.never))
							.keyboardType(.numberPad)
					}

					CountryPicker(label: localize(key: "country"), countryId: $countryId)

					TextField(localize(key: "description"), text: $description, axis: .vertical)
						.lineLimit(4...)

					ImageField(
						placeholder: localize(key: "enter_photo_url"),
						description: localize(key: "band_photo_url"),
						imageUrl: $imageUrl
					)

					Button(localize(key: "add")) {
						submit()
					}
					.disabled(
						name.isEmpty ||
						formedYear == nil ||
						countryId == nil ||
						(status == "disbanded" && disbandedYear == nil)
					)

					if !resultText.isEmpty, Int(resultText) == nil {
						Text(localize(key: resultText))
					}
				}
			}
		}
	}

	func submit() {
		let token = KeychainService.retrieveToken() ?? ""

		Task {
			do {
				let band = BandAddDto(
					id: nil,
					name: name,
					formedYear: formedYear.map { KotlinInt(int: Int32($0)) },
					status: status.uppercased(),
					disbandedYear: disbandedYear.map { KotlinInt(int: Int32($0)) },
					country: countryId.map { KotlinInt(int: $0) },
					description: description,
					imageUrl: imageUrl
				)
				resultText = try await BandClient().addBand(
					band: band,
					token: token
				)
			} catch {
				resultText = "something_wrong"
			}
		}
	}
}

#Preview {
	BandAddView()
}
