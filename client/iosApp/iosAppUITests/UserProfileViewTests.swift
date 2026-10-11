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
		XCTAssertTrue(app.activityIndicators.firstMatch.waitForExistence(timeout: 10)) //fails
	}
}
