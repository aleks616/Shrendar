//
//  UpcomingAnniversaries.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 15/10/2026.
//

import SharedLogic
import SwiftUI

struct UpcomingAnniversaries: View {
	@State private var data: UpcomingAnniversariesDto? = nil
	@State private var isLoading = true

	func loadData() async {
		do {
			let token = KeychainService.retrieveToken()
			data = try await HomepageClient().getUpcoming(
				token: token
			)

		} catch {
			data = nil
		}
		isLoading = false

	}

	var body: some View {
		VStack {
			if !isLoading {
				if let data {
					VStack(alignment: .leading) {
						Text(localize(key: "upcoming_album_anniversaries")).font(
							.system(size: 26.0)
						).padding(.bottom, 12).bold()
						ForEach(
							Array((data.albums ?? []).enumerated()),
							id: \.offset
						) { _, album in
							Text(
								"\(album.bandName ?? "unknown") - \(album.title ?? "unknown")"
							).fontWeight(.bold)
							Text(
								"\(album.daysTillAnniversary.map { String(describing: $0) } ?? "unknown") \(localize(key: "time_dp")) \(localize(key: "to")) \(localize(key: "anniversary"))"
							)
							Text("\(album.age + 1) \(localize(key: "time_yp"))")
							Text(
								"\(album.releaseDate.map { String(describing: $0) } ?? "-")"
							)
							Spacer()
						}
						Spacer()

						Text(localize(key: "upcoming_birthdays")).font(
							.system(size: 26.0)
						).padding(.bottom, 12).bold()
						ForEach(
							Array((data.artistBirthdays ?? []).enumerated()),
							id: \.offset
						) { _, artist in
							Text("\(artist.name ?? "unknown")").fontWeight(.bold)
							Text(
								"\(artist.daysTillAnniversary.map { String(describing: $0) } ?? "unknown") \(localize(key: "time_dp")) \(localize(key: "to")) \(localize(key: "birthday"))"
							)
							Text(
								"\(artist.yearsSince as! Int + 1) \(localize(key: "time_yp"))"
							)
							Text(
								"\(artist.anniversaryDate.map { String(describing: $0) } ?? "-")"
							)
							Spacer()
						}
						Spacer()

						Text(localize(key: "upcoming_death_anniversaries")).font(
							.system(size: 26.0)
						).padding(.bottom, 12).bold()
						ForEach(
							Array((data.artistDeathAnns ?? []).enumerated()),
							id: \.offset
						) { _, artist in
							Text("\(artist.name ?? "unknown")").fontWeight(.bold)
							Text(
								"\(artist.daysTillAnniversary.map { String(describing: $0) } ?? "-") \(localize(key: "time_dp")) \(localize(key: "to")) \(localize(key: "death_anniversary"))"
							)
							Text(
								"\(artist.yearsSince as! Int + 1) \(localize(key: "time_yp"))"
							)
							Text(
								"\(artist.anniversaryDate.map { String(describing: $0) } ?? "-")"
							)
							Spacer()
						}
						Spacer()

					}
					.font(.system(size: 18.0))
				}
			}

		}.task {
			await loadData()
		}

	}
}

#Preview {
	UpcomingAnniversaries()
}
