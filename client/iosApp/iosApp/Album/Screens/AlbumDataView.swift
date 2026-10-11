//
//  AlbumDataView.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 10/10/2026.
//

import SharedLogic
import SwiftUI

struct AlbumDataView: View {
	let albumId: Int

	@State private var album: AlbumWikiDto?
	@State private var isLoading = true

	var body: some View {
		NavigationStack {
			Group {
				if isLoading {
					ProgressView()
						.controlSize(.large)
						.frame(maxWidth: .infinity, maxHeight: .infinity)
				} else if let album {
					ScrollView {
						VStack(alignment: .leading, spacing: 16) {
							Text(album.albumName ?? "")
								.font(.largeTitle.bold())

							HStack(alignment: .top, spacing: 20) {
								VStack(alignment: .leading, spacing: 8) {
									HStack(alignment: .firstTextBaseline, spacing: 4) {
										Text("\(localize(key: "band")):")
											.foregroundStyle(.secondary)
										Text(album.band?.name ?? "-")
									}

									HStack(alignment: .firstTextBaseline, spacing: 4) {
										Text("\(localize(key: "release_date")):")
											.foregroundStyle(.secondary)
										Text(album.releaseDate.map { String(describing: $0) } ?? "-")
										Text(" \(localize(key: "anniversary_in"))")
											.foregroundStyle(.secondary)
										Text(album.daysTillAnniversary.map { String(describing: $0) } ?? "-")
										Text("\(localize(key: "days"))")
									}

									HStack(alignment: .firstTextBaseline, spacing: 4) {
										Text("\(localize(key: "years_since")):")
											.foregroundStyle(.secondary)
										Text(album.albumAge.map { String(describing: $0) } ?? "-")
									}

									HStack(alignment: .firstTextBaseline, spacing: 4) {
										Text("\(localize(key: "album_type")):")
											.foregroundStyle(.secondary)
										Text(album.type ?? "-")
									}

									HStack(alignment: .firstTextBaseline, spacing: 4) {
										Text("\(localize(key: "genre")):")
											.foregroundStyle(.secondary)
										Text(album.genre?.name ?? "-")
									}
								}
								.frame(maxWidth: .infinity, alignment: .leading)
							}

							if let artworkUrl = album.artworkUrl,
								let url = URL(string: artworkUrl)
							{
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
								.accessibilityLabel(album.albumName ?? "")
							}

							TranslatedDescription(
								description: album.description_ ?? ""
							)
						}
						.padding(.horizontal, 12)
						.padding(.vertical, 16)
					}
				}
			}
			.task(id: albumId) {
				await loadAlbum()
			}
		}
	}

	private func loadAlbum() async {
		isLoading = true

		do {
			album = try await AlbumClient().getAlbumWikiPageData(
				id: Int64(albumId)
			)
		} catch {
			album = nil
		}
		isLoading = false
	}
}
