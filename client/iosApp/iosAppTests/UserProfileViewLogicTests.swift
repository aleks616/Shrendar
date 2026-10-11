import SharedLogic
import XCTest

@testable import Shrendar

final class UserProfileViewLogicTests: XCTestCase {
	func testCalculateRankProgressReturnsNilForOutOfRangeRank() {
		let view = UserProfileView(login: "alice")
		let user = UserProfileDto(
			login: "alice",
			username: "Alice",
			rankId: 18,
			rankName: "",
			xp: 10,
			bio: nil,
			accountAge: nil,
			lastLogin: nil,
			favoriteBands: nil,
			favoriteArtists: nil,
			favoriteGenres: nil,
			contributions: nil,
			user: false
		)

		XCTAssertNil(view.calculateRankProgress(for: user))
	}

	func testCalculateRankProgressReturnsZeroAtTheStartOfARank() {
		let view = UserProfileView(login: "alice")
		let user = UserProfileDto(
			login: "alice",
			username: "Alice",
			rankId: 2,
			rankName: "",
			xp: 15,
			bio: nil,
			accountAge: nil,
			lastLogin: nil,
			favoriteBands: nil,
			favoriteArtists: nil,
			favoriteGenres: nil,
			contributions: nil,
			user: false
		)

		guard let progress = view.calculateRankProgress(for: user) else {
			return XCTFail("Expected rank progress")
		}

		XCTAssertEqual(progress, 0, accuracy: 0.0001)
	}

	func testCalculateRankProgressComputesIntermediateProgress() {
		let view = UserProfileView(login: "alice")
		let user = UserProfileDto(
			login: "alice",
			username: "Alice",
			rankId: 2,
			rankName: "",
			xp: 27,
			bio: nil,
			accountAge: nil,
			lastLogin: nil,
			favoriteBands: nil,
			favoriteArtists: nil,
			favoriteGenres: nil,
			contributions: nil,
			user: false
		)

		guard let progress = view.calculateRankProgress(for: user) else {
			return XCTFail("Expected rank progress")
		}

		XCTAssertEqual(progress, 0.48, accuracy: 0.0001)
	}

	func testCalculateRankProgressClampsToOne() {
		let view = UserProfileView(login: "alice")
		let user = UserProfileDto(
			login: "alice",
			username: "Alice",
			rankId: 2,
			rankName: "",
			xp: 80,
			bio: nil,
			accountAge: nil,
			lastLogin: nil,
			favoriteBands: nil,
			favoriteArtists: nil,
			favoriteGenres: nil,
			contributions: nil,
			user: false
		)

		guard let progress = view.calculateRankProgress(for: user) else {
			return XCTFail("Expected rank progress")
		}

		XCTAssertEqual(progress, 1, accuracy: 0.0001)
	}

	func testToggleFavoriteRemovesExistingIdentifier() {
		let view = UserProfileView(login: "alice")
		var favoriteIDs: Set<String> = ["21"]

		view.toggleFavorite(21, in: &favoriteIDs)

		XCTAssertTrue(favoriteIDs.isEmpty)
	}

	func testToggleFavoriteStoresIdentifiersUsingTheirStringRepresentation() {
		let view = UserProfileView(login: "alice")
		var favoriteIDs = Set<String>()

		view.toggleFavorite(Int64(144), in: &favoriteIDs)

		XCTAssertEqual(favoriteIDs, ["144"])
	}
}
