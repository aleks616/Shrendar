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
	@State private var linkedBandId: Int?
	
	var body: some View {
		VStack(alignment: .leading, spacing: 6) {
			Text(localize(key: "band"))
			VStack(alignment: .leading) {
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
						Button(action: {
							selectedBand = band
							searchText = band.name ?? ""
							results = []
						}) {
							HStack {
								Text(band.country.map { "\(band.name ?? "") (\(localize(key: $0)))" } ?? band.name ?? "")
								Spacer()
							}
							.contentShape(Rectangle())
						}
						.buttonStyle(.plain)
						
						Button(action:{
							linkedBandId = Int(band.id)
						}){
							Image(systemName: "arrow.up.right.square")
						}
						.buttonStyle(.glassProminent)
					}.padding(8)
				}
				
			}
			
			.background(Color.secondary.opacity(0.15))
			.cornerRadius(10)
			.background(
				NavigationLink(
					destination: BandDataView(bandId: linkedBandId ?? 0),
					isActive: Binding(
						get: { linkedBandId != nil },
						set: { if !$0 { linkedBandId = nil } }
					)
				) {
					EmptyView()
				}
					.hidden()
			)
			Text(localize(key: "enter_2_plus_characters"))
				.font(.system(size: 16.0))
				.foregroundStyle(.secondary)
		}}
}
