package client.album

import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.runComposeUiTest
import com.example.client.album.screens.AlbumDataView
import kotlin.test.Test

@OptIn(ExperimentalTestApi::class)
class AlbumDataViewTest {

    @Test
    fun rendersAlbumViewIndependently() {
        runComposeUiTest {
            setContent {
                AlbumDataView(albumId=255)
            }

            onRoot().assertIsDisplayed()
        }
    }
}
