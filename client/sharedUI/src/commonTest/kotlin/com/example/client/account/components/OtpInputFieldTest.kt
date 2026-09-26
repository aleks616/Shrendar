package com.example.client.account.components

import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.runComposeUiTest
import kotlin.test.Test
import kotlin.test.assertEquals

@OptIn(ExperimentalTestApi::class)
class OtpInputFieldTest {

    @Test
    fun rendersOneInputBoxPerRequestedDigit() = runComposeUiTest {
        val otp = mutableStateOf("")
        setContent {
            OtpInputField(otp = otp, count = 6)
        }

        repeat(6) { index ->
            onNodeWithTag("otpBox$index").assertIsDisplayed()
        }
    }

    @Test
    fun updatesOtpStateWhenDigitIsEntered() = runComposeUiTest {
        val otp = mutableStateOf("")
        setContent {
            OtpInputField(otp = otp, count = 4)
        }

        onNodeWithTag("otpBox0").performTextInput("7")
        waitForIdle()

        assertEquals("7", otp.value)
    }
}
