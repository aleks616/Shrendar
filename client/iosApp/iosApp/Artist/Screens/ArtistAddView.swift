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

	var isValid: Bool {
		!name.isEmpty && birthdate != nil && (!isDead || deathdate != nil)
	}

	var body: some View {
		NavigationStack {
			VStack(spacing: 4) {
				Text(localize(key: "enter_artist_data"))
					.font(.system(size: 28.0, weight: .bold))

				Form {
					VStack(alignment: .leading, spacing: 6) {
						Text(localize(key: "artist_name"))
						TextField(localize(key: "artist_name"), text: $name)
							.padding(12)
							.background(Color.secondary.opacity(0.15))
							.cornerRadius(10)
					}

					CalendarDateField(
						label: localize(key: "birthdate"),
						minDate: .distantPast,
						maxDate: maxBirthDate,
						isDisabled: false,
						fieldError: nil,
						date: $birthdate
					)

					Toggle(localize(key: "is_dead"), isOn: $isDead)

					if isDead {
						CalendarDateField(
							label: localize(key: "death_date"),
							minDate: minDeathDate,
							maxDate: Foundation.Date(),
							isDisabled: false,
							fieldError: nil,
							date: $deathdate
						)
					}

					GenderPicker(gender: $gender)

					CountryPicker(label: localize(key: "country"), countryId: $countryId)

					VStack(alignment: .leading, spacing: 6) {
						Text(localize(key: "description"))
						TextField(localize(key: "artist_description"), text: $description, axis: .vertical)
							.lineLimit(4...)
							.padding(12)
							.background(Color.secondary.opacity(0.15))
							.cornerRadius(10)
					}

					ImageField(
						placeholder: localize(key: "enter_photo_url"),
						description: localize(key: "artist_photo_url"),
						imageUrl: $imageUrl
					)
					Section {
						VStack {
							if !resultText.isEmpty {
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
							.glassEffect()
							.cornerRadius(25)
						}
						.frame(maxWidth: .infinity)
						.padding(.bottom, 8)
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
