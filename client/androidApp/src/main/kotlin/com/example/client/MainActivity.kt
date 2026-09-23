package com.example.client

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
            RegisterView()
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    RegisterView()
}