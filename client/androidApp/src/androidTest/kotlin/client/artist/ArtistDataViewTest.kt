package client.artist

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import com.example.client.artist.ArtistDataView
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class ArtistDataViewTest {

    @Test
    fun rendersArtistViewIndependently() {
        runComposeUiTest {
            setContent {
                ArtistDataView(artistId=144)
            }

            onRoot().assertIsDisplayed()
        }
    }
}
