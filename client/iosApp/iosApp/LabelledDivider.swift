//
//  LabelledDivider.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 16/09/2026.
//


// Source - https://stackoverflow.com/a/57576694
// Posted by Sajjon, modified by community. See post 'Timeline' for change history
// Retrieved 2026-09-16, License - CC BY-SA 4.0
import SwiftUI
struct LabelledDivider: View {

    let label: String
    let horizontalPadding: CGFloat
    let color: Color

    init(label: String, horizontalPadding: CGFloat = 20, color: Color = .gray) {
        self.label = label
        self.horizontalPadding = horizontalPadding
        self.color = color
    }

    var body: some View {
        HStack {
            line
            Text(label).foregroundColor(color)
            line
        }
    }

    var line: some View {
        VStack { Divider().background(color) }.padding(horizontalPadding)
    }
}
