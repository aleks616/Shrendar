package com.example.client.account.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class RegisterViewTest {

    @Test
    fun displaysRegistrationForm()=runComposeUiTest {
        setContent {
            RegisterView()
        }

        onNodeWithText("Create account").assertIsDisplayed()
        onNodeWithText("Sign Up to Continue").assertIsDisplayed()
        onNodeWithText("E-mail address").assertIsDisplayed()
        onNodeWithText("Login").assertIsDisplayed()
        onNodeWithText("Password").assertIsDisplayed()
        onNodeWithText("Repeat password").assertIsDisplayed()
        onNodeWithText("Sign Up").assertIsDisplayed()
        onNodeWithText("or").assertIsDisplayed()
    }

    @Test
    fun invokesBackCallback()=runComposeUiTest {
        var backPressed=false
        setContent {
            RegisterView(onBack={backPressed=true})
        }

        onNodeWithContentDescription("back").performClick()

        runOnIdle {
            assertTrue(backPressed)
        }
    }

    @Test
    fun invokesSignInCallback()=runComposeUiTest {
        var signInPressed=false
        setContent {
            RegisterView(signInScreen={signInPressed=true})
        }

        onNodeWithText("Sign in").performClick()

        runOnIdle {
            assertTrue(signInPressed)
        }
    }
}
