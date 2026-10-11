import SwiftUI
import XCTest
@testable import Shrendar

final class LogOutButtonTests: XCTestCase {
    func testBodyRendersAsAButton() {
        XCTAssertTrue(LogOutButton().body is Button<Text>)
    }
}
