//
//  DataTableHeightPreferenceKey.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 05/10/2026.
//
import SwiftUI

struct DataTableHeightPreferenceKey: PreferenceKey {
	static let defaultValue: CGFloat = 0

	static func reduce(value: inout CGFloat, nextValue: () -> CGFloat) {
		value = max(value, nextValue())
	}
}
