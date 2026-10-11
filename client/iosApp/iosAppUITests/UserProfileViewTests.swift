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
		let username = app.staticTexts["profile.username"]
		XCTAssertTrue(username.waitForExistence(timeout: 10))
	}
}
