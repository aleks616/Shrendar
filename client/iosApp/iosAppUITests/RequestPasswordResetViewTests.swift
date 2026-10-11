import XCTest

final class RequestPasswordResetViewTests: XCTestCase {
	private var app: XCUIApplication!

	override func setUpWithError() throws {
		continueAfterFailure = false
		app = XCUIApplication()
		app.launchArguments = ["--test-view=requestPasswordReset"]
		app.launch()
	}

	func testRequestPasswordResetViewShowsItsForm() {
		XCTAssertTrue(app.staticTexts["Forgot password?"].waitForExistence(timeout: 5))
		XCTAssertTrue(app.textFields["requestPasswordReset.login"].exists)
		XCTAssertTrue(app.buttons["requestPasswordReset.submit"].exists)
		XCTAssertFalse(app.buttons["requestPasswordReset.submit"].isEnabled)
	}
}
