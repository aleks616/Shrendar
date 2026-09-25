package com.example.client

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors=lightColorScheme(
    primary=Color(0xFF5F55EC),
    surfaceVariant=Color(0xFF717171),
)

private val DarkColors=darkColorScheme(
    primary=Color(0xFF6F7BF7),
    surfaceVariant=Color(0xFF717171),
)

@Composable
fun AppTheme(
    content:@Composable ()->Unit,
) {
    MaterialTheme(
        colorScheme=if(isSystemInDarkTheme()) DarkColors else LightColors,
        content=content,
    )
}
