package com.example.client.account.components

import android.content.Context
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.content.edit
import com.example.client.MR
import com.example.client.account.AccountClient
import com.example.client.logout
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch

@Composable
@Preview
fun LogOutButton() {
    val scope=rememberCoroutineScope()
    val context=LocalContext.current

    suspend fun logout(context:Context):String? {
        try{
            val preferenceToken=context.getSharedPreferences("authToken",Context.MODE_PRIVATE)
            val token=preferenceToken.getString("authToken",null)
            if(token!=null){
                val result=AccountClient.logout(token)
                if(result=="logged_out"){
                    preferenceToken.edit {clear()}
                    LoginModel.setLoggedIn(false)
                    return null
                }
            }
            return "no token saved"
        }
        catch(_:Exception){
            return "something_wrong"
        }
    }


    Button(
        onClick={scope.launch {
            logout(context)
        }},
        colors=ButtonDefaults.buttonColors(containerColor=MaterialTheme.colorScheme.error
        )
    ) {
        Text(stringResource(MR.strings.logout))
    }
}