import XCTest
@testable import Shrendar

final class TranslateDateTests: XCTestCase {
    func testReturnsDashForMissingValue() {
        XCTAssertEqual(translateDate(nil), "-")
        XCTAssertEqual(translateDate(""), "-")
    }

    func testTranslatesTodayKeyword() {
        XCTAssertEqual(translateDate("today"), localize(key: "today"))
    }

    func testLeavesMalformedValuesUntouched() {
        XCTAssertEqual(translateDate("yesterday"), "yesterday")
        XCTAssertEqual(translateDate("days only"), "days only")
    }

    func testTranslatesRelativeDates() {
        XCTAssertEqual(
            translateDate("5 days"),
            "5 \(localize(key: \"days\")) \(localize(key: \"time_ago\"))"
        )
    }
}
