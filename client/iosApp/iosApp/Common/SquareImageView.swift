//
//  ImageView.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 24/10/2026.
//

import SwiftUI

struct SquareImageView: View {
	let urlParam: String?

	var body: some View{
		if let imageURL = urlParam,
			let url = URL(string: imageURL){
			AsyncImage(url: url) { image in
				image
					.resizable()
					.aspectRatio(1, contentMode: .fill)
					.clipShape(Circle())
				
			} placeholder: {
				ProgressView()
			}
			.frame(maxWidth: 48)
			.accessibilityLabel("photo")
		} else {
			Image(systemName: "photo")
				.resizable()
				.aspectRatio(1, contentMode: .fill)
				.clipShape(Circle())
				.accessibilityLabel("no image")
				.frame(maxWidth: 48)
		}

	}
	
}
