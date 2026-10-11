package client.account.screens

import androidx.compose.ui.test.*
import com.example.client.account.screens.RequestPasswordResetView
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
