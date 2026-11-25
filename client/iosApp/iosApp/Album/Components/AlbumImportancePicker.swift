//
//  AlbumImportancePicker.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 25/11/2026.
//

import SwiftUI

struct AlbumImportancePicker: View {
	let albumType: String?
	@Binding var importance: Int

	var body: some View {
		let maxImportance = albumType == "studio" ? 5 : 3
		VStack(alignment: .leading, spacing: 6) {
			Text(localize(key: "album_importance"))
			Picker(localize(key: "album_importance"), selection: $importance) {
				ForEach(1...maxImportance, id: \.self) { value in
					Text("\(value)")
						.tag(value)
				}
			}
			.labelsHidden()
			.pickerStyle(.menu)
			.disabled(albumType != "studio" && albumType != "ep")
			.opacity(albumType != "studio" && albumType != "ep" ? 0.4 : 1)
			Text(localize(key: "album_importance_info"))
				.font(.system(size: 16.0))
				.foregroundStyle(.secondary)
		}
	}
}
