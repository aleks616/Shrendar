package com.example.client

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.tooling.preview.Preview
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.kiwi.navigationcompose.typed.composable
import com.kiwi.navigationcompose.typed.createRoutePattern
import com.example.client.account.screens.WelcomeView
import com.example.client.account.screens.RegisterView
import com.example.client.account.screens.RequestPasswordResetView
import com.example.client.account.screens.SignInView
import com.example.client.account.screens.PasswordResetView
import com.example.client.account.screens.SettingsView
import com.example.client.band.BandDataView
import com.example.client.profile.screens.ProfilePageView
import kotlinx.serialization.ExperimentalSerializationApi

class MainActivity:ComponentActivity() {
    private val resetUrl=mutableStateOf<String?>(null)

    @OptIn(ExperimentalSerializationApi::class)
    override fun onCreate(savedInstanceState:Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)
        resetUrl.value=intent?.data?.toString()

        setContent {
            val navController=rememberNavController()
            LaunchedEffect(resetUrl.value) {
                if(resetUrl.value!=null) {
                    navController.navigate(createRoutePattern<Destinations.PasswordReset>()) {
                        launchSingleTop=true
                    }
                }
            }
            NavHost(
                navController=navController,
                startDestination=createRoutePattern<Destinations.BandDataView>(),
            ) {
                composable<Destinations.Welcome> {
                    WelcomeView(
                        registerScreen={
                            navController.navigate(createRoutePattern<Destinations.Register>())
                        },
                        signInScreen={
                            navController.navigate(createRoutePattern<Destinations.SignIn>())
                        }
                    )
                }
                composable<Destinations.Register> {
                    RegisterView(
                        onBack={navController.popBackStack()},
                        signInScreen={
                            navController.navigate(createRoutePattern<Destinations.SignIn>())
                        }
                    )
                }
                composable<Destinations.SignIn> {
                    SignInView(
                        onBack={navController.popBackStack()},
                        onForgotPassword={
                            navController.navigate(createRoutePattern<Destinations.RequestPasswordReset>())
                        }
                    )
                }
                composable<Destinations.RequestPasswordReset> {
                    RequestPasswordResetView(onBack={navController.popBackStack()})
                }
                composable<Destinations.PasswordReset> {
                    PasswordResetView(
                        resetUrl=resetUrl.value.orEmpty(),
                        onBack={navController.popBackStack()},
                    )
                }
                composable<Destinations.Profile> {
                    ProfilePageView()
                }
                composable<Destinations.Settings> {
                    SettingsView(
                        onBack={navController.popBackStack()},
                        onChangePassword={
                            navController.navigate(
                                createRoutePattern<Destinations.RequestPasswordReset>()
                            )
                        },
                    )
                }
                composable<Destinations.BandDataView> {
                    BandDataView()
                }

            }
        }
    }

    override fun onNewIntent(intent:Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        resetUrl.value=intent.data?.toString()
    }

}

@Preview
@Composable
fun AppAndroidPreview() {
    WelcomeView()
}