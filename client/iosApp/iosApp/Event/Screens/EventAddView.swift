//
//  EventAddView.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 25/11/2026.
//

import SharedLogic
import SwiftUI

struct EventAddView: View {
	@State private var selectedBand: BandAlbumAddDto? = nil
	@State private var date: Foundation.Date? = nil
	@State private var name: String = ""
	@State private var description: String = ""
	@State private var resultText: String = ""

	var minDate: Foundation.Date {
		guard let formedYear = selectedBand?.formedYear?.intValue else { return .distantPast }
		return Calendar.current.date(from: DateComponents(year: Int(formedYear), month: 1, day: 1)) ?? .distantPast
	}

	var maxDate: Foundation.Date {
		Calendar.current.date(byAdding: .year, value: 1, to: Foundation.Date()) ?? .distantFuture
	}

	var body: some View {
		NavigationStack {
			Group {
				Form {
					Section {
						Text(localize(key: "enter_event_data"))
							.font(.system(size: 24.0, weight: .bold))
					}

					BandPicker(selectedBand: $selectedBand)

					DatePicker(
						localize(key: "date"),
						selection: Binding(
							get: { date ?? maxDate },
							set: { date = $0 }
						),
						in: minDate...maxDate,
						displayedComponents: .date
					)
					.disabled(selectedBand == nil)

					TextField(localize(key: "event_name"), text: $name)

					TextField(localize(key: "description"), text: $description, axis: .vertical)
						.lineLimit(4...)

					Button(localize(key: "add")) {
						submit()
					}
					.disabled(selectedBand == nil || date == nil || name.isEmpty || description.isEmpty)

					if !resultText.isEmpty {
						Text(localize(key: resultText))
					}
				}
			}
		}
	}

	func submit() {
		guard let bandId = selectedBand?.id, let date else { return }

		let token = KeychainService.retrieveToken() ?? ""

		Task {
			do {
				let event = EventAddDto(
					id: nil,
					bandId: KotlinInt(int: bandId),
					date: date.formatted(
						Foundation.Date.ISO8601FormatStyle(timeZone: Calendar.current.timeZone).year().month().day()
					),
					name: name,
					description: description
				)
				resultText = try await EventClient().addEvent(
					event: event,
					token: token
				)
			} catch {
				resultText = "something_wrong"
			}
		}
	}
}

#Preview {
	EventAddView()
}
