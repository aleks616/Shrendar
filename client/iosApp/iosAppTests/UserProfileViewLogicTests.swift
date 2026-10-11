import SharedLogic
import XCTest
@testable import Shrendar

final class UserProfileViewLogicTests: XCTestCase {
    func testCalculateRankProgressReturnsNilForOutOfRangeRank() {
        let view = UserProfileView(login: "alice")
        let user = UserProfileDto(login: "alice", username: "Alice", rankId: 18, xp: 10)

        XCTAssertNil(view.calculateRankProgress(for: user))
    }

    func testCalculateRankProgressReturnsZeroAtTheStartOfARank() {
        let view = UserProfileView(login: "alice")
        let user = UserProfileDto(login: "alice", username: "Alice", rankId: 2, xp: 15)

        XCTAssertEqual(view.calculateRankProgress(for: user), 0, accuracy: 0.0001)
    }

    func testCalculateRankProgressComputesIntermediateProgress() {
        let view = UserProfileView(login: "alice")
        let user = UserProfileDto(login: "alice", username: "Alice", rankId: 2, xp: 27)

        XCTAssertEqual(view.calculateRankProgress(for: user), 0.48, accuracy: 0.0001)
    }

    func testCalculateRankProgressClampsToOne() {
        let view = UserProfileView(login: "alice")
        let user = UserProfileDto(login: "alice", username: "Alice", rankId: 2, xp: 80)

        XCTAssertEqual(view.calculateRankProgress(for: user), 1, accuracy: 0.0001)
    }

    func testToggleFavoriteRemovesExistingIdentifier() {
        var view = UserProfileView(login: "alice")
        var favoriteIDs: Set<String> = ["21"]

        view.toggleFavorite(21, in: &favoriteIDs)

        XCTAssertTrue(favoriteIDs.isEmpty)
    }

    func testToggleFavoriteStoresIdentifiersUsingTheirStringRepresentation() {
        var view = UserProfileView(login: "alice")
        var favoriteIDs = Set<String>()

        view.toggleFavorite(Int64(144), in: &favoriteIDs)

        XCTAssertEqual(favoriteIDs, ["144"])
    }
}
