import XCTest

final class AlbumDataViewTests: XCTestCase {
	private var app: XCUIApplication!

	override func setUpWithError() throws {
		continueAfterFailure = false
		app = XCUIApplication()
		app.launchArguments = ["--test-view=album"]
		app.launch()
	}

	func testAlbumDataViewRendersIndependently() {
		XCTAssertTrue(app.activityIndicators.firstMatch.waitForExistence(timeout: 5))
	}
}
