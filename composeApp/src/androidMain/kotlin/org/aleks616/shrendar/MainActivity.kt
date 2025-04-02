package org.aleks616.shrendar

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.tooling.preview.Preview

@SuppressLint("StaticFieldLeak")
lateinit var context:Context //todo: test if it actually leaks memory
fun getAndroidContext():Context=context

class MainActivity:ComponentActivity() {
    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)
        context=this.applicationContext
        setContent{
            App()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}