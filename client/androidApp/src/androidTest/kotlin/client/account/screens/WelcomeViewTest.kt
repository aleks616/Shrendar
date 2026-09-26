package com.example.client.account.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class WelcomeViewTest {

    @Test
    fun displaysWelcomeContent()=runComposeUiTest {
        setContent {
            WelcomeView()
        }

        onNodeWithText("Welcome").assertIsDisplayed()
        onNodeWithText("Create an account or sign in to continue").assertIsDisplayed()
        onNodeWithText("Create account").assertIsDisplayed()
        onNodeWithText("Already have an account").assertIsDisplayed()
        onNodeWithText("Continue as a").assertIsDisplayed()
        onNodeWithText("guest?").assertIsDisplayed()
    }

    @Test
    fun invokesRegisterCallback()=runComposeUiTest {
        var registerPressed=false
        setContent {
            WelcomeView(registerScreen={registerPressed=true})
        }

        onNodeWithText("Create account").performClick()

        runOnIdle {
            assertTrue(registerPressed)
        }
    }

    @Test
    fun invokesSignInCallback()=runComposeUiTest {
        var signInPressed=false
        setContent {
            WelcomeView(signInScreen={signInPressed=true})
        }

        onNode(hasText("Already have an account") and hasClickAction()).performClick()

        runOnIdle {
            assertTrue(signInPressed)
        }
    }
}
