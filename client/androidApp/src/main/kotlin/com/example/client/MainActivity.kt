package com.example.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.client.account.screens.WelcomeView

class MainActivity:ComponentActivity() {
    override fun onCreate(savedInstanceState:Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        /*CoroutineScope(Dispatchers.Main).launch {
            try {
                Log.d("GenreApi", GenreApi().getAll().toString())
            } catch (error: Exception) {
                Log.e("GenreApi", "Unable to fetch genres", error)
            }
        }*/

        setContent {
            WelcomeView()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    WelcomeView()
}