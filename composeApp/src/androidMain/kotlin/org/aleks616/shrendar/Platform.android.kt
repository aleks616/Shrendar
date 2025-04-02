package org.aleks616.shrendar

import android.os.Build
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

class AndroidPlatform:Platform{
    override val name:String="Android ${Build.VERSION.SDK_INT}"
}
class AndroidPlatformShort:PlatformShort{
    override val name:String="android"
}

actual fun getPlatformFull():Platform=AndroidPlatform()

actual fun getPlatformShort():PlatformShort=AndroidPlatformShort()
@Composable
actual fun getScreenWidth():Dp {
    val configuration=LocalConfiguration.current
    return configuration.screenWidthDp.dp
}