import XCTest
@testable import Shrendar

final class KeychainServiceTests: XCTestCase {
    override func setUpWithError() throws {
        XCTAssertTrue(KeychainService.removeToken())
    }

    override func tearDownWithError() throws {
        XCTAssertTrue(KeychainService.removeToken())
    }

    func testSavedTokenCanBeRetrieved() {
        let token = "ios-keychain-test-token"

        XCTAssertTrue(KeychainService.saveToken(token))
        XCTAssertEqual(KeychainService.retrieveToken(), token)
    }

    func testRemovingSavedTokenMakesItUnavailable() {
        XCTAssertTrue(KeychainService.saveToken("ios-keychain-test-token"))

        XCTAssertTrue(KeychainService.removeToken())
        XCTAssertNil(KeychainService.retrieveToken())
    }

    func testRemovingMissingTokenSucceeds() {
        XCTAssertTrue(KeychainService.removeToken())
    }
}
