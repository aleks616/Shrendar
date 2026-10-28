package com.example.client.account.screens

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.client.*
import com.example.client.account.AccountClient
import com.example.client.account.ResetPasswordDto
import com.example.client.register.RegisterValidator
import com.ramcosta.composedestinations.annotation.Destination
import com.ramcosta.composedestinations.annotation.RootGraph
import com.ramcosta.composedestinations.annotation.parameters.DeepLink
import com.ramcosta.composedestinations.navigation.DestinationsNavigator
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import java.util.*
import kotlin.js.ExperimentalJsExport

@Composable
@Destination<RootGraph>(
    deepLinks=[
        DeepLink(
            uriPattern="shrendar://reset-password?code={code}&email={email}"
        )
    ]
)
@OptIn(ExperimentalMaterial3Api::class,ExperimentalJsExport::class)
fun PasswordResetView(
    code:String,
    email:String,
    navigator:DestinationsNavigator,
) {
    var password by remember {mutableStateOf("")}
    var confirmPassword by remember {mutableStateOf("")}
    var errorKey by remember {mutableStateOf<String?>(null)}
    val scope=rememberCoroutineScope()
    val language=Locale.getDefault().language.takeIf {it.isNotBlank()}?.uppercase()?:"EN"

    suspend fun createPassword() {
        if(code.isBlank()||email.isBlank()) {
            errorKey="something_wrong"
            return
        }

        if(password!=confirmPassword) {
            errorKey="passwords_dont_match"
            return
        }

        val registerValidator=RegisterValidator()
        val passwordValid=registerValidator.isPasswordValid(password)
        if(!passwordValid) {
            errorKey="invalid_password"
            return
        }

        val resetPasswordRequest=ResetPasswordDto(
            email=email,
            newPassword=password,
            code=code,
            language=language,
        )

        try {
            AccountClient.resetPassword(resetPasswordRequest)
            errorKey=null
        }
        catch(e:Exception) {
            errorKey="something_wrong"
            Log.e("create password",e.toString())
        }
    }

    AppTheme {
        Surface {
            Column(modifier=Modifier.fillMaxSize()) {

                Column(
                modifier=Modifier
                    .fillMaxSize()
                    .padding(top=15.dp),
                horizontalAlignment=Alignment.CenterHorizontally,
                verticalArrangement=Arrangement.spacedBy(16.dp),
            ) {
                Text(
                    text=stringResource(MR.strings.create_new_password),
                    fontSize=28.sp,
                    fontWeight=FontWeight.Bold,
                )
                TextField(
                    value=password,
                    onValueChange={password=it},
                    label={Text(stringResource(MR.strings.password))},
                    keyboardOptions=KeyboardOptions(
                        autoCorrectEnabled=false,
                        keyboardType=KeyboardType.Password,
                        imeAction=ImeAction.Next,
                    ),
                    visualTransformation=PasswordVisualTransformation(),
                )
                TextField(
                    value=confirmPassword,
                    onValueChange={confirmPassword=it},
                    label={Text(stringResource(MR.strings.re_enter_password))},
                    keyboardOptions=KeyboardOptions(
                        autoCorrectEnabled=false,
                        keyboardType=KeyboardType.Password,
                        imeAction=ImeAction.Done,
                    ),
                    visualTransformation=PasswordVisualTransformation(),
                )
                errorKey?.let {key->
                    Text(text=stringResource(LocalText().getStringResource(key)),color=Color.Red)
                }
                Button(
                    onClick={
                        scope.launch {createPassword()}
                    },
                    enabled=password.isNotEmpty()&&confirmPassword.isNotEmpty(),
                    modifier=Modifier
                        .fillMaxWidth()
                        .padding(horizontal=20.dp),
                ) {
                    Text(stringResource(MR.strings.change_password))
                }
            }
            }
        }
    }
}