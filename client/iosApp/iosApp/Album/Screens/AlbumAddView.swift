//
//  AlbumAddView.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 25/11/2026.
//

import SharedLogic
import SwiftUI

struct AlbumAddView: View {
	@State private var selectedBand: BandAlbumAddDto? = nil
	@State private var title: String = ""
	@State private var releaseDate: Foundation.Date? = nil
	@State private var albumType: String? = nil
	@State private var importance: Int = 0
	@State private var genreId: Int32? = nil
	@State private var description: String = ""
	@State private var imageUrl: String = ""
	@State private var resultText: String = ""
	
	var minReleaseDate: Foundation.Date {
		guard let year = selectedBand?.formedYear?.intValue else { return .distantPast }
		return Calendar.current.date(from: DateComponents(year: Int(year), month: 1, day: 1)) ?? .distantPast
	}
	
	var maxReleaseDate: Foundation.Date {
		Calendar.current.date(byAdding: .year, value: 1, to: Foundation.Date()) ?? .distantFuture
	}
	
	var isValid: Bool {
		selectedBand != nil && !title.isEmpty && releaseDate != nil && albumType != nil && genreId != nil
	}
	
	var body: some View {
		NavigationStack {
			VStack(spacing: 4) {
				Text(localize(key: "enter_album_data"))
					.font(.system(size: 28.0, weight: .bold))

				Form {
					BandPicker(selectedBand: $selectedBand)

					VStack(alignment: .leading, spacing: 6) {
						Text(localize(key: "album_title"))
						TextField(localize(key: "album_title"), text: $title)
							.padding(12)
							.background(Color.secondary.opacity(0.15))
							.cornerRadius(10)
					}

					CalendarDateField(
						label: localize(key: "release_date"),
						minDate: minReleaseDate,
						maxDate: maxReleaseDate,
						isDisabled: selectedBand == nil,
						fieldError: selectedBand == nil ? localize(key: "choose_band_first") : nil,
						date: $releaseDate
					)

					AlbumTypePicker(albumType: $albumType, importance: $importance)

					AlbumImportancePicker(albumType: albumType, importance: $importance)

					GenrePicker(genreId: $genreId)

					VStack(alignment: .leading, spacing: 6) {
						Text(localize(key: "description"))
						TextField(localize(key: "enter_album_description"), text: $description, axis: .vertical)
							.lineLimit(4...)
							.padding(12)
							.background(Color.secondary.opacity(0.15))
							.cornerRadius(10)
					}

					ImageField(
						placeholder: localize(key: "enter_image_url"),
						description: localize(key: "album_artwork_url_info"),
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
					let album = AlbumAddDto(
						id: nil,
						bandId: selectedBand.map { KotlinInt(int: $0.id) },
						title: title,
						releaseDate: releaseDate?.formatted(
							Foundation.Date.ISO8601FormatStyle(timeZone: Calendar.current.timeZone).year().month().day()
						),
						type: albumType?.uppercased(),
						description: description,
						mainSubgenre: genreId.map { KotlinInt(int: $0) },
						importance: KotlinInt(int: Int32(importance)),
						artworkUrl: imageUrl
					)
					resultText = try await AlbumClient().addAlbum(
						album: album,
						token: token
					)
				} catch {
					resultText = "something_wrong"
				}
			}
		}
	}


#Preview {
	AlbumAddView()
}
