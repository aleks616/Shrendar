//
//  AlbumTypePicker.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 25/11/2026.
//

import SwiftUI

struct AlbumTypePicker: View {
	@Binding var albumType: String?
	@Binding var importance: Int
	let types: [String] = ["studio", "ep", "compilation", "concert", "demo", "single", "other"]

	var body: some View {
		VStack(alignment: .leading, spacing: 6) {
			Text(localize(key: "album_type"))
			Picker(localize(key: "album_type"), selection: $albumType) {
				Text(localize(key: "choose_album_type"))
					.tag(nil as String?)
				ForEach(types, id: \.self) { type in
					Text(localize(key: "album_type_" + type))
						.tag(type as String?)
				}
			}
			.labelsHidden()
			.pickerStyle(.menu)
			.onChange(of: albumType) { _, newValue in
				if newValue == "ep" && importance > 3 {
					importance = 3
				}
			}
		}
	}
}
