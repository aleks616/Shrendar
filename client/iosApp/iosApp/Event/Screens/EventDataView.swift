//
//  EventDataView.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 10/10/2026.
//

import SharedLogic
import SwiftUI

struct EventDataView: View {
	let eventId: Int

	@State private var event: EventWikiDto?
	@State private var isLoading = true

	var body: some View {
		NavigationStack {
			Group {
				if isLoading {
					ProgressView()
						.controlSize(.large)
						.frame(maxWidth: .infinity, maxHeight: .infinity)
				} else if let event {
					ScrollView {
						VStack(alignment: .leading, spacing: 16) {
							Text(event.name ?? "")
								.font(.largeTitle.bold())

							HStack(alignment: .top, spacing: 20) {
								VStack(alignment: .leading, spacing: 8) {
									HStack(alignment: .firstTextBaseline, spacing: 4) {
										Text("\(localize(key: "band")):")
											.foregroundStyle(.secondary)
										Text(event.bandName ?? "-")
									}

									HStack(alignment: .firstTextBaseline, spacing: 4) {
										Text("\(localize(key: "date")):")
											.foregroundStyle(.secondary)
										Text(
											event.date.map {
												String(describing: $0)
											} ?? "-"
										)
										Text(" \(localize(key: "anniversary_in"))")
											.foregroundStyle(.secondary)
										Text(
											event.daysTillAnniversary.map {
												String(describing: $0)
											} ?? "-"
										)
										Text("\(localize(key: "days"))")
									}

									HStack(alignment: .firstTextBaseline, spacing: 4) {
										Text("\(localize(key: "years_since")):")
											.foregroundStyle(.secondary)
										Text(
											event.yearsSince.map {
												String(describing: $0)
											} ?? "-"
										)
									}
								}
								.frame(maxWidth: .infinity, alignment: .leading)
							}

							TranslatedDescription(
								description: event.description_ ?? ""
							)
						}
						.padding(.horizontal, 12)
						.padding(.vertical, 16)
					}
				}
			}
			.task(id: eventId) {
				await loadEvent()
			}
		}
	}

	private func loadEvent() async {
		isLoading = true

		do {
			event = try await EventClient().getEventData(
				id: Int32(eventId)
			)
		} catch {
			event = nil
		}
		isLoading = false
	}
}
