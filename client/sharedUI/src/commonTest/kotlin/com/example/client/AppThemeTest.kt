package com.example.client

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class AppThemeTest {

    @Test
    fun providesMaterialThemeToContent() = runComposeUiTest {
        var primaryColor: Color? = null
        setContent {
            AppTheme {
                primaryColor = MaterialTheme.colorScheme.primary
                Text("Themed content")
            }
        }

        onNodeWithText("Themed content").assertIsDisplayed()
        runOnIdle {
            assertNotNull(primaryColor)
            assertTrue(primaryColor != Color.Unspecified)
        }
    }
}
