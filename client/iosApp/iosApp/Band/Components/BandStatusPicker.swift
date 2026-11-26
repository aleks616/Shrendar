//
//  BandStatusPicker.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 25/11/2026.
//

import SwiftUI

struct BandStatusPicker: View {
	@Binding var status: String

	var body: some View {
		VStack(alignment: .leading, spacing: 6) {
			Text(localize(key: "status"))
			Picker(localize(key: "status"), selection: $status) {
				Text(localize(key: "active"))
					.tag("active")
				Text(localize(key: "disbanded"))
					.tag("disbanded")
				Text(localize(key: "on_hold"))
					.tag("on_hold")
				Text(localize(key: "unknown"))
					.tag("unknown")
			}
			.labelsHidden()
			.pickerStyle(.menu)
		}
	}
}
