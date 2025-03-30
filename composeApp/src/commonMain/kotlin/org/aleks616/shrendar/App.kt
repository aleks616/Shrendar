package org.aleks616.shrendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.ui.tooling.preview.Preview
import shrendar.composeapp.generated.resources.Res
import shrendar.composeapp.generated.resources.compose_multiplatform


val networkClient=NetworkClient()
suspend fun fetchRanks():List<Ranks> =networkClient.fetchRanks()

@Composable
@Preview
fun App(){
    var albums by remember{mutableStateOf(emptyList<Ranks>())}
    var clicks by remember{mutableStateOf(0)}


    LaunchedEffect(clicks){
        albums=fetchRanks()
    }

    var showContent by remember{mutableStateOf(false)}
    Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally){
        Button(onClick={showContent=!showContent},colors=ButtonDefaults.buttonColors(backgroundColor=Color.DarkGray)){
            Text("Click me!",color=Color.White)
        }
        AnimatedVisibility(showContent){
            val greeting=remember{Greeting().greet()}
            Column(Modifier.fillMaxWidth(),horizontalAlignment=Alignment.CenterHorizontally){
                Image(painterResource(Res.drawable.compose_multiplatform),null)
                Text("Compose: $greeting")
            }
        }

/*        Text(clicks.toString())
        Text(albums.lastIndex.toString())*/
        albums.forEach{
            Text(text=it.id.toString()+" "+it.name+" "+it.minXp)
        }
    }

}