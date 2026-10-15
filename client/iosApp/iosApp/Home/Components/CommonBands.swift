//
//  CommonBands.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 14/10/2026.
//

import SharedLogic
import SwiftUI

struct CommonBands: View {
	@State private var bands: CommonBandsDto? = nil
	@State private var isLoading = true

	func loadData() async {

		do {
			let token = KeychainService.retrieveToken()
			if token != nil {
				bands = try await HomepageClient().getCommonBands(
					token: token!
				)
			}
		} catch {
			bands = nil
		}
		isLoading = false

	}

	var body: some View {
		VStack {
			if (!isLoading){
				if let bands {
					VStack(alignment: .leading) {
						Text(localize(key: "bands_to_check_out")).font(
							.system(size: 26.0).bold()
						).padding(.bottom, 12).frame(maxWidth:.infinity,alignment: .leading)
						ForEach(
							Array((bands.bands ?? []).enumerated()),
							id: \.offset
						) { _, band in
							Text("\(band.bandName ?? "unknown")").fontWeight(.bold)
							Text(localize(key: "member_in_favorites"))
							Text("\(band.nickname ?? band.artistName ?? "unknown")")
							Spacer()
						}
					}.frame(maxWidth: .infinity)
						.font(.system(size: 18.0))
				}
			}
		}.task {
			await loadData()
		}

	}
}

#Preview {
	CommonBands()
}
