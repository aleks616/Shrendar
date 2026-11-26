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
		Picker(localize(key: "gender"), selection: $gender) {
			Text(localize(key: "male"))
				.tag("M")
			Text(localize(key: "female"))
				.tag("F")
			Text(localize(key: "gender_x"))
				.tag("X")
		}
	}
}
