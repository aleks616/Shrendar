import XCTest

final class ArtistDataViewTests: XCTestCase {
	private var app: XCUIApplication!

	override func setUpWithError() throws {
		continueAfterFailure = false
		app = XCUIApplication()
		app.launchArguments = ["--test-view=artist"]
		app.launch()
	}

	func testArtistDataViewRendersIndependently() {
		XCTAssertTrue(app.activityIndicators.firstMatch.waitForExistence(timeout: 5))
	}
}
