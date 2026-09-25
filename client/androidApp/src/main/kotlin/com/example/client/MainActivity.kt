package com.example.client

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.example.client.account.screens.WelcomeView
import com.example.client.account.screens.RegisterView
import com.example.client.account.screens.SignInView
import kotlinx.serialization.ExperimentalSerializationApi

class MainActivity:ComponentActivity() {
    @OptIn(ExperimentalSerializationApi::class)
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
            val navController=rememberNavController()
            NavHost(
                navController=navController,
                startDestination=createRoutePattern<Destinations.Welcome>(),
            ) {
                composable<Destinations.Welcome> {
                    WelcomeView(
                        registerScreen={
                            navController.navigate(createRoutePattern<Destinations.Register>())
                        },
                        signInScreen={
                            navController.navigate(createRoutePattern<Destinations.SignIn>())
                        },
                    )
                }
                composable<Destinations.Register> {
                    RegisterView(
                        onBack={navController.popBackStack()},
                        signInScreen={navController.navigate(createRoutePattern<Destinations.SignIn>())})
                }
                composable<Destinations.SignIn> {
                    SignInView(onBack={navController.popBackStack()})
                }
            }
        }
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    WelcomeView()
}