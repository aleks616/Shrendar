//
//  ArtistAddView.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 25/11/2026.
//

import SharedLogic
import SwiftUI

struct ArtistAddView: View {
	@State private var name: String = ""
	@State private var birthdate: Foundation.Date? = nil
	@State private var isDead: Bool = false
	@State private var deathdate: Foundation.Date? = nil
	@State private var gender: String = "M"
	@State private var countryId: Int32? = nil
	@State private var description: String = ""
	@State private var imageUrl: String = ""
	@State private var resultText: String = ""

	var maxBirthDate: Foundation.Date {
		Calendar.current.date(byAdding: .year, value: -10, to: Foundation.Date()) ?? Foundation.Date()
	}

	var minDeathDate: Foundation.Date {
		guard let birthdate else { return .distantPast }
		return Calendar.current.date(byAdding: .year, value: 10, to: birthdate) ?? .distantPast
	}

	var body: some View {
		NavigationStack {
			Group {
				Form {
					Section {
						Text(localize(key: "enter_artist_data"))
							.font(.system(size: 24.0, weight: .bold))
					}

					TextField(localize(key: "artist_name"), text: $name)

					DatePicker(
						localize(key: "birthdate"),
						selection: Binding(
							get: { birthdate ?? maxBirthDate },
							set: { birthdate = $0 }
						),
						in: .distantPast...maxBirthDate,
						displayedComponents: .date
					)

					Toggle(localize(key: "is_dead"), isOn: $isDead)

					if isDead {
						DatePicker(
							localize(key: "death_date"),
							selection: Binding(
								get: { deathdate ?? Foundation.Date() },
								set: { deathdate = $0 }
							),
							in: minDeathDate...Foundation.Date(),
							displayedComponents: .date
						)
					}

					GenderPicker(gender: $gender)

					CountryPicker(label: localize(key: "country"), countryId: $countryId)

					TextField(localize(key: "description"), text: $description, axis: .vertical)
						.lineLimit(4...)

					ImageField(
						placeholder: localize(key: "enter_photo_url"),
						description: localize(key: "artist_photo_url"),
						imageUrl: $imageUrl
					)

					Button(localize(key: "add")) {
						submit()
					}
					.disabled(name.isEmpty || birthdate == nil || (isDead && deathdate == nil))

					if !resultText.isEmpty {
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
				let artist = ArtistAddDto(
					id: nil,
					name: name,
					birthDate: birthdate?.formatted(
						Foundation.Date.ISO8601FormatStyle(timeZone: Calendar.current.timeZone).year().month().day()
					),
					deathDate: isDead ? deathdate?.formatted(
						Foundation.Date.ISO8601FormatStyle(timeZone: Calendar.current.timeZone).year().month().day()
					) : nil,
					gender: gender,
					country: countryId.map { KotlinInt(int: $0) },
					description: description,
					artistImageUrl: imageUrl
				)
				resultText = try await ArtistClient().addArtist(
					artist: artist,
					token: token
				)
			} catch {
				resultText = "something_wrong"
			}
		}
	}
}

#Preview {
	ArtistAddView()
}
