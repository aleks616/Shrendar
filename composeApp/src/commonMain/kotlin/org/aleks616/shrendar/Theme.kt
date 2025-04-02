package org.aleks616.shrendar

import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

@Composable
fun AppTheme(content: @Composable () -> Unit){
    MaterialTheme(
        colors=MaterialTheme.colors.copy(
            background=Color(0xFF252525), //background
            primaryVariant=Color(0xFF303030), //header etc
            onBackground=Color(0xFFFFFFFF), //normal text
            primary=Color(0xFF4D4D4D), //button
            onPrimary=Color(0xFFFFFFFF), //button text
            surface=Color(0xFF4D4D4D), //like button but not sure,
            onSurface=Color(0xFFFFFFFF), //like color on button but not sure
            onError=Color(0xFFBF0B0B) //error text color
        ),content={
            Surface(color=MaterialTheme.colors.background){
                content()
            }
        }
    )
}