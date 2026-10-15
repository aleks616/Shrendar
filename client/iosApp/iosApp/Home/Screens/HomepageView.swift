//
//  HomepageView.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 15/10/2026.
//

import SharedLogic
import SwiftUI

struct HomepageView: View {
	@State private var data: HomepageMainDto? = nil
	@State private var isLoading = true

	func loadData() async {
		do {
			let token = KeychainService.retrieveToken()
			data = try await HomepageClient().getTodayAnniversaries(
				token: token
			)
		} catch {
			data = nil
		}
		isLoading = false

	}

	var body: some View {
		let albumCount: Int =
			(data?.favoriteAlbums?.count ?? 0)
			+ (data?.recommendedAlbumsAnniversaries?.count ?? 0)
		let birthdayCount: Int =
			(data?.favoriteArtistsBirthdays?.count ?? 0)
			+ (data?.otherBandMembersBirthdays?.count ?? 0)
			+ (data?.recommendedArtistBirthdays?.count ?? 0)
		let deathAnniversaryCount: Int =
			(data?.favoriteArtistsDeathAnniversaries?.count ?? 0)
			+ (data?.otherBandMembersDeathAnniversaries?.count ?? 0)
			+ (data?.recommendedArtistDeathAnniversaries?.count ?? 0)
		let eventsCount: Int = data?.eventAnniversaries?.count ?? 0

		NavigationStack {
			ScrollView {
				VStack {
					if isLoading {
						ProgressView()
							.controlSize(.large)
							.frame(maxWidth: .infinity, maxHeight: .infinity)
					} else if let data {
						VStack {
							if albumCount > 0 {
								Text("\(localize(key:"album_anniversaries"))")
									.font(.system(size: 26.0))
									.bold()
								ForEach(
									Array((data.favoriteAlbums ?? []).enumerated()),
									id: \.offset
								) { _, album in
									AlbumCardView(album: album)
									Spacer()
								}

								ForEach(
									Array(
										(data.recommendedAlbumsAnniversaries ?? [])
											.enumerated()
									),
									id: \.offset
								) { _, album in
									AlbumCardView(album: album)
									Spacer()
								}
							}

							if birthdayCount > 0 {
								Spacer()
								Text("\(localize(key:"birthdays_today"))")
									.font(.system(size: 26.0))
									.bold()
								
								ForEach(
									Array((data.favoriteArtistsBirthdays ?? []).enumerated()),
									id: \.offset
								) { _, artist in
									ArtistBirthdayCardView(artist: artist)
									Spacer()
								}
								
								ForEach(
									Array((data.otherBandMembersBirthdays ?? []).enumerated()),
									id: \.offset
								) { _, artist in
									ArtistBirthdayCardView(artist: artist)
									Spacer()
								}
								
								ForEach(
									Array((data.recommendedArtistBirthdays ?? []).enumerated()),
									id: \.offset
								) { _, artist in
									ArtistBirthdayCardView(artist: artist)
									Spacer()
								}

							}
							
							if deathAnniversaryCount > 0 {
								Spacer()
								Text("\(localize(key:"died_today"))")
									.font(.system(size: 26.0))
									.bold()
								
								ForEach(
									Array((data.favoriteArtistsDeathAnniversaries ?? []).enumerated()),
									id: \.offset
								) { _, artist in
									ArtistDeathAnniversaryCardView(artist: artist)
									Spacer()
								}
								
								ForEach(
									Array((data.otherBandMembersDeathAnniversaries ?? []).enumerated()),
									id: \.offset
								) { _, artist in
									ArtistDeathAnniversaryCardView(artist: artist)
									Spacer()
								}
								
								ForEach(
									Array((data.recommendedArtistDeathAnniversaries ?? []).enumerated()),
									id: \.offset
								) { _, artist in
									ArtistDeathAnniversaryCardView(artist: artist)
									Spacer()
								}
								
							}
							
							if eventsCount > 0 {
								Spacer()
								Text("\(localize(key:"events_today"))")
									.font(.system(size: 26.0))
									.bold()
								ForEach(
									Array((data.eventAnniversaries ?? []).enumerated()),
									id: \.offset
								) { _, event in
									EventCardView(event: event)
									Spacer()
								}
							}

						}
					}
					UpcomingAnniversaries()
					CommonBands()

				}.padding(.all, 8)
			}.task {
				await loadData()
			}
		}

	}

}

#Preview {
	HomepageView()
}
