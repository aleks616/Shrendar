package client.event

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import com.example.client.event.screens.EventDataView
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class EventDataViewTest {

    @Test
    fun rendersEventViewIndependently() {
        runComposeUiTest {
            setContent {
                EventDataView(eventId=2)
            }

            onRoot().assertIsDisplayed()
        }
    }
}
