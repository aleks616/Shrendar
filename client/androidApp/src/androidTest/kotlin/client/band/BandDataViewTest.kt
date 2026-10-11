package client.band

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import com.example.client.band.BandDataView
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class BandDataViewTest {

    @Test
    fun rendersBandViewIndependently() {
        runComposeUiTest {
            setContent {
                BandDataView(bandId=21)
            }

            onRoot().assertIsDisplayed()
        }
    }
}
