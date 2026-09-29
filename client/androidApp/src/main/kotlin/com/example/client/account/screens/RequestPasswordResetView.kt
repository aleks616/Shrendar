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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.client.*
import com.example.client.account.AccountClient
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.*
import kotlin.time.Duration.Companion.seconds

@Composable
@Preview
@OptIn(ExperimentalMaterial3Api::class)
fun RequestPasswordResetView(
    onBack:()->Unit={},
) {
    var login by remember {mutableStateOf("")}
    var errorKey by remember {mutableStateOf<String?>(null)}
    var codeSent by remember {mutableStateOf(false)}
    var timerOn by remember {mutableStateOf(false)}
    var resendCountdown by remember {mutableStateOf(60)}

    val scope=rememberCoroutineScope()

    suspend fun requestPasswordReset() {
        try {
            val language=Locale.getDefault().language.takeIf {it.isNotBlank()}?.uppercase()?:"EN"
            val result=AccountClient.requestPasswordReset(login,language)
            if(result!="password_link_sent") {
                if(result=="too_many_user_requests") {
                    resendCountdown=240
                    timerOn=true
                }
                errorKey=result
                return
            }
            errorKey=null
            codeSent=true
            timerOn=true
            resendCountdown=60
        }
        catch(e:Exception) {
            Log.e("request password reset",e.toString())
        }
    }

    LaunchedEffect(timerOn) {
        while(timerOn) {
            if(resendCountdown>0) {
                resendCountdown--
                delay(1.seconds)
            }
            else {
                timerOn=false
            }
        }
    }

    AppTheme {
        Surface {
            Column(modifier=Modifier.fillMaxSize()) {
                TopAppBar(
                    title={},
                    navigationIcon={
                        BackButton {onBack()}
                    },
                )
                Column(
                    horizontalAlignment=Alignment.CenterHorizontally,
                    modifier=Modifier.fillMaxWidth(),
                    verticalArrangement=Arrangement.spacedBy(16.dp)
                ) {
                    Text(
                        text=stringResource(MR.strings.forgot_password_question),
                        fontSize=28.sp,
                        fontWeight=FontWeight.Bold
                    )
                    Spacer(modifier=Modifier.height(4.dp))
                    TextField(
                        value=login,
                        onValueChange={login=it},
                        label={Text(stringResource(MR.strings.login_email))},
                        modifier=Modifier
                            .fillMaxWidth()
                            .padding(horizontal=20.dp),
                        keyboardOptions=KeyboardOptions(
                            autoCorrectEnabled=false,
                            keyboardType=KeyboardType.Email,
                            imeAction=ImeAction.Done
                        )
                    )
                    Button(
                        onClick={
                            scope.launch {
                                requestPasswordReset()
                            }
                        },
                        enabled=login.isNotEmpty()&&!timerOn,
                    ) {
                        Text(stringResource(MR.strings.reset_password))
                    }

                    errorKey?.let {key->
                        Text(text=stringResource(LocalText().getStringResource(key)),color=Color.Red)
                    }

                    if(codeSent) {
                        Text(stringResource(MR.strings.password_link_sent))
                        if(timerOn) {
                            Text(text=stringResource(MR.strings.resend_code_in)+' '+resendCountdown)
                        }
                        Button(
                            onClick={
                                scope.launch {
                                    requestPasswordReset()
                                }
                            },
                            enabled=resendCountdown==0
                        ) {
                            Text(stringResource(MR.strings.resend_code))
                        }
                    }
                }
            }
        }
    }
}