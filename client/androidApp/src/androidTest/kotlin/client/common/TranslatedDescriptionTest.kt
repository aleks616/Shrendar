package client.common

import androidx.compose.ui.test.*
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
