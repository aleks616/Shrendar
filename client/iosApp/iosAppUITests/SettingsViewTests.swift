import XCTest

final class SettingsViewTests: XCTestCase {
	private var app: XCUIApplication!

	override func setUpWithError() throws {
		continueAfterFailure = false
		app = XCUIApplication()
		app.launchArguments = ["--test-view=settings"]
		app.launch()
	}

	func testSettingsViewRendersIndependently() {
		XCTAssertTrue(app.navigationBars["Settings"].waitForExistence(timeout: 5))
	}
}
