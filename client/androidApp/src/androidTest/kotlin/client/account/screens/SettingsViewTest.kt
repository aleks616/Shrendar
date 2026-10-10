package client.account.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SettingsViewTest {

    @Test
    fun displaysSettingsViewWithoutAuthentication() {
        runComposeUiTest {
            setContent {
                SettingsView()
            }

            waitForIdle()

            onNodeWithText("Settings").assertIsDisplayed()
            onNodeWithText("Something went wrong. Try again later.").assertIsDisplayed()
        }
    }
}
