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

	var isValid: Bool {
		selectedBand != nil && date != nil && !name.isEmpty && !description.isEmpty
	}

	var body: some View {
		NavigationStack {
			VStack(spacing: 4) {
				Text(localize(key: "enter_event_data"))
					.font(.system(size: 28.0, weight: .bold))

				Form {
					BandPicker(selectedBand: $selectedBand)

					CalendarDateField(
						label: localize(key: "date"),
						minDate: minDate,
						maxDate: maxDate,
						isDisabled: selectedBand == nil,
						fieldError: selectedBand == nil ? localize(key: "choose_band_first") : nil,
						date: $date
					)

					VStack(alignment: .leading, spacing: 6) {
						Text(localize(key: "event_name"))
						TextField(localize(key: "event_name"), text: $name)
							.padding(12)
							.background(Color.secondary.opacity(0.15))
							.cornerRadius(10)
					}

					VStack(alignment: .leading, spacing: 6) {
						Text(localize(key: "description"))
						TextField(localize(key: "enter_event_description"), text: $description, axis: .vertical)
							.lineLimit(4...)
							.padding(12)
							.background(Color.secondary.opacity(0.15))
							.cornerRadius(10)
					}
					Section {
						VStack {
							if !resultText.isEmpty {
								Text(localize(key: resultText))
							}

							Button(action: submit) {
								Text(localize(key: "add")).frame(width: 140)
							}
							.buttonStyle(.plain)
							.disabled(!isValid)
							.padding()
							.background(Color.accentColor)
							.foregroundColor(.white)
							.glassEffect()
							.cornerRadius(25)
						}
						.frame(maxWidth: .infinity)
						.padding(.bottom, 8)
					}
					.listRowBackground(Color.clear)
				}
				.simultaneousGesture(TapGesture().onEnded {
					UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
				})
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
