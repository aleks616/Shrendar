package com.example.client.account.screens


import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class PasswordResetViewTest {
    @Test
    fun requestPasswordResetRequiresAnAccountKey() {
        runComposeUiTest {
            setContent {
                RequestPasswordResetView()
            }

            onNodeWithText("Forgot password?").assertIsDisplayed()
            onNodeWithText("Login or e-mail address").assertIsDisplayed()
            onNode(hasText("Reset password") and hasClickAction()).assertIsNotEnabled()

            onNodeWithText("Login or e-mail address").performTextInput("alice@example.com")

            onNode(hasText("Reset password") and hasClickAction()).assertIsEnabled()
        }
    }

    @Test
    fun createPasswordRequiresBothPasswordFields() {
        runComposeUiTest {
            setContent {
                PasswordResetView(
                    resetUrl="https://example.com/reset-password?code=123456&account=alice%40example.com"
                )
            }

            onNodeWithText("Create new password").assertIsDisplayed()
            onNodeWithText("Password").assertIsDisplayed()
            onNodeWithText("Repeat password").assertIsDisplayed()
            onNode(hasText("Change password") and hasClickAction()).assertIsNotEnabled()
        }
    }

    @Test
    fun createPasswordShowsAnErrorWhenPasswordsDoNotMatch() {
        runComposeUiTest {
            setContent {
                PasswordResetView(
                    resetUrl="https://example.com/reset-password?code=123456&account=alice%40example.com"
                )
            }

            onNodeWithText("Password").performTextInput("NewPassword1!")
            onNodeWithText("Repeat password").performTextInput("DifferentPassword1!")
            onNode(hasText("Change password") and hasClickAction()).performClick()

            onNodeWithText("Passwords don't match").assertIsDisplayed()
        }
    }

    @Test
    fun createPasswordShowsAnErrorForInvalidPassword() {
        runComposeUiTest {
            setContent {
                PasswordResetView(
                    resetUrl="https://example.com/reset-password?code=123456&account=alice%40example.com"
                )
            }

            onNodeWithText("Password").performTextInput("weak")
            onNodeWithText("Repeat password").performTextInput("weak")
            onNode(hasText("Change password") and hasClickAction()).performClick()

            onNodeWithText("one uppercase letter, one symbol",substring=true).assertIsDisplayed()
        }
    }

    @Test
    fun createPasswordShowsAnErrorWhenResetParametersAreMissing() {
        runComposeUiTest {
            setContent {
                PasswordResetView(resetUrl="https://example.com/reset-password")
            }

            onNodeWithText("Password").performTextInput("NewPassword1!")
            onNodeWithText("Repeat password").performTextInput("NewPassword1!")
            onNode(hasText("Change password") and hasClickAction()).performClick()

            onNodeWithText("Something went wrong. Try again later.").assertIsDisplayed()
        }
    }

    @Test
    fun invokesRequestPasswordResetBackCallback() {
        runComposeUiTest {
            var backPressed=false
            setContent {
                RequestPasswordResetView(onBack={backPressed=true})
            }

            onNodeWithContentDescription("back").performClick()

            runOnIdle {
                assertTrue(backPressed)
            }
        }
    }
}