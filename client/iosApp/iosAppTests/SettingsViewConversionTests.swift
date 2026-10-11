import SharedLogic
import XCTest
@testable import Shrendar

final class SettingsViewConversionTests: XCTestCase {
    func testFoundationDateBuildsDateFromSharedLogicDate() {
        let view = SettingsView()
        let date = view.foundationDate(from: SharedLogic.Date(year: 1999, month: 5, day: 20))
        let components = Calendar.current.dateComponents([.year, .month, .day], from: date!)

        XCTAssertEqual(components.year, 1999)
        XCTAssertEqual(components.month, 5)
        XCTAssertEqual(components.day, 20)
    }

    func testFoundationDateReturnsNilWhenSharedLogicDateIsMissing() {
        let view = SettingsView()

        XCTAssertNil(view.foundationDate(from: nil))
    }

    func testKotlinDatePreservesYearMonthAndDay() {
        let view = SettingsView()
        let calendar = Calendar.current
        let date = calendar.date(from: DateComponents(year: 2001, month: 9, day: 11))!
        let kotlinDate = view.kotlinDate(from: date)

        XCTAssertEqual(kotlinDate.year, 2001)
        XCTAssertEqual(kotlinDate.month, 9)
        XCTAssertEqual(kotlinDate.day, 11)
    }

    func testDateConversionRoundTripsSharedLogicBirthdays() {
        let view = SettingsView()
        let original = SharedLogic.Date(year: 1988, month: 12, day: 3)
        let converted = view.foundationDate(from: original)!
        let roundTripped = view.kotlinDate(from: converted)

        XCTAssertEqual(roundTripped.year, original.year)
        XCTAssertEqual(roundTripped.month, original.month)
        XCTAssertEqual(roundTripped.day, original.day)
    }
}
