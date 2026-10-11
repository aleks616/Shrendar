package com.example.client.profile.screens

import androidx.compose.ui.test.*
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ProfilePageViewTest {

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
