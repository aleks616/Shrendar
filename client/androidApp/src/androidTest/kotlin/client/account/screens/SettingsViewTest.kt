package com.example.client.account.screens

import android.content.Context
import androidx.compose.ui.test.*
import androidx.test.platform.app.InstrumentationRegistry
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class SettingsViewTest {

    @Test
    fun disablesSavingWhenTheUsernameIsTooShort() {
        InstrumentationRegistry.getInstrumentation().targetContext
            .getSharedPreferences("authToken", Context.MODE_PRIVATE)
            .edit()
            .putString("authToken", "test-auth-token")
            .commit()

        runComposeUiTest {
            setContent {
                SettingsView()
            }

            waitForIdle()
            onNodeWithText("Username").assertIsDisplayed()
            onNodeWithText("Username").performTextInput("a")

            onNodeWithText("Username length must be between 4 and 25 characters").assertIsDisplayed()
            onNode(hasText("Save changes") and hasClickAction()).assertIsNotEnabled()
        }
    }
}
