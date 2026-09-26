package com.example.client

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertTrue

@OptIn(ExperimentalTestApi::class)
class BackButtonTest {

    @Test
    fun displaysBackButton()=runComposeUiTest {
        setContent {
            BackButton()
        }

        onNodeWithContentDescription("back").assertIsDisplayed()
    }

    @Test
    fun invokesBackCallback()=runComposeUiTest {
        var backPressed=false
        setContent {
            BackButton(onBack={backPressed=true})
        }

        onNodeWithContentDescription("back").performClick()

        runOnIdle {
            assertTrue(backPressed)
        }
    }
}
