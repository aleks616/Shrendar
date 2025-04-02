package org.aleks616.shrendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import java.awt.KeyboardFocusManager
import java.awt.Toolkit

class JVMPlatform:Platform {
    override val name:String="Java ${System.getProperty("java.version")}"
}

class JVMPlatformShort:PlatformShort {
    override val name:String="desktop"
}

actual fun getPlatformFull():Platform=JVMPlatform()

actual fun getPlatformShort():PlatformShort=JVMPlatformShort()

@Composable
actual fun getScreenWidth():Dp{
    val window=KeyboardFocusManager.getCurrentKeyboardFocusManager().activeWindow
    return (window?.width ?: Toolkit.getDefaultToolkit().screenSize.width).dp
}