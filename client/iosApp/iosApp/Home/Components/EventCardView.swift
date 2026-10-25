//
//  EventCardView.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 15/10/2026.
//
import SharedLogic
import SwiftUI

struct EventCardView: View {
	let event: EventWikiDto

	var body: some View {
		VStack(alignment: .leading, spacing: 12) {
			VStack(alignment: .leading, spacing: 4) {
				Text(
					"\(event.yearsSince.map { String(describing: $0) } ?? "unknown") 🎤"
				)
				.font(.system(size: 20).bold())
				NavigationLink(destination: EventDataView(eventId: event.id as! Int)){
					Text(
						"\(event.name ?? "unknown")"
					).font(
						.system(size: 18).bold()
					)
				}
			}
			VStack(alignment: .leading, spacing: 8) {
				Text(
					"\(localize(key: "date")): \(event.date.map { String(describing: $0) } ?? "unknown")"
				)
				Text("\(localize(key: "band")): \(event.bandName ?? "unknown")")
			}
		}
		.frame(width: 320)
		.padding(.horizontal, 16)
		.padding(.vertical, 16)
		.background(Color.gray.opacity(0.1))
		.clipShape(RoundedRectangle(cornerRadius: 12))
		.shadow(radius: 2)
	}
}
