//
//  GenderPicker.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 25/11/2026.
//

import SwiftUI

struct GenderPicker: View {
	@Binding var gender: String

	var body: some View {
		VStack(alignment: .leading, spacing: 6) {
			Text(localize(key: "gender"))
			Picker(localize(key: "select_gender"), selection: $gender) {
				Text(localize(key: "male"))
					.tag("M")
				Text(localize(key: "female"))
					.tag("F")
				Text(localize(key: "gender_x"))
					.tag("X")
			}
			.labelsHidden()
			.pickerStyle(.menu)
		}
	}
}
