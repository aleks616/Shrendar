package org.aleks616.shrendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


class JsPlatform:Platform{
    override val name:String=js("window.navigator.userAgent") as String
}

class JsPlatformShort:PlatformShort{
    override val name:String="js"
}

actual fun getPlatformFull():Platform=JsPlatform()

actual fun getPlatformShort():PlatformShort=JsPlatformShort()

@Composable
actual fun getScreenWidth():Dp{
    return js("window.innerWidth").unsafeCast<Double>().dp
}