package org.aleks616.shrendar

import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.Dp

interface Platform{
    val name:String
}
interface PlatformShort{
    val name:String
}

expect fun getPlatformFull():Platform
expect fun getPlatformShort():PlatformShort
@Composable
expect fun getScreenWidth():Dp