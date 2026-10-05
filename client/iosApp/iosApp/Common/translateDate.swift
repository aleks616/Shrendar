//
//  translateDate.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 05/10/2026.
//


func translateDate(_ date: String?) -> String {
		guard let date, !date.isEmpty else {
			return "-"
		}
		if date == "today" {
			return localize(key: "today")
		}

		let parts = date.split(separator: " ")
		guard parts.count >= 2, let amount = Int(parts[0]) else {
			return date
		}
		return
			"\(amount) \(localize(key: String(parts[1]))) \(localize(key: "time_ago"))"
	}
