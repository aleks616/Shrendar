//
//  BandPicker.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 25/11/2026.
//

import SharedLogic
import SwiftUI

struct BandPicker: View {
	@Binding var selectedBand: BandAlbumAddDto?
	@State private var searchText: String = ""
	@State private var results: [BandAlbumAddDto] = []
	@State private var linkedBandId: Int? = nil

	var body: some View {
		VStack(alignment: .leading, spacing: 6) {
			Text(localize(key: "band"))
			VStack(alignment: .leading, spacing: 0) {
				TextField(localize(key: "search_band_dots"), text: $searchText)
					.autocorrectionDisabled()
					.padding(12)
					.onChange(of: searchText) { _, newValue in
						if newValue == selectedBand?.name {
							return
						}
						if newValue.count >= 2 {
							Task {
								if let bands = try? await BandClient().getBandDataForAlbumAdding(name: newValue) {
									results = bands
								}
							}
						} else {
							results = []
						}
					}
				ForEach(results, id: \.id) { band in
					Divider()
					HStack {
						Button {
							selectedBand = band
							searchText = band.name ?? ""
							results = []
						} label: {
							HStack {
								Text(band.country.map { "\(band.name ?? "") (\(localize(key: $0)))" } ?? band.name ?? "")
								Spacer()
							}
							.contentShape(Rectangle())
						}
						.buttonStyle(.plain)
						Button {
							linkedBandId = Int(band.id)
						} label: {
							Image(systemName: "arrow.up.right.square")
								.contentShape(Rectangle())
						}
						.buttonStyle(.plain)
					}
					.padding(12)
				}
			}
			.background(Color.secondary.opacity(0.15))
			.cornerRadius(10)
			Text(localize(key: "enter_2_plus_characters"))
				.font(.system(size: 16.0))
				.foregroundStyle(.secondary)
		}
		.navigationDestination(item: $linkedBandId) { bandId in
			BandDataView(bandId: bandId)
		}
	}
}
