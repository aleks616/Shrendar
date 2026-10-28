package com.example.client.navigation

import android.content.Context
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.navigation.NavController
import androidx.navigation.compose.currentBackStackEntryAsState
import com.example.client.LocalText
import com.example.client.account.components.LoginModel
import com.ramcosta.composedestinations.generated.NavGraphs
import com.ramcosta.composedestinations.generated.destinations.HomepageDataViewDestination
import com.ramcosta.composedestinations.generated.destinations.ProfilePageViewDestination
import com.ramcosta.composedestinations.generated.destinations.SearchResultsViewDestination
import com.ramcosta.composedestinations.generated.destinations.SettingsViewDestination
import com.ramcosta.composedestinations.generated.destinations.WelcomeViewDestination
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import com.ramcosta.composedestinations.spec.DestinationSpec
import com.ramcosta.composedestinations.spec.Direction
import com.ramcosta.composedestinations.spec.DirectionDestinationSpec
import com.ramcosta.composedestinations.utils.currentDestinationAsState
import com.ramcosta.composedestinations.utils.rememberDestinationsNavigator
import com.ramcosta.composedestinations.utils.startDestination
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import androidx.compose.runtime.collectAsState
import com.ramcosta.composedestinations.generated.destinations.SignInViewDestination


@Composable
fun BottomBar(
    navController:NavController
){
    data class BottomBarDestination(
        val destination:DestinationSpec,
        val direction:Direction,
        val icon:ImageVector,
        val label: StringResource
    )

    val loggedInOnlyDestinations:List<BottomBarDestination> =listOf(
        BottomBarDestination(ProfilePageViewDestination,ProfilePageViewDestination(),Icons.Default.Person,LocalText().getStringResource("profile")),
        BottomBarDestination(SettingsViewDestination,SettingsViewDestination,Icons.Default.Settings,LocalText().getStringResource("settings"))
    )

    val destinations:MutableList<BottomBarDestination> =mutableListOf(
        BottomBarDestination(HomepageDataViewDestination,HomepageDataViewDestination,Icons.Default.Home,LocalText().getStringResource("home")),
        BottomBarDestination(SearchResultsViewDestination,SearchResultsViewDestination,Icons.Default.Search,LocalText().getStringResource("search")),
        BottomBarDestination(SignInViewDestination,SignInViewDestination,Icons.Default.Person,LocalText().getStringResource("sign_in")),
    )

    val loginModel=LoginModel()
    if(loginModel.isLoggedIn.collectAsState().value){
        destinations.addAll(loggedInOnlyDestinations)
    }


    val destinationsNavigator=navController.rememberDestinationsNavigator()
    val currentDestination=navController.currentDestinationAsState().value?:NavGraphs.root.startDestination
    NavigationBar{
        destinations.forEach{destination->
            NavigationBarItem(
                selected=currentDestination==destination.destination,
                onClick={
                    destinationsNavigator.navigate(destination.direction){
                        launchSingleTop=true
                        restoreState=true
                    }
                },
                icon={
                    Icon(destination.icon,contentDescription=stringResource(destination.label))
                },
                label={
                    Text(text=stringResource(destination.label))
                }
            )
        }
    }

}