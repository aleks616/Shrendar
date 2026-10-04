package com.example.client.account.components

import android.content.Context
import android.util.Log
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.edit
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.NoCredentialException
import com.example.client.AppTheme
import com.example.client.account.AccountClient
import com.google.android.gms.common.SignInButton
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import kotlinx.coroutines.launch
import org.json.JSONObject


@Composable
@Preview
@OptIn(ExperimentalMaterial3Api::class)
fun SignInWithGoogleView(){
    val scope=rememberCoroutineScope()
    val context=LocalContext.current

    suspend fun signInWithGoogle() {
        val googleIdOption=GetSignInWithGoogleOption.Builder(
            "13978966379-vdg146pscvqtdotp98lqvjnrj9lik4nf.apps.googleusercontent.com"
        )
            .build()
        val request=GetCredentialRequest.Builder()
            .addCredentialOption(googleIdOption)
            .build()
        val credential=try {
            CredentialManager.create(context)
                .getCredential(context=context,request=request)
                .credential
        }
        catch(_:NoCredentialException) {
            Log.e("sign in with google","no credential found")
            return
        }

        if(credential is CustomCredential&&
           credential.type==GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
        ) {
            val googleToken=GoogleIdTokenCredential.createFrom(credential.data).idToken
            val result=AccountClient.authWithGoogle(googleToken)
            val token=JSONObject(result).getString("token")
            context.getSharedPreferences("authToken",Context.MODE_PRIVATE)
                .edit {putString("authToken",token)}
        }
    }

    AppTheme{
        AndroidView(
            factory={viewContext->
                SignInButton(viewContext).apply {
                    setSize(SignInButton.SIZE_WIDE)
                    setColorScheme(SignInButton.COLOR_LIGHT)
                    setOnClickListener {scope.launch {signInWithGoogle()}}
                }
            }
        )
    }
}