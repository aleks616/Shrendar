//
//  ArtistDataView.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 10/10/2026.
//

import SharedLogic
import SwiftUI

struct ArtistDataView: View {
	let artistId: Int

	@State private var artist: ArtistWikiDto?
	@State private var isLoading = true
	@State private var isFavorite = false

	let isLoggedIn: Bool = {
		let token = KeychainService.retrieveToken()
		return token != nil && !token!.isEmpty
	}()

	func toggleArtistFavorite() {
		Task { @MainActor in
			do {
				let result = try await ProfileClient().toggleFavoriteArtist(
					artistId: Int64(truncating: artistId as NSNumber),
					token: KeychainService.retrieveToken()
				)
				guard result == "artist_toggle" else {
					print("Failed to toggle artists favorite status")
					return
				}
				isFavorite.toggle()
			} catch {
				print(error.localizedDescription)
			}
		}
	}

	var body: some View {
		NavigationStack {
			Group {
				if isLoading {
					ProgressView()
						.controlSize(.large)
						.frame(maxWidth: .infinity, maxHeight: .infinity)
				} else if let artist {
					let bands = artist.bands ?? []
					let isDead = artist.deathDate != nil
					let bandColumns: [Table<ArtistsBandsHistoryDto>.Column] = [
						(
							localize(key: "bands"),
							{
								(bands: ArtistsBandsHistoryDto) -> String? in
								bands.bandName
							}
						),
						(
							localize(key: "role"),
							{
								(bands: ArtistsBandsHistoryDto) -> String? in
								bands.yearRole?
									.map { (yearRole: Any) in
										String(describing: yearRole).replacingOccurrences(
											of: "guitar",
											with: localize(key: "guitar")
										)
										.replacingOccurrences(
											of: "bass",
											with: localize(key: "bass")
										)
										.replacingOccurrences(
											of: "drums",
											with: localize(key: "drums")
										)
										.replacingOccurrences(
											of: "backing vocals",
											with: localize(key: "backing_vocals")
										)
										.replacingOccurrences(
											of: "vocals",
											with: localize(key: "vocals")
										)
									}
									.joined(separator: "\n")
							}
						),
					]

					ScrollView {
						VStack(alignment: .leading, spacing: 16) {
							HStack {
								Text(artist.name ?? "")
									.font(.largeTitle.bold())

								Button(action: toggleArtistFavorite) {
									Image(systemName: isFavorite ? "star.fill" : "star")
										.foregroundStyle(
											isFavorite ? Color.accentColor : .secondary
										)
										.frame(width: 40, height: 40)
								}
								.disabled(!isLoggedIn)
								.buttonStyle(.plain)
								.accessibilityLabel(localize(key: "favorite"))
							}

							AnyView(
								HStack(alignment: .top, spacing: 20) {
									VStack(alignment: .leading, spacing: 8) {
										HStack(alignment: .firstTextBaseline, spacing: 4)
										{
											Text("\(localize(key: "country")):")
												.foregroundStyle(.secondary)
											Text(localize(key: artist.country ?? "-"))
										}
										HStack(alignment: .firstTextBaseline, spacing: 4)
										{
											Text("\(localize(key: "age")):")
												.foregroundStyle(.secondary)
											Text("\(artist.age)")
										}

										HStack(alignment: .firstTextBaseline, spacing: 4)
										{
											Text("\(localize(key: "birthday")):")
												.foregroundStyle(.secondary)
											Text(artist.birthDate.map { String(describing: $0) } ?? "-")
											Text(" \(localize(key: "next_in"))")
												.foregroundStyle(.secondary)
											Text(
												artist.daysTillBirthday.map { String(describing: $0) } ?? "-"
											)
											Text(
												"\(localize(key:"days"))"
											)
										}
										if isDead {
											HStack(
												alignment: .firstTextBaseline,
												spacing: 4
											) {
												Text(
													"\(localize(key: "death_anniversary")):"
												)
												.foregroundStyle(.secondary)
												Text(artist.deathDate.map { String(describing: $0) } ?? "-")
												Text(" \(localize(key: "next_in"))")
													.foregroundStyle(.secondary)
												Text(
													artist.daysTillDeathAnniversary.map { String(describing: $0) }
														?? "-"
												)
												Text(
													" \(localize(key:"days"))"
												)
											}
										}

										HStack(alignment: .firstTextBaseline, spacing: 4)
										{
											Text("\(localize(key: "gender")):")
												.foregroundStyle(.secondary)
											Text(localize(key: artist.gender ?? "-"))
										}

										HStack(alignment: .firstTextBaseline, spacing: 4)
										{
											Text("\(localize(key: "zodiac_sign")):")
												.foregroundStyle(.secondary)
											Text(
												localize(
													key: "zodiac_\(artist.zodiacSign.map { String(describing: $0) } ?? "-")"
												)
											)
										}

										HStack(alignment: .firstTextBaseline, spacing: 4)
										{
											Text(
												"\(localize(key: "chinese_zodiac_sign")):"
											)
											.foregroundStyle(.secondary)
											Text(
												localize(
													key:
														"chinese_zodiac_\(artist.chineseZodiacSign.map { String(describing: $0) } ?? "-")"
												)
											)
										}
									}
									.frame(maxWidth: .infinity, alignment: .leading)
								}
							)
							if let artistImageUrl = artist.artistImageUrl,
								let url = URL(string: artistImageUrl)
							{
								AsyncImage(url: url) { phase in
									switch phase {
									case .success(let image):
										image
											.resizable()
											.scaledToFit()
									case .failure:
										Image(systemName: "photo")
											.resizable()
											.scaledToFit()
											.foregroundStyle(.secondary)
											.padding(40)
									default:
										ProgressView()
											.frame(width: 180, height: 180)
									}
								}
								.frame(maxWidth: 320, maxHeight: 320)
								.frame(maxWidth: .infinity)
								.clipShape(RoundedRectangle(cornerRadius: 12))
								.accessibilityLabel(artist.name ?? "")
							}

							TranslatedDescription(
								description: artist.description_ ?? ""
							)

							Text(localize(key: "bands"))
								.font(.title3)

							Table(
								columns: bandColumns,
								data: bands,
								emptyText: localize(key: "no_bands")
							)
							.frame(minWidth: UIScreen.main.bounds.width - 24)
							.frame(maxWidth: .infinity)

						}
						.padding(.horizontal, 12)
						.padding(.vertical, 16)
					}
				}
			}
			.task(id: artistId) {
				await loadArtist()
			}
		}
	}

	private func loadArtist() async {
		isLoading = true

		do {
			let token = KeychainService.retrieveToken()
			artist = try await ArtistClient().getArtistWikiPageDataById(
				id: Int64(artistId),
				token: token
			)
			isFavorite = artist?.favorite == true
		} catch {
			artist = nil
		}
		isLoading = false
	}

}
