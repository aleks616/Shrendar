package client.common

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import com.example.client.common.TranslatedDescription
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class TranslatedDescriptionTest {
    @Test
    fun displaysTheDescriptionAndTranslateAction() {
        runComposeUiTest {
            setContent {
                TranslatedDescription(description="Original description")
            }

            onNodeWithText("Original description").assertIsDisplayed()
            onNodeWithContentDescription("Translate description").assertIsDisplayed()
        }
    }
}
