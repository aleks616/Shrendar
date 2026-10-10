//
//  BandDataView.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 09/10/2026.
//

import SharedLogic
import SwiftUI

struct BandDataView: View {
	let bandId: Int

	@State private var band: BandWikiDto?
	@State private var isLoading = true
	@State private var selectedTab: BandTab = .members
	@State private var selectedMemberFilter: MemberFilter = .all
	@State private var selectedAlbumFilter: AlbumFilter = .all
	@State private var showingGenreInformation = false
	@State private var isFavorite = false

	enum BandTab: String, CaseIterable {
		case members
		case albums
		case similar

		var localizationKey: String {
			switch self {
			case .members:
				return "members"
			case .albums:
				return "albums"
			case .similar:
				return "similar_bands"
			}
		}
	}

	enum MemberFilter: String, CaseIterable {
		case all
		case current
		case past

		var localizationKey: String {
			switch self {
			case .all:
				return "all_members"
			case .current:
				return "current_members"
			case .past:
				return "past_members"
			}
		}
	}

	enum AlbumFilter: String, CaseIterable {
		case all
		case studio

		var localizationKey: String {
			switch self {
			case .all:
				return "all"
			case .studio:
				return "studio"
			}
		}
	}

	let isLoggedIn:Bool = {
		let token = KeychainService.retrieveToken()
		return token != nil && !token!.isEmpty
	}()
	
	func toggleBandFavorite() {
		Task { @MainActor in
			do {
				let result = try await ProfileClient().toggleFavoriteBand(
					bandId: Int32(truncating: bandId as NSNumber),
					token: KeychainService.retrieveToken()
				)
				guard result == "band_toggled" else {
					print("Failed to toggle band favorite status")
					return
				}
				isFavorite.toggle()
			} catch {
				print(error.localizedDescription)
			}
		}
	}
	
	func toggleArtistFavoriteAll(){
		Task{@MainActor in
			do {
				let result = try await ProfileClient().toggleFavoriteArtistAll(
					bandId: Int32(truncating: bandId as NSNumber),
					token: KeychainService.retrieveToken()
				)
				guard result == "artist_toggle" else {
					print("Failed to toggle artists favorite status")
					return
				}
				isFavorite.toggle()
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
				} else if let band {
					let bandMembers = band.bandMembers ?? []
					let currentMembers = bandMembers.filter { (member: BandsMembersWikiDto) in
						member.yearRole?.contains { (yearRole: Any) in
							String(describing: yearRole).contains("-)")
						} == true
					}
					let pastMembers = bandMembers.filter { (member: BandsMembersWikiDto) in
						member.yearRole?.contains { (yearRole: Any) in
							String(describing: yearRole).contains("-)")
						} != true
					}
					let showingBandMembers: [BandsMembersWikiDto] = {
						switch selectedMemberFilter {
						case .all:
							return bandMembers
						case .current:
							return currentMembers
						case .past:
							return pastMembers
						}
					}()
					let albums = band.albums ?? []
					let showingAlbums = selectedAlbumFilter == .studio
						? albums.filter { $0.type == "Studio" }
						: albums
					let statusColor: Color = {
						switch band.status {
						case "Active":
							return .green
						case "Disbanded":
							return .red
						default:
							return .orange
						}
					}()
					let memberColumns: [Table<BandsMembersWikiDto>.Column] = [
						(localize(key: "person_name"), {
							(member: BandsMembersWikiDto) -> String? in
							member.artistName
						}),
						(localize(key: "role"), {
							(member: BandsMembersWikiDto) -> String? in
							member.yearRole?
								.map { (yearRole: Any) in
									String(describing: yearRole).replacingOccurrences(
										of: "guitar",
										with: localize(key: "guitar")
									)
										.replacingOccurrences(
											of: "bass",
											with: localize(key: "bass")
										)
										.replacingOccurrences(
											of: "drums",
											with: localize(key: "drums")
										)
										.replacingOccurrences(
											of: "backing vocals",
											with: localize(key: "backing_vocals")
										)
										.replacingOccurrences(
											of: "vocals",
											with: localize(key: "vocals")
										)
								}
								.joined(separator: "\n")
						}),
					]
					let albumColumns: [Table<AlbumDto>.Column] = [
						(localize(key: "title"), {
							(album: AlbumDto) -> String? in album.title
						}),
						(localize(key: "release_date"), {
							(album: AlbumDto) -> String? in
							album.releaseDate.map { String(describing: $0) }
						}),
						(localize(key: "album_type"), {
							(album: AlbumDto) -> String? in album.type
						}),
						(localize(key: "main_genre"), {
							(album: AlbumDto) -> String? in album.genreName
						}),
					]
					let similarBandColumns: [Table<BandGenreDto>.Column] = [
						(localize(key: "thing_name"), {
							(similarBand: BandGenreDto) -> String? in similarBand.name
						}),
						(localize(key: "formed_year"), {
							(similarBand: BandGenreDto) -> String? in
							similarBand.formedYear.map { String(describing: $0) }
						}),
						(localize(key: "country"), {
							(similarBand: BandGenreDto) -> String? in
							similarBand.country.map { localize(key: $0) }
						}),
					]

					ScrollView {
						VStack(alignment: .leading, spacing: 16) {
							HStack{
								Text(band.name ?? "")
									.font(.largeTitle.bold())
								
								Button(action: toggleBandFavorite) {
									Image(systemName: isFavorite ? "star.fill" : "star")
										.foregroundStyle(isFavorite ? Color.accentColor : .secondary)
										.frame(width: 40, height: 40)
								}
								.disabled(!isLoggedIn)
								.buttonStyle(.plain)
								.accessibilityLabel(localize(key: "favorite"))
							}

							AnyView(
								HStack(alignment: .top, spacing: 20) {
								VStack(alignment: .leading, spacing: 8) {
									HStack(alignment: .firstTextBaseline, spacing: 4) {
										Text("\(localize(key: "country")):")
											.foregroundStyle(.secondary)
										Text(localize(key: band.country ?? "-"))
									}
									HStack(alignment: .firstTextBaseline, spacing: 4) {
										Text("\(localize(key: "status")):")
											.foregroundStyle(.secondary)
										Text(localize(key:(band.status?.lowercased())!))
											.foregroundStyle(statusColor)
									}
									HStack(alignment: .firstTextBaseline, spacing: 4) {
										Text("\(localize(key: "years_active")):")
											.foregroundStyle(.secondary)
										Text(
											"\(band.formedYear.map { String(describing: $0) } ?? "-") - \(band.disbandedYear.map { String(describing: $0) } ?? localize(key: "present"))"
										)
									}
								}
								.frame(maxWidth: .infinity, alignment: .leading)

								VStack(alignment: .leading, spacing: 8) {
									HStack(spacing: 4) {
										Text("\(localize(key: "top_genres")):")
											.foregroundStyle(.secondary)
										Button {
											showingGenreInformation = true
										} label: {
											Image(systemName: "info.circle")
										}
										.foregroundStyle(.secondary)
										.accessibilityLabel("More information")
									}
									ForEach(
										Array((band.computedGenres ?? []).enumerated()),
										id: \.offset
									) { _, genre in
										Text("-\(genre.name ?? "-")")
									}
								}
								.frame(maxWidth: .infinity, alignment: .leading)
							}
							)

							if let imageURL = band.imageUrl, let url = URL(string: imageURL) {
								AsyncImage(url: url) { phase in
									switch phase {
									case .success(let image):
										image
											.resizable()
											.scaledToFit()
									case .failure:
										Image(systemName: "photo")
											.resizable()
											.scaledToFit()
											.foregroundStyle(.secondary)
											.padding(40)
									default:
										ProgressView()
											.frame(width: 180, height: 180)
									}
								}
								.frame(maxWidth: 320, maxHeight: 320)
								.frame(maxWidth: .infinity)
								.clipShape(RoundedRectangle(cornerRadius: 12))
								.accessibilityLabel(band.name ?? "")
							}

							TranslatedDescription(description: band.description_ ?? "")

							Picker("", selection: $selectedTab) {
								ForEach(BandTab.allCases, id: \.self) { tab in
									Text(localize(key: tab.localizationKey))
										.tag(tab)
								}
							}
							.pickerStyle(.segmented)
							.frame(maxWidth: .infinity)

							switch selectedTab {
							case .members:
									HStack{
										HStack(spacing: 4) {
											ForEach(MemberFilter.allCases, id: \.self) { filter in
												Button {
													selectedMemberFilter = filter
												} label: {
													Text(localize(key: filter.localizationKey))
														.frame(minWidth: 64)
														.padding(.vertical, 6)
														.padding(.horizontal, 4)
												}
												.buttonStyle(.plain)
												.background(
													selectedMemberFilter == filter
													? Color.primary.opacity(0.18)
													: Color.clear
												)
												.clipShape(Capsule())
											}
										}
										.padding(4)
										.background(Color.secondary.opacity(0.08))
										.clipShape(Capsule())
										.fixedSize(horizontal: true, vertical: false)
										
										Spacer()
										Button(action:toggleArtistFavoriteAll){
											Text(localize(key: "favorite_all"))
										}.disabled(!isLoggedIn)
											.buttonStyle(.glass)
									}
									

								Table(
									columns: memberColumns,
									data: showingBandMembers,
									emptyText: localize(key: "no_band_members")
								)
								.frame(minWidth: UIScreen.main.bounds.width - 24)
								.frame(maxWidth: .infinity)

							case .albums:
								HStack(spacing: 4) {
									ForEach(AlbumFilter.allCases, id: \.self) { filter in
										Button {
											selectedAlbumFilter = filter
										} label: {
											Text(localize(key: filter.localizationKey))
												.frame(minWidth: 64)
												.padding(.vertical, 6)
												.padding(.horizontal, 4)
										}
										.buttonStyle(.plain)
										.background(
											selectedAlbumFilter == filter
												? Color.primary.opacity(0.18)
												: Color.clear
										)
										.clipShape(Capsule())
									}
								}
								.padding(4)
								.background(Color.secondary.opacity(0.08))
								.clipShape(Capsule())
								.fixedSize(horizontal: true, vertical: false)

								Table(
									columns: albumColumns,
									data: showingAlbums,
									emptyText: localize(key: "no_albums")
								)
								.frame(maxWidth: .infinity)

							case .similar:
								Table(
									columns: similarBandColumns,
									data: band.similar ?? [],
									emptyText: localize(key: "no_similar_bands")
								)
								.frame(maxWidth: .infinity)
							}
						}
						.padding(.horizontal, 12)
						.padding(.vertical, 16)
					}
				}
			}
			.navigationBarTitleDisplayMode(.inline)
			.task(id: bandId) {
				await loadBand()
			}
			.alert(
				localize(key: "top_genres"),
				isPresented: $showingGenreInformation
			) {
				Button(localize(key: "ok"), role: .cancel) {}
			} message: {
				Text(localize(key:"band_genre_info"))
			}
		}
	}

	private func loadBand() async {
		isLoading = true

		do {
			let token = KeychainService.retrieveToken()
			band = try await BandClient().getBandWikiPageDataById(id: Int32(bandId),token:token)
			isFavorite = band?.favorite==true
		} catch {
			band = nil
		}
		isLoading = false
	}
}
