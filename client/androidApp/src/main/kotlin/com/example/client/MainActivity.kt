package com.example.client

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.mutableStateOf
import com.example.client.navigation.AppScaffold

class MainActivity:ComponentActivity() {
    private val resetUrl=mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState:Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme {
                AppScaffold()
            }
        }
    }

    override fun onNewIntent(intent:Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        resetUrl.value=intent.data?.toString()
    }

}