import XCTest

final class WelcomeViewTests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
    }

    func testWelcomeViewShowsLocalizedContentAndEntryActions() {
        XCTAssertTrue(staticText(["Welcome", "Witaj"]).waitForExistence(timeout: 5))
        XCTAssertTrue(staticText([
            "Create an account or sign in to continue",
            "Stwórz konto albo zaloguj się, aby kontynuować",
        ]).exists)
        XCTAssertTrue(button(["Create account", "Utwórz konto"]).exists)
        XCTAssertTrue(button(["Already have an account", "Mam już konto"]).exists)
    }

    func testCreateAccountActionOpensRegisterView() {
        button(["Create account", "Utwórz konto"]).tap()

        XCTAssertTrue(staticText(["Create account", "Utwórz konto"]).waitForExistence(timeout: 5))
        XCTAssertTrue(staticText(["Sign Up to Continue", "Zarejestruj się, aby kontynuować"]).exists)
    }

    func testExistingAccountActionOpensSignInView() {
        button(["Already have an account", "Mam już konto"]).tap()

        XCTAssertTrue(staticText(["Sign in", "Zaloguj się"]).waitForExistence(timeout: 5))
    }

    private func staticText(_ labels: [String]) -> XCUIElement {
        app.staticTexts.matching(NSPredicate(format: "label IN %@", labels)).firstMatch
    }

    private func button(_ labels: [String]) -> XCUIElement {
        app.buttons.matching(NSPredicate(format: "label IN %@", labels)).firstMatch
    }
}
