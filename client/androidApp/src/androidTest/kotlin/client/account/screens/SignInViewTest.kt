package com.example.client.account.screens

import androidx.compose.ui.test.*
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class SignInViewTest {
    @Test
    fun displaysSignInForm() {
        runComposeUiTest {
            setContent {
                SignInView()
            }

            onAllNodesWithText("Sign in").assertCountEquals(2)
            onNodeWithText("Login or e-mail address").assertIsDisplayed()
            onNodeWithText("Password").assertIsDisplayed()
            onNodeWithText("Reset password").assertIsDisplayed()
            onNode(hasText("Sign in") and hasClickAction()).assertIsNotEnabled()
        }
    }

    @Test
    fun enablesSubmissionAfterEnteringCredentials() {
        runComposeUiTest {
            setContent {
                SignInView()
            }

            onNodeWithText("Login or e-mail address").performTextInput("testuser")
            onNodeWithText("Password").performTextInput("Password1!")

            onNode(hasText("Sign in") and hasClickAction()).assertIsEnabled()
        }
    }

    @Test
    fun invokesForgotPasswordCallback() {
        runComposeUiTest {
            var forgotPasswordPressed=false
            setContent {
                SignInView(onForgotPassword={forgotPasswordPressed=true})
            }

            onNodeWithText("Reset password").performClick()

            runOnIdle {
                assertTrue(forgotPasswordPressed)
            }
        }
    }

    @Test
    fun invokesBackCallback() {
        runComposeUiTest {
            var backPressed=false
            setContent {
                SignInView(onBack={backPressed=true})
            }

            onNodeWithContentDescription("back").performClick()

            runOnIdle {
                assertTrue(backPressed)
            }
        }
    }
}
