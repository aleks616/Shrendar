import XCTest

final class SettingsViewTests: XCTestCase {
	private var app: XCUIApplication!

	override func setUpWithError() throws {
		continueAfterFailure = false
		app = XCUIApplication()
		app.launchArguments = ["--test-view=settings"]
		app.launch()
	}

	func testSettingsViewRendersIndependentControls() {
		XCTAssertTrue(app.navigationBars["Settings"].waitForExistence(timeout: 5))
		XCTAssertTrue(app.textFields["settings.username"].exists)
		XCTAssertTrue(app.textFields["settings.email"].exists)
		XCTAssertTrue(app.buttons["settings.birthdate"].exists)
		XCTAssertTrue(app.buttons["settings.save"].exists)
		XCTAssertTrue(app.buttons["settings.changePassword"].exists)
		XCTAssertTrue(app.buttons["settings.deleteAccount"].exists)
	}

	func testDeleteAccountFlowOpensCredentialSheet() {
		app.buttons["settings.deleteAccount"].tap()

		XCTAssertTrue(app.textFields["settings.delete.email"].waitForExistence(timeout: 5))
		XCTAssertTrue(app.textFields["settings.delete.login"].exists)
		XCTAssertTrue(app.secureTextFields["settings.delete.password"].exists)
	}
}
