import XCTest

final class LabelledDividerTests: XCTestCase {
    private var app: XCUIApplication!

    override func setUpWithError() throws {
        continueAfterFailure = false
        app = XCUIApplication()
        app.launch()
        app.buttons.matching(NSPredicate(
            format: "label IN %@",
            ["Create account", "Utwórz konto"]
        )).firstMatch.tap()
    }

    func testRegisterDividerDisplaysItsLocalizedLabel() {
        let dividerLabel = app.staticTexts.matching(NSPredicate(
            format: "label IN %@",
            ["or", "albo"]
        )).firstMatch

        XCTAssertTrue(dividerLabel.waitForExistence(timeout: 5))
    }
}
