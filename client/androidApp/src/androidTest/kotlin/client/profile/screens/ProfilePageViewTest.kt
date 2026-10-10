package client.profile.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ProfilePageViewTest {

    @Test
    fun displaysProfileViewWithoutARequestedLogin() {
        runComposeUiTest {
            setContent {
                ProfilePageView(login="")
            }

            waitForIdle()

            onNodeWithText("Preview user").assertIsDisplayed()
            onNodeWithText("Bio").assertIsDisplayed()
        }
    }
}
