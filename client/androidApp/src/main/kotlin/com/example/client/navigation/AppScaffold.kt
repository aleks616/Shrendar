package com.example.client.navigation

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.client.account.components.LoginModel
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import androidx.compose.runtime.collectAsState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold() {
    val navController=rememberNavController()
    val navigator=navController.rememberDestinationsNavigator()

    val navBackStackEntry=navController.currentBackStackEntryAsState().value
    val bottomBarState=rememberSaveable {mutableStateOf(false)}
    val topBarState=rememberSaveable {mutableStateOf(true)}

    val route=navBackStackEntry?.destination?.route.orEmpty().substringBefore("?")
    when(route) {
        "welcome_view"-> {
            bottomBarState.value=false
            topBarState.value=false
        }
        "homepage_data_view"-> {
            bottomBarState.value=true
            topBarState.value=false
        }
        "search_results_view"-> {
            bottomBarState.value=true
            topBarState.value=false
        }
        "profile_page_view"-> {
            bottomBarState.value=true
            topBarState.value=false
        }
        "settings_view"-> {
            bottomBarState.value=true
            topBarState.value=false
        }
        "sign_in_view"-> {
            bottomBarState.value=true
            topBarState.value=false
        }
        "register_view"-> {
            bottomBarState.value=true
            topBarState.value=false
        }
        else-> {
            bottomBarState.value=false
            topBarState.value=true
        }
    }

    val loggedIn=LocalContext.current.getSharedPreferences("authToken",Context.MODE_PRIVATE).getString("authToken",null)!=null
    if(!LoginModel.isLoggedIn.collectAsState().value&&loggedIn){
        LoginModel.setLoggedIn(true)
    }

    Scaffold(
        topBar={
            AnimatedVisibility(
                visible=topBarState.value,
                enter=slideInVertically(initialOffsetY={-it}),
                exit=slideOutVertically(targetOffsetY={-it})
            ) {
                TopAppBar(
                    title={},
                    navigationIcon={
                        BackButton {navigator.popBackStack()}
                    },
                )
            }
        },
        bottomBar={
            AnimatedVisibility(
                visible=bottomBarState.value,
                enter=slideInVertically(initialOffsetY={it}),
                exit=slideOutVertically(targetOffsetY={it})
            ) {
                BottomBar(navController)
            }
        }
    ) {innerPadding->
        Box(modifier=Modifier.padding(innerPadding)) {
            DestinationsNavHost(
                navController=navController,
                navGraph=NavGraphs.root
            )
        }
    }
}