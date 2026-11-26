//
//  BandMembersAddView.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 25/11/2026.
//

import SharedLogic
import SwiftUI

struct MemberPreview: Equatable {
	let artistId: Int64
	let name: String
	let nickname: String
	let joinedYear: Int
	let leftYear: Int?
	let role: String
}

struct BandMembersAddView: View {
	let bandId: Int
	@State private var band: BandWikiDto? = nil
	@State private var selectedArtist: ArtistMemberAddDto? = nil
	@State private var nickname: String = ""
	@State private var joinedYear: Int? = nil
	@State private var leftYear: Int? = nil
	@State private var role: String = ""
	@State private var members: [MemberPreview] = []
	@State private var isDuplicate: Bool = false
	@State private var resultText: String = ""

	var currentYear: Int {
		Calendar.current.component(.year, from: Foundation.Date())
	}

	var joinedYearPlaceholder: String {
		guard let formedYear = band?.formedYear?.intValue else { return "" }
		return "\(formedYear)"
	}

	var leftYearPlaceholder: String {
		guard let joinedYear else { return "" }
		return "\(joinedYear)"
	}

	var memberColumns: [Table<MemberPreview>.Column] {
		[
			(title: "name", value: { $0.name }),
			(title: "nickname", value: { $0.nickname }),
			(title: "joined", value: { String($0.joinedYear) }),
			(title: "left", value: { $0.leftYear.map { String($0) } }),
			(title: "role", value: { $0.role }),
			(title: "", value: { _ in nil })
		]
	}

	var joinedYearError: String? {
		guard let joinedYear else { return nil }
		if joinedYear > currentYear {
			return localize(key: "joined_future")
		}
		if let formedYear = band?.formedYear?.intValue, joinedYear < Int(formedYear) {
			return "Band can't be joined before it was formed"
		}
		if let disbandedYear = band?.disbandedYear?.intValue, joinedYear > Int(disbandedYear) {
			return "Band can't be joined after it was disbanded"
		}
		if let birthYear = selectedArtist?.birthYear?.intValue, joinedYear - Int(birthYear) < 10 {
			return localize(key: "artist_too_young_joining")
		}
		if let deathYear = selectedArtist?.deathYear?.intValue, joinedYear > Int(deathYear) {
			return localize(key: "artist_dead_joining")
		}
		return nil
	}

	var leftYearError: String? {
		guard let leftYear else { return nil }
		if leftYear > currentYear {
			return localize(key: "left_future")
		}
		if let joinedYear, leftYear < joinedYear {
			return localize(key: "left_before_joined")
		}
		if let deathYear = selectedArtist?.deathYear?.intValue, leftYear > Int(deathYear) {
			return localize(key: "artist_leave_when_dead")
		}
		return nil
	}

	var canAddToPreview: Bool {
		selectedArtist != nil &&
		joinedYear != nil &&
		joinedYearError == nil &&
		leftYearError == nil &&
		!role.trimmingCharacters(in: .whitespaces).isEmpty &&
		role.count <= 20
	}

	var body: some View {
		NavigationStack {
			VStack(spacing: 4) {
				VStack(spacing: 6) {
					Text(localize(key: "add_members"))
						.font(.system(size: 28.0, weight: .bold))

					if let country = band?.country {
						Text("\(localize(key: "adding_to")) \(band?.name ?? "") (\(localize(key: country)))")
							.font(.system(size: 16.0))
							.foregroundStyle(.secondary)
					}
				}

				Form {
					ArtistPicker(selectedArtist: $selectedArtist)

					VStack(alignment: .leading, spacing: 6) {
						Text(localize(key: "nickname"))
						TextField(localize(key: "nickname"), text: $nickname)
							.padding(12)
							.background(Color.secondary.opacity(0.15))
							.cornerRadius(10)
					}

					VStack(alignment: .leading, spacing: 6) {
						Text(localize(key: "joined_year"))
						TextField(joinedYearPlaceholder, value: $joinedYear, format: .number.grouping(.never))
							.keyboardType(.numberPad)
							.padding(12)
							.background(Color.secondary.opacity(0.15))
							.cornerRadius(10)
						if let joinedYearError {
							Text(joinedYearError)
								.font(.system(size: 16.0))
								.foregroundStyle(.red)
						}
					}

					VStack(alignment: .leading, spacing: 6) {
						Text(localize(key: "left_year"))
						TextField(leftYearPlaceholder, value: $leftYear, format: .number.grouping(.never))
							.keyboardType(.numberPad)
							.padding(12)
							.background(Color.secondary.opacity(0.15))
							.cornerRadius(10)
						if let leftYearError {
							Text(leftYearError)
								.font(.system(size: 16.0))
								.foregroundStyle(.red)
						}
					}

					VStack(alignment: .leading, spacing: 6) {
						Text(localize(key: "role"))
						TextField(localize(key: "role"), text: $role)
							.padding(12)
							.background(Color.secondary.opacity(0.15))
							.cornerRadius(10)
						if role.count > 20 {
							Text(localize(key: "role_too_long"))
								.font(.system(size: 16.0))
								.foregroundStyle(.red)
						}
					}

					Section {
						VStack {
							if isDuplicate {
								Text("duplicate error, entry not added")
									.font(.system(size: 16.0))
									.foregroundStyle(.red)
							}

							Button(action: addMemberToPreview) {
								Text(localize(key: "add_to_preview")).frame(width: 140)
							}
							.buttonStyle(.plain)
							.disabled(!canAddToPreview)
							.padding()
							.background(Color.accentColor)
							.foregroundColor(.white)
							.glassEffect()
							.cornerRadius(25)
						}
						.frame(maxWidth: .infinity)
					}
					.listRowBackground(Color.clear)

					if !members.isEmpty {
						Table(columns: memberColumns, data: members, emptyText: localize(key: "no_results")) { member, index in
							if index == memberColumns.count - 1 {
								Button {
									removeMember(member)
								} label: {
									Image(systemName: "trash")
										.font(.system(size: 18.0))
								}
								.buttonStyle(.plain)
							} else {
								Text(memberColumns[index].value(member) ?? "-")
									.font(.system(size: 18.0))
							}
						}
					}
					Section {
						VStack(spacing: 4) {
							Button(action: submit) {
								Text(localize(key: "add_all")).frame(width: 140)
							}
							.buttonStyle(.plain)
							.disabled(members.isEmpty)
							.padding()
							.background(Color.accentColor)
							.foregroundColor(.white)
							.glassEffect()
							.cornerRadius(25)

							if !resultText.isEmpty {
								Text(localize(key: resultText))
							}
						}
						.frame(maxWidth: .infinity)
						.padding(.bottom, 8)
					}
					.listRowBackground(Color.clear)
				}
				.listSectionSpacing(.compact)
				.onTapGesture {
					UIApplication.shared.sendAction(#selector(UIResponder.resignFirstResponder), to: nil, from: nil, for: nil)
				}
			}
			.task {
				await loadBand()
			}
		}
	}

	func loadBand() async {
		if let result = try? await BandClient().getBandWikiPageDataById(id: Int32(bandId), token: KeychainService.retrieveToken()) {
			band = result
		}
	}

	func addMemberToPreview() {
		guard let selectedArtist, let joinedYear else { return }

		let member = MemberPreview(
			artistId: selectedArtist.id,
			name: selectedArtist.name ?? "",
			nickname: nickname,
			joinedYear: joinedYear,
			leftYear: leftYear,
			role: role
		)

		isDuplicate = members.contains {
			$0.artistId == member.artistId &&
			$0.role == member.role &&
			($0.joinedYear == member.joinedYear || $0.leftYear == member.leftYear)
		}
		guard !isDuplicate else { return }

		members.append(member)
	}

	func removeMember(_ member: MemberPreview) {
		members.removeAll { $0 == member }
	}

	func submit() {
		let token = KeychainService.retrieveToken() ?? ""

		Task {
			do {
				let data: [ArtistBandAddDto] = members.map { member in
					ArtistBandAddDto(
						id: nil,
						bandId: KotlinInt(int: Int32(bandId)),
						artistId: KotlinLong(value: Int64(Int(member.artistId))),
						nickname: member.nickname,
						role: member.role,
						joinedYear: KotlinInt(int: Int32(member.joinedYear)),
						leftYear: member.leftYear.map { KotlinInt(int: Int32($0)) }
					)
				}
				resultText = try await BandClient().addBandMembers(members: data, token: token)
			} catch {
				resultText = "something_wrong"
			}
		}
	}
}

#Preview {
	BandMembersAddView(bandId: 21)
}
