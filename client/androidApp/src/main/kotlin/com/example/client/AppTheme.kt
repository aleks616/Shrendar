package com.example.client

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColors=lightColorScheme(
    primary=Color(0xFF5F55EC),
    onPrimary=Color.White,
    primaryContainer=Color(0xFFE3E0FF),
    onPrimaryContainer=Color(0xFF5F55EC),
    secondary=(Color(0xFFEFEFF0)),
    onSecondary=Color(0xFF71717A),
    background=Color(0xFFF7F7F7),
    onBackground=Color(0xFF202020),
    surface=Color.White,
    onSurface=Color(0xFF141414),
    surfaceVariant=Color(0xFFE7E7E7),
    onSurfaceVariant=Color(0xFF666666),
    outline=Color(0xFF8A8A8A),
    outlineVariant=Color(0xFFD0D0D0),
    error=Color(0xFFB00020),
    onError=Color.White,
)

private val DarkColors=darkColorScheme(
    primary=Color(0xFF6F7BF7),
    onPrimary=Color.White,
    primaryContainer=Color(0xFF3A3868),
    onPrimaryContainer=Color(0xFF6F7BF7),
    secondary=Color(0xFF232325),
    onSecondary=Color(0XFF9F9FA9),
    background=Color(0xFF121212),
    onBackground=Color(0xFFF5F5F5),
    surface=Color(0xFF1E1E1E),
    onSurface=Color(0xFFF5F5F5),
    surfaceVariant=Color(0xFF303030),
    onSurfaceVariant=Color(0xFFBDBDBD),
    outline=Color(0xFF8A8A8A),
    outlineVariant=Color(0xFF4A4A4A),
    error=Color(0xFFFF003C),
    onError=Color.White,
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
