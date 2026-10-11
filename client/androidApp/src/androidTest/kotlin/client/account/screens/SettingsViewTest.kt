package com.example.client.account.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
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
            onNodeWithText("Username").assertIsDisplayed()
            onNodeWithText("E-mail address").assertIsDisplayed()
            onNodeWithText("Birthdate").assertIsDisplayed()
            onNodeWithText("Save changes").assertIsDisplayed()
            onNodeWithText("Change password").assertIsDisplayed()
            onNodeWithText("Delete account").assertIsDisplayed()
            onNodeWithText("Something went wrong. Try again later.").assertIsDisplayed()
        }
    }

    @Test
    fun disablesSavingWhenTheUsernameIsTooShort() {
        runComposeUiTest {
            setContent {
                SettingsView()
            }

            waitForIdle()
            onNodeWithText("Username").performTextInput("abc")

            onNodeWithText("Username length must be between 4 and 25 characters").assertIsDisplayed()
            onNode(hasText("Save changes") and hasClickAction()).assertIsNotEnabled()
        }
    }
}
