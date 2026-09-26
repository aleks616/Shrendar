import XCTest

final class SecureInputViewTests: XCTestCase {
	private var app: XCUIApplication!
	
	override func setUpWithError() throws {
		continueAfterFailure = false
		app = XCUIApplication()
		app.launch()
		app.buttons["welcome.signIn"].tap()
	}
	
	func testPasswordCanBeRevealedAndSecuredAgain() {
		let secureField = app.secureTextFields["signIn.password"]
		XCTAssertTrue(secureField.exists)
		
		secureField.tap()
		secureField.typeText("Test1234a")
		app.buttons["signIn.password.visibilityToggle"].tap()
		
		let visibleField = app.textFields["signIn.password"]
		XCTAssertTrue(visibleField.waitForExistence(timeout: 3))
		XCTAssertEqual(visibleField.value as? String, "Test1234a")
		
		app.buttons["signIn.password.visibilityToggle"].tap()
		XCTAssertTrue(secureField.waitForExistence(timeout: 3))
	}
}
