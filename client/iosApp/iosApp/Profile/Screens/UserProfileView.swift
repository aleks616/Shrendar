import SharedLogic
import SwiftUI

struct UserProfileView: View {
	let login: String

	@State var user: UserProfileDto?
	@State var isLoading = true
	@State var selectedTab: ProfileTab = .favoriteBands
	@State var favoriteBandIDs = Set<String>()
	@State var favoriteArtistIDs = Set<String>()
	@State var favoriteGenreIDs = Set<String>()
	@State var showLastOnline = false
	@State var bio = ""
	@State var isReportDialogOpen = false
	@State var reportReason = ""
	@State var canReport = true

	let ranks = [
		1, 15, 40, 120, 270, 520, 820, 1200, 1700,
		2400, 3500, 5500, 8000, 11000, 16000, 21000,
		182500, 400000,
	]

	enum ProfileTab: String, CaseIterable {
		case favoriteBands
		case favoriteArtists
		case favoriteGenres

		var localizationKey: String {
			switch self {
			case .favoriteBands:
				return "favorite_bands"
			case .favoriteArtists:
				return "favorite_artists"
			case .favoriteGenres:
				return "favorite_genres"
			}
		}
	}

	func toggleBandFavorite(_ band: FavoriteBandDto) {
		guard let id = band.id else { return }
		Task { @MainActor in
			do {
				let result = try await ProfileClient().toggleFavoriteBand(
					bandId: Int32(truncating: id),
					token: KeychainService.retrieveToken()
				)
				guard result == "band_toggled" else {
					print(result ?? "")
					return
				}
				toggleFavorite(id, in: &favoriteBandIDs)
			} catch {
				print(error.localizedDescription)
			}
		}
	}

	func toggleArtistFavorite(_ artist: FavoriteArtistDto) {
		guard let id = artist.id else { return }
		Task { @MainActor in
			do {
				let result = try await ProfileClient().toggleFavoriteArtist(
					artistId: Int64(truncating: id),
					token: KeychainService.retrieveToken()
				)
				guard result == "artist_toggled" else {
					print(result ?? "")
					return
				}
				toggleFavorite(id, in: &favoriteArtistIDs)
			} catch {
				print(error.localizedDescription)
			}
		}
	}

	func toggleGenreFavorite(_ genre: FavoriteGenreDto) {
		guard let id = genre.id else { return }
		Task { @MainActor in
			do {
				let result = try await ProfileClient().toggleFavoriteGenre(
					genreId: Int32(truncating: id),
					token: KeychainService.retrieveToken()
				)
				guard result == "genre_toggled" else {
					print(result ?? "")
					return
				}
				toggleFavorite(id, in: &favoriteGenreIDs)
			} catch {
				print(error.localizedDescription)
			}
		}
	}

	func toggleFavorite<T>(_ id: T, in ids: inout Set<String>) {
		let key = String(describing: id)
		if ids.contains(key) {
			ids.remove(key)
		} else {
			ids.insert(key)
		}
	}

	func updateBio(_ value: String) {
		bio = value
		Task { @MainActor in
			do {
				let result = try await ProfileClient().updateBio(
					bio: value,
					token: KeychainService.retrieveToken()
				)
				guard result == "bio_added" else {
					print(result ?? "-")
					return
				}
			} catch {
				print("updating bio error")
			}
		}
	}

	func reportUser() {
		isReportDialogOpen = false
		Task { @MainActor in
			do {
				if user?.userId == nil {
					return
				}
				let reportRequest = ReportRequestDto(
					reportedUserId: Int32(user!.userId),
					reason: reportReason
				)
				let token = KeychainService.retrieveToken()
				let result = try await ReportClient().reportUser(
					report: reportRequest,
					token: token
				)
				if result == "report_success" {
					reportReason = ""
					canReport = false
				}
			} catch {
				print(error.localizedDescription)
			}

		}
	}

	var body: some View {
		NavigationStack {
			Group {
				if isLoading {
					ProgressView()
						.controlSize(.large)
						.frame(maxWidth: .infinity, maxHeight: .infinity)
				} else if let user {
					ScrollView {
						VStack(alignment: .leading, spacing: 16) {
							HStack {
								HStack(spacing: 14) {
									ZStack(alignment: .bottomTrailing) {
										Image(systemName: "person.fill")
											.resizable()
											.scaledToFit()
											.padding(16)
											.foregroundStyle(Color.accentColor)
											.frame(width: 80, height: 80)
											.background(Color.accentColor.opacity(0.15))
											.clipShape(RoundedRectangle(cornerRadius: 16))

										Circle()
											.fill(.gray)
											.frame(width: 24, height: 24)
											.overlay {
												Circle()
													.stroke(.background, lineWidth: 2)
											}
											.offset(x: 6, y: 6)
											.onTapGesture {
												showLastOnline = true
											}
											.popover(isPresented: $showLastOnline) {
												Text(
													"\(localize(key: "last_online")) \(translateDate(user.lastLogin))"
												)
												.padding()
												.presentationCompactAdaptation(.popover)
											}
									}

									VStack(alignment: .leading, spacing: 2) {
										Text(user.username)
											.font(.title2.bold())
											.accessibilityIdentifier("profile.username")
										Text("@\(user.login)")
											.font(.title3)
											.foregroundStyle(.secondary)
										Text(
											"\(localize(key: "member_since")) \(translateDate(user.accountAge))"
										)
										.font(.footnote)
									}
								}
								Spacer()
								Button(localize(key: "report")) {
									isReportDialogOpen = true
								}.buttonStyle(.glass)
									.disabled(!canReport)
									.tint(.red)
							}

							if let progress = calculateRankProgress(for: user) {
								VStack(alignment: .leading, spacing: 6) {
									HStack(alignment: .top) {
										Text(
											"\(localize(key: "level")) \(user.rankId)\n\(localize(key: "rank\(user.rankId)"))"
										)
										.font(.subheadline)

										Spacer()

										Text(
											"\(localize(key: "level")) \(user.rankId + 1)\n\(localize(key: "rank\(user.rankId + 1)"))"
										)
										.multilineTextAlignment(.trailing)
										.font(.subheadline)
									}

									ProgressView(value: progress)
										.tint(.accentColor)
								}
							}

							VStack(alignment: .leading, spacing: 6) {
								Text(localize(key: "bio"))
									.font(.headline)

								if user.user {
									EditableSurface(
										value: $bio,
										onSave: updateBio
									)
								} else {
									ScrollView {
										Text(user.bio ?? "")
											.frame(
												maxWidth: .infinity,
												alignment: .leading
											)
											.padding(10)
									}
									.frame(
										maxWidth: .infinity,
										minHeight: 80,
										maxHeight: 208
									)
									.background(Color.secondary.opacity(0.12))
									.overlay(
										RoundedRectangle(cornerRadius: 12)
											.stroke(Color.accentColor, lineWidth: 1)
									)
									.clipShape(RoundedRectangle(cornerRadius: 12))
								}
							}

							Picker("", selection: $selectedTab) {
								ForEach(ProfileTab.allCases, id: \.self) { tab in
									Text(localize(key: tab.localizationKey))
										.tag(tab)
								}
							}
							.pickerStyle(.segmented)

							switch selectedTab {
							case .favoriteBands:
								FavoriteBandsTable(
									favoriteBands: user.favoriteBands,
									isOwnProfile: user.user,
									favoriteIds: favoriteBandIDs,
									onToggle: toggleBandFavorite
								)
							case .favoriteArtists:
								FavoriteArtistsTable(
									favoriteArtists: user.favoriteArtists,
									isOwnProfile: user.user,
									favoriteIds: favoriteArtistIDs,
									onToggle: toggleArtistFavorite
								)
							case .favoriteGenres:
								FavoriteGenresTable(
									favoriteGenres: user.favoriteGenres,
									isOwnProfile: user.user,
									favoriteIds: favoriteGenreIDs,
									onToggle: toggleGenreFavorite
								)
							}

							Text(localize(key: "contributions"))
								.font(.headline)
								.padding(.top, 4)
							ContributionsTable(user)
						}
						.padding(.horizontal, 12)
						.padding(.vertical, 16)
					}
				}
			}
			.navigationBarTitleDisplayMode(.inline)
			.sheet(isPresented: $isReportDialogOpen) {
				NavigationStack {
					Form {
						TextField(
							localize(key: "report_reason"),
							text: $reportReason,
							axis: .vertical
						)
						.accessibilityIdentifier("profile.reportuser.reason")
						.lineLimit(3...8)
					}
					.navigationTitle(
						"\(localize(key: "report")) \(user?.login ?? "error")?"
					)
					.toolbar {
						ToolbarItem(placement: .cancellationAction) {
							Button(localize(key: "cancel")) {
								isReportDialogOpen = false
							}
						}
						ToolbarItem(placement: .confirmationAction) {
							Button(localize(key: "report")) {
								reportUser()
							}.tint(.red)
							.disabled(
								reportReason.count > 0 && reportReason.count < 15
							)
						}
					}
					if reportReason.count > 0 && reportReason.count < 15 {
						Text(localize(key: "report_invalid")).foregroundStyle(.red)
							.font(.footnote)
					}
				}.presentationDetents([.large])
			}
		}
		.task(id: login) {
			await loadUser()
		}
	}

	func calculateRankProgress(for user: UserProfileDto) -> Double? {
		let rank = Int(user.rankId)
		guard rank >= 1, rank <= 17 else {
			return nil
		}

		let currentMinimum = ranks[rank - 1]
		let nextMinimum = ranks[rank]
		let progress =
			(Double(user.xp) - Double(currentMinimum))
			/ Double(nextMinimum - currentMinimum)
		return min(max(progress, 0), 1)
	}

	func loadUser() async {
		isLoading = true

		guard !login.trimmingCharacters(in: .whitespacesAndNewlines).isEmpty
		else {
			isLoading = false
			return
		}

		do {
			let authToken = KeychainService.retrieveToken()
			let profile = try await ProfileClient().getUserProfile(
				login: login,
				token: authToken
			)
			user = profile
			bio = profile?.bio ?? ""
			favoriteBandIDs = Set(
				profile?.favoriteBands?.compactMap {
					$0.id.map { String(describing: $0) }
				} ?? []
			)
			favoriteArtistIDs = Set(
				profile?.favoriteArtists?.compactMap {
					$0.id.map { String(describing: $0) }
				} ?? []
			)
			favoriteGenreIDs = Set(
				profile?.favoriteGenres?.compactMap {
					$0.id.map { String(describing: $0) }
				} ?? []
			)
			if profile == nil {
				print("no profile data")
			}
			if authToken != nil {
				let result = try await ReportClient().canReport(
					userId: Int32(user?.userId ?? 0),
					token: authToken ?? ""
				)
				canReport = result as! Bool
			}
		} catch {
			user = nil
		}
		isLoading = false
	}
}

#Preview {
	UserProfileView(login: "aleks")
}
