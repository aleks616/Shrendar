import XCTest
@testable import Shrendar

final class LocalizeTests: XCTestCase {
    func testWelcomeKeyReturnsLocalizedText() {
        XCTAssertTrue(["Welcome", "Witaj"].contains(localize(key: "welcome")))
    }

    func testSignInKeyReturnsLocalizedText() {
        XCTAssertTrue(["Sign in", "Zaloguj się"].contains(localize(key: "sign_in")))
    }
}
