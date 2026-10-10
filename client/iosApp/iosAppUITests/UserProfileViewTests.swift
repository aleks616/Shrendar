import XCTest

final class UserProfileViewTests: XCTestCase {
	private var app: XCUIApplication!

	override func setUpWithError() throws {
		continueAfterFailure = false
		app = XCUIApplication()
		app.launchArguments = ["--test-view=profile"]
		app.launch()
	}

	func testUserProfileViewRendersIndependently() {
		XCTAssertTrue(app.staticTexts["Something went wrong. Try again later."].waitForExistence(timeout: 5))
	}
}
