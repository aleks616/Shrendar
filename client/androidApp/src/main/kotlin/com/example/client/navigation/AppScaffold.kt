package com.example.client.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.client.BackButton
import com.ramcosta.composedestinations.DestinationsNavHost
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppScaffold(){
    val navController=rememberNavController()
    val navigator=navController.rememberDestinationsNavigator()

    val navBackStackEntry=navController.currentBackStackEntryAsState().value
    val bottomBarState=rememberSaveable { mutableStateOf(true) }
    val topBarState=rememberSaveable { mutableStateOf(true) }

    when(navBackStackEntry?.destination?.route) {
        "welcome_view"-> {
            bottomBarState.value=false
            topBarState.value=false
        }

        else-> {
            bottomBarState.value=true
            topBarState.value=true
        }
    }

    Scaffold(
        topBar={
            if(topBarState.value){
                TopAppBar(
                    title={},
                    navigationIcon={
                        BackButton {navigator.popBackStack()}
                    },
                )
            }
        },
        bottomBar={
            if(bottomBarState.value){
                BottomBar(navController)
            }
        }
    ){innerPadding->
        Box(modifier=Modifier.padding(innerPadding)){
            DestinationsNavHost(
                navController=navController,
                navGraph=NavGraphs.root
            )
        }
    }
}