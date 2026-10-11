package com.example.client.profile.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ProfilePageViewTest {
    @Test
    fun displaysProfilePreviewDataWithoutARequestedLogin() {
        runComposeUiTest {
            setContent {
                ProfilePageView(login="")
            }

            waitForIdle()

            onNodeWithText("Preview user").assertIsDisplayed()
            onNodeWithText("Bio").assertIsDisplayed()
            onNodeWithText("Favorite bands").assertIsDisplayed()
            onNodeWithText("Contributions").assertIsDisplayed()
            onNodeWithText("No favorite bands").assertIsDisplayed()
        }
    }

    @Test
    fun switchesBetweenFavoriteTabsInPreviewMode() {
        runComposeUiTest {
            setContent {
                ProfilePageView(login="")
            }

            waitForIdle()
            onNodeWithText("Favorite artists").performClick()
            onNodeWithText("No favorite artists").assertIsDisplayed()
            onNodeWithText("Favorite genres").performClick()
            onNodeWithText("No favorite genres").assertIsDisplayed()
        }
    }
}
