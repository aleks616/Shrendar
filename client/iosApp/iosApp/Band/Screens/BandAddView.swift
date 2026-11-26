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
	@State private var linkedBandId: Int? = nil

	var formedYearPlaceholder: String {
		"1920"
	}

	var disbandedYearPlaceholder: String {
		guard let formedYear else { return "" }
		return "\(formedYear)"
	}

	var isValid: Bool {
		!name.isEmpty && formedYear != nil && countryId != nil && (status != "disbanded" || disbandedYear != nil)
	}

	var body: some View {
		NavigationStack {
			VStack(spacing: 4) {
				Text(localize(key: "enter_band_data"))
					.font(.system(size: 28.0, weight: .bold))

				Form {
					VStack(alignment: .leading, spacing: 6) {
						Text(localize(key: "band_name"))
						TextField(localize(key: "band_name"), text: $name)
							.padding(12)
							.background(Color.secondary.opacity(0.15))
							.cornerRadius(10)
					}

					VStack(alignment: .leading, spacing: 6) {
						Text(localize(key: "formed_year"))
						TextField(formedYearPlaceholder, value: $formedYear, format: .number.grouping(.never))
							.keyboardType(.numberPad)
							.padding(12)
							.background(Color.secondary.opacity(0.15))
							.cornerRadius(10)
					}

					BandStatusPicker(status: $status)
						.onChange(of: status) { _, newValue in
							if newValue != "disbanded" {
								disbandedYear = nil
							}
						}

					if status == "disbanded" {
						VStack(alignment: .leading, spacing: 6) {
							Text(localize(key: "disbanded_year"))
							TextField(disbandedYearPlaceholder, value: $disbandedYear, format: .number.grouping(.never))
								.keyboardType(.numberPad)
								.padding(12)
								.background(Color.secondary.opacity(0.15))
								.cornerRadius(10)
						}
					}

					CountryPicker(label: localize(key: "country"), countryId: $countryId)

					VStack(alignment: .leading, spacing: 6) {
						Text(localize(key: "description"))
						TextField(localize(key: "band_description"), text: $description, axis: .vertical)
							.lineLimit(4...)
							.padding(12)
							.background(Color.secondary.opacity(0.15))
							.cornerRadius(10)
					}

					ImageField(
						placeholder: localize(key: "enter_photo_url"),
						description: localize(key: "band_photo_url"),
						imageUrl: $imageUrl
					)
					Section {
						VStack(spacing: 8) {
							if !resultText.isEmpty, Int(resultText) == nil {
								Text(localize(key: resultText))
							}

							Button(action: submit) {
								Text(localize(key: "add")).frame(width: 140)
							}
							.buttonStyle(.plain)
							.disabled(!isValid)
							.padding()
							.background(Color.accentColor)
							.foregroundColor(.white)
							.cornerRadius(25)

							if let bandId = Int(resultText) {
								Button {
									linkedBandId = bandId
								} label: {
									Text(localize(key: "add_band_members")).frame(width: 140)
								}
								.buttonStyle(.plain)
								.padding()
								.background(Color.accentColor)
								.foregroundColor(.white)
								.cornerRadius(25)
							}
						}
						.frame(maxWidth: .infinity)
						.padding(.bottom, 8)
						.navigationDestination(item: $linkedBandId) { bandId in
							BandMembersAddView(bandId: bandId)
						}
					}
					.listRowBackground(Color.clear)
				}
				.onTapGesture {
					UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
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
