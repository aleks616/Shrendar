import XCTest

final class EventDataViewTests: XCTestCase {
	private var app: XCUIApplication!

	override func setUpWithError() throws {
		continueAfterFailure = false
		app = XCUIApplication()
		app.launchArguments = ["--test-view=event"]
		app.launch()
	}

	func testEventDataViewRendersIndependently() {
		XCTAssertTrue(app.activityIndicators.firstMatch.waitForExistence(timeout: 5))
	}
}
