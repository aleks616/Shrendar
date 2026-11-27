//
//  CalendarDateField.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 25/11/2026.
//

import SwiftUI

struct CalendarDateField: View {
	let label: String
	let minDate: Foundation.Date
	let maxDate: Foundation.Date
	let isDisabled: Bool
	let fieldError: String?
	@Binding var date: Foundation.Date?
	@State private var modalOpen = false
	@State private var pickedDate = Foundation.Date()

	var body: some View {
		VStack(alignment: .leading, spacing: 6) {
			Text(label)
				Button(action: {
					pickedDate = date ?? min(max(Foundation.Date(), minDate), maxDate)
					modalOpen = true
				}) {
				Text(
					date?.formatted(
						Foundation.Date.ISO8601FormatStyle(timeZone: Calendar.current.timeZone)
							.year().month().day()
					) ?? "YYYY-MM-DD"
				)
				.foregroundColor(date == nil ? .secondary : .primary)
				.frame(maxWidth: .infinity, alignment: .leading)
				.padding(12)
				.background(Color.secondary.opacity(0.15))
				.cornerRadius(10)
				.contentShape(Rectangle())
			}
			.buttonStyle(.plain)
			.disabled(isDisabled)
			.opacity(isDisabled ? 0.4 : 1)
			if let fieldError {
				Text(fieldError)
					.font(.system(size: 16.0))
					.foregroundStyle(.red)
			}
		}
		.sheet(isPresented: $modalOpen) {
			NavigationStack {
				DatePicker(
					label,
					selection: $pickedDate,
					in: minDate...maxDate,
					displayedComponents: .date
				)
				.datePickerStyle(.graphical)
				.padding()
				.toolbar {
					ToolbarItem(placement: .cancellationAction) {
						Button(localize(key: "cancel")) {
							modalOpen = false
						}
					}
					ToolbarItem(placement: .confirmationAction) {
						Button(localize(key: "done")) {
							date = pickedDate
							modalOpen = false
						}
					}
				}
			}
			.presentationDetents([.medium])
		}
	}
}

#Preview {
	CalendarDateField(
		label: "Release date",
		minDate: .distantPast,
		maxDate: .distantFuture,
		isDisabled: false,
		fieldError: nil,
		date: .constant(nil)
	)
}
