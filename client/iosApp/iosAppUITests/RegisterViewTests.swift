import XCTest

final class RegisterViewTests: XCTestCase {
	private var app: XCUIApplication!
	
	override func setUpWithError() throws {
		continueAfterFailure = false
		app = XCUIApplication()
		app.launch()
		app.buttons["welcome.createAccount"].tap()
	}
	
	func testRegisterFormRequiresAllFieldsBeforeSubmission() {
		let signUp = app.buttons["register.submit"]
		XCTAssertTrue(signUp.exists)
		XCTAssertFalse(signUp.isEnabled)
		
		XCTAssertTrue(app.textFields["register.email"].exists)
		XCTAssertTrue(app.textFields["register.login"].exists)
		XCTAssertTrue(app.secureTextFields["register.password"].exists)
		XCTAssertTrue(app.secureTextFields["register.confirmPassword"].exists)
	}
	
	func testRegisterFormEnablesSubmissionAfterAllFieldsAreFilled() {
		let email = app.textFields["register.email"]
		email.tap()
		email.typeText("test@example.com")
		let login = app.textFields["register.login"]
		login.tap()
		login.typeText("testuser")
		
		let password = app.secureTextFields["register.password"]
		password.tap()
		password.typeText("Test1234a")
		let confirmPassword = app.secureTextFields["register.confirmPassword"]
		confirmPassword.tap()
		confirmPassword.typeText("Test1234a")
		
		XCTAssertTrue(app.buttons["register.submit"].isEnabled)
	}
	
	func testRegisterSignInLinkOpensSignInView() {
		app.buttons["register.signInLink"].tap()
		
		XCTAssertTrue(app.textFields["signIn.login"].waitForExistence(timeout: 5))
	}
}
