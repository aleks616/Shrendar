package com.example.client.account.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SignInViewTest {

    @Test
    fun displaysSignInForm() = runComposeUiTest {
        setContent {
            SignInView()
        }

        onAllNodesWithText("Sign in").assertCountEquals(2)
        onNodeWithText("Login or e-mail address").assertIsDisplayed()
        onNodeWithText("Password").assertIsDisplayed()
        onNode(hasText("Sign in") and hasClickAction()).assertIsNotEnabled()
    }

    @Test
    fun invokesBackCallback() = runComposeUiTest {
        var backPressed = false
        setContent {
            SignInView(onBack = { backPressed = true })
        }

        onNodeWithContentDescription("back").performClick()

        runOnIdle {
            assertTrue(backPressed)
        }
    }
}
