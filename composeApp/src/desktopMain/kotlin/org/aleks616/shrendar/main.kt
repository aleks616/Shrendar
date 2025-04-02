package org.aleks616.shrendar

import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application

fun main() = application {
    Window(
        onCloseRequest = ::exitApplication,
        title = "Shrendar",
    ) {
        AppTheme {
            App()
        }

    }
}