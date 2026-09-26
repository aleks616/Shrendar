package com.example.client.account.components

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class LabelledDividerTest {

    @Test
    fun displaysDividerLabel() = runComposeUiTest {
        setContent {
            LabelledDivider(text = "Continue")
        }

        onNodeWithText("Continue").assertIsDisplayed()
    }
}
