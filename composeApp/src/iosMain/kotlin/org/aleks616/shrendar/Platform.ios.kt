package org.aleks616.shrendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.cinterop.ExperimentalForeignApi
import kotlinx.cinterop.useContents
import platform.UIKit.UIDevice
import platform.UIKit.UIScreen

class IOSPlatform:Platform{
    override val name:String=UIDevice.currentDevice.systemName()+" "+UIDevice.currentDevice.systemVersion
}
class IOSPlatformShort:PlatformShort{
    override val name:String="ios"
}

actual fun getPlatformFull():Platform=IOSPlatform()
actual fun getPlatformShort():PlatformShort=IOSPlatformShort()


@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun getScreenWidth():Dp{
    return UIScreen.mainScreen.bounds.useContents{
        size.width
    }.toInt().dp
}