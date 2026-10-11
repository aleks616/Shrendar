//
//  ToggleFavoriteButton.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 05/10/2026.
//
import SwiftUI

func ToggleFavoriteButton(isSelected: Bool, action: @escaping () -> Void)
		-> some View{
		Button(action: action) {
			Image(systemName: isSelected ? "star.fill" : "star")
				.foregroundStyle(isSelected ? Color.accentColor : .secondary)
				.frame(width: 32, height: 32)
		}
		.buttonStyle(.plain)
		.accessibilityLabel(localize(key: "favorite"))
	}
