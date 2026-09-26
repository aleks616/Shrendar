import XCTest

final class SignInViewTests: XCTestCase {
	private var app: XCUIApplication!
	
	override func setUpWithError() throws {
		continueAfterFailure = false
		app = XCUIApplication()
		app.launch()
		app.buttons["welcome.signIn"].tap()
	}
	
	func testSignInFormRequiresBothFieldsBeforeSubmission() {
		let signIn = app.buttons["signIn.submit"]
		XCTAssertTrue(signIn.exists)
		XCTAssertFalse(signIn.isEnabled)
		
		XCTAssertTrue(app.textFields["signIn.login"].exists)
		XCTAssertTrue(app.secureTextFields["signIn.password"].exists)
	}
	
	func testSignInFormEnablesSubmissionWhenCredentialsAreEntered() {
		let login = app.textFields["signIn.login"]
		login.tap()
		login.typeText("testuser")
		let password = app.secureTextFields["signIn.password"]
		password.tap()
		password.typeText("Test1234a")
		
		XCTAssertTrue(app.buttons["signIn.submit"].isEnabled)
	}
}
