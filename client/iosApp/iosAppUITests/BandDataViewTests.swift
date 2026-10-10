import XCTest

final class BandDataViewTests: XCTestCase {
	private var app: XCUIApplication!

	override func setUpWithError() throws {
		continueAfterFailure = false
		app = XCUIApplication()
		app.launchArguments = ["--test-view=band"]
		app.launch()
	}

	func testBandDataViewRendersIndependently() {
		XCTAssertTrue(app.activityIndicators.firstMatch.waitForExistence(timeout: 5))
	}
}
