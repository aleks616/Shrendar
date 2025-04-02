package org.aleks616.shrendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import kotlinx.browser.window

class WasmPlatform:Platform{
    override val name:String="Web with Kotlin/Wasm"
}

class WasmPlatformShort:PlatformShort{
    override val name:String="web"
}

actual fun getPlatformFull():Platform=WasmPlatform()

actual fun getPlatformShort():PlatformShort=WasmPlatformShort()
@Composable
actual fun getScreenWidth():Dp{
    return window.innerWidth.dp
}