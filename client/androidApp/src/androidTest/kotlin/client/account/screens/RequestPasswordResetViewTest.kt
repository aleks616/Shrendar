package client.account.screens

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.onNode
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class RequestPasswordResetViewTest {

    @Test
    fun displaysRequestPasswordResetForm() {
        runComposeUiTest {
            setContent {
                RequestPasswordResetView()
            }

            onNodeWithText("Forgot password?").assertIsDisplayed()
            onNodeWithText("Login or e-mail address").assertIsDisplayed()
            onNode(hasText("Reset password") and hasClickAction()).assertIsNotEnabled()
        }
    }

    @Test
    fun enablesSubmissionAfterAccountKeyIsEntered() {
        runComposeUiTest {
            setContent {
                RequestPasswordResetView()
            }

            onNodeWithText("Login or e-mail address").performTextInput("alice@example.com")

            onNode(hasText("Reset password") and hasClickAction()).assertIsEnabled()
        }
    }
}
