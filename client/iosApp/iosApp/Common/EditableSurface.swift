//
//  EditableSurface.swift
//  iosApp
//
//  Created by Aleks Jankowiak on 06/10/2026.
//

import SwiftUI

struct EditableSurface: View {
    @Binding private var value: String
    @State private var draft: String
    @State private var isEditing = false
    @FocusState private var isTextEditorFocused: Bool
    private let onSave: ((String) -> Void)?

    init(value: Binding<String>, onSave: ((String) -> Void)? = nil) {
        self._value = value
        self._draft = State(initialValue: value.wrappedValue)
        self.onSave = onSave
    }

    var body: some View {
        Group {
            if isEditing {
                editorSurface
            }
            else {
                displaySurface
            }
        }
                .onChange(of: value) { _, newValue in
            if !isEditing {
                draft = newValue
            }
        }
                .onChange(of: isEditing) { _, editing in
            isTextEditorFocused = editing
        }
    }

    private var displaySurface: some View {
        ZStack(alignment: .bottomTrailing) {
            ScrollView {
                Text(draft)
                        .frame(maxWidth: .infinity, alignment: .leading)
                        .padding(10)
                        .padding(.trailing, 32)
            }

            actionButton(
                systemName: "pencil",
                accessibilityLabel: "Edit"
            ) {
                isEditing = true
            }
            .padding(8)
        }
        .surfaceStyle()
    }

    private var editorSurface: some View {
        ZStack(alignment: .bottomTrailing) {
            TextEditor(text: $draft)
                    .scrollContentBackground(.hidden)
                    .padding(6)
                    .padding(.trailing, 32)
                    .focused($isTextEditorFocused)

            actionButton(
                systemName: "checkmark",
                accessibilityLabel: "Save",
                action: save
            )
            .padding(8)
        }
        .surfaceStyle()
    }

    private func actionButton(systemName: String,
                              accessibilityLabel: String,
                              action: @escaping () -> Void) -> some View {
        Button(action: action) {
            Image(systemName: systemName)
                    .font(.body.weight(.semibold))
                    .imageScale(.large)
                    .scaleEffect(1.5)
                    .foregroundStyle(Color.accentColor)
                    .frame(width: 64, height: 64)
        }
                .buttonStyle(.plain)
                .accessibilityLabel(accessibilityLabel)
    }

    private func save() {
        value = draft
        isEditing = false
        onSave?(draft)
    }
}

private extension View {
    func surfaceStyle() -> some View {
        frame(maxWidth: .infinity, minHeight: 208, maxHeight: 208)
                .background(Color.secondary.opacity(0.08))
                .overlay(
            RoundedRectangle(cornerRadius: 12)
            .stroke(Color.accentColor, lineWidth: 1)
        )
                .clipShape(RoundedRectangle(cornerRadius: 12))
    }
}

#Preview {
    EditableSurface(value: .constant("Editable text"))
}
