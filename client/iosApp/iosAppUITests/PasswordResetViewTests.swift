//
// Created by Aleks Jankowiak on 30/09/2026.
//

import Foundation
import XCTest

final class PasswordResetViewTests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }

    func testRequestPasswordResetRequiresAnAccountKey() {
        app.buttons["welcome.signIn"].tap()
        app.buttons["signin.resetPassword"].tap()

        let submit = app.buttons["requestPasswordReset.submit"]
        XCTAssertTrue(submit.exists)
        XCTAssertFalse(submit.isEnabled)
        XCTAssertTrue(app.textFields["requestPasswordReset.login"].exists)

        let login = app.textFields["requestPasswordReset.login"]
        login.tap()
        login.typeText("alice@example.com")

        XCTAssertTrue(submit.isEnabled)
    }

    func testCreatePasswordRequiresBothPasswordFields() {
        app.open(URL(string: "shrendar://reset-password?code=123456&account=alice%40example.com")!)

        let submit = app.buttons["resetPassword.submit"]
        XCTAssertTrue(submit.exists)
        XCTAssertFalse(submit.isEnabled)
        XCTAssertTrue(app.secureTextFields["resetPassword.password"].exists)
        XCTAssertTrue(app.secureTextFields["resetPassword.confirmPassword"].exists)
    }

    func testCreatePasswordShowsMismatchError() {
        app.open(URL(string: "shrendar://reset-password?code=123456&account=alice%40example.com")!)

        let password = app.secureTextFields["resetPassword.password"]
        XCTAssertTrue(password.waitForExistence(timeout: 5))
        XCTAssertTrue(password.isHittable)
        password.tap()
        XCTAssertTrue(app.keyboards.firstMatch.waitForExistence(timeout: 3))
        password.typeText("NewPassword1!")
        let confirmPassword = app.secureTextFields["resetPassword.confirmPassword"]
        confirmPassword.tap()
        XCTAssertTrue(app.keyboards.firstMatch.exists)
        confirmPassword.typeText("DifferentPassword1!")
        app.buttons["resetPassword.submit"].tap()

        XCTAssertTrue(app.staticTexts["Passwords don't match"].waitForExistence(timeout: 3))
    }
}
