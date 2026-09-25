package com.example.client.account.screens

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.client.AppTheme
import com.example.client.BackButton
import com.example.client.LocalText
import com.example.client.MR
import com.example.client.account.components.LabelledDivider
import com.example.client.account.components.OtpInputField
import com.example.client.account.components.pxToDp
import com.example.client.account_created
import com.example.client.already_have_an_account
import com.example.client.arrow_left
import com.example.client.confirm_account
import com.example.client.continue_as
import com.example.client.create_account
import com.example.client.email_address
import com.example.client.guest_question
import com.example.client.login
import com.example.client.or
import com.example.client.password
import com.example.client.re_enter_password
import com.example.client.register.RegisterClient
import com.example.client.register.RegisterRequestDto
import com.example.client.register.RegisterValidator
import com.example.client.resend_code
import com.example.client.resend_code_in
import com.example.client.sign_in
import com.example.client.sign_up
import com.example.client.sign_up_to_continue
import com.example.client.special_sign_in_later
import com.example.client.verification_code_sent
import dev.icerock.moko.resources.compose.painterResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

@Composable
@Preview
@OptIn(ExperimentalMaterial3Api::class)
fun RegisterView(
    onBack:()->Unit={},
    signInScreen:()->Unit={},
) {
    var email by remember {mutableStateOf("")}
    var login by remember {mutableStateOf("")}
    var password by remember {mutableStateOf("")}
    var repeatPassword by remember {mutableStateOf("")}
    var errorKey by remember {mutableStateOf<String?>(null)}

    val code=remember {mutableStateOf("")}
    var codeSent:Boolean by remember {mutableStateOf(false)}
    var timerOn:Boolean by remember {mutableStateOf(false)}
    var resendCountdown by remember {mutableStateOf(60)}
    var confirmed:Boolean by remember {mutableStateOf(false)}

    val scope=rememberCoroutineScope()

    suspend fun validate():String? {
        val registerValidator=RegisterValidator()

        val loginValid=registerValidator.validateLogin(login)
        if(loginValid!=null) return loginValid

        val emailValid=registerValidator.validateEmail(email)
        if(emailValid!=null) return emailValid

        if(password!=repeatPassword) return "passwords_dont_match"

        val passwordValid=registerValidator.isPasswordValid(password)
        if(!passwordValid) return "invalid_password"

        return null
    }

    suspend fun register() {
        val language=Locale.getDefault().language.takeIf {it.isNotBlank()}?.uppercase()?:"EN"
        val registerRequestDto=
            RegisterRequestDto(login=login,displayName=login,email=email,password=password,language=language)
        val result=RegisterClient.register(registerRequestDto)
        if(result=="verification_code_sent") {
            errorKey=null
            codeSent=true
            timerOn=true
        }
        else if(result=="something_wrong") {
            errorKey="something_wrong"
        }
    }

    suspend fun confirmAccount() {
        val registerRequest=RegisterRequestDto(login=login,displayName=login,email=email,password=password)
        val confirmationResult=RegisterClient.registerConfirm(registerRequest,code.value)
        if(confirmationResult=="account_created") {
            confirmed=true
            timerOn=false
            codeSent=false
            errorKey=null
        }
        else {
            errorKey=confirmationResult
            code.value=""
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
                    Text(text=stringResource(MR.strings.create_account),fontSize=28.sp,fontWeight=FontWeight.Bold)
                    Text(text=stringResource(MR.strings.sign_up_to_continue),fontSize=20.sp)
                    Spacer(modifier=Modifier.height(20.dp))
                    TextField(
                        value=email,
                        onValueChange={email=it},
                        label={Text(stringResource(MR.strings.email_address))},
                        keyboardOptions=KeyboardOptions(
                            autoCorrectEnabled=false,
                            keyboardType=KeyboardType.Email,
                            imeAction=ImeAction.Next
                        )
                    )
                    TextField(
                        value=login,
                        onValueChange={login=it},
                        label={Text(stringResource(MR.strings.login))},
                        keyboardOptions=KeyboardOptions(
                            autoCorrectEnabled=false,
                            keyboardType=KeyboardType.Text,
                            imeAction=ImeAction.Next
                        )
                    )
                    TextField(
                        value=password,
                        onValueChange={password=it},
                        label={Text(stringResource(MR.strings.password))},
                        keyboardOptions=KeyboardOptions(
                            autoCorrectEnabled=false,
                            keyboardType=KeyboardType.Password,
                            imeAction=ImeAction.Next
                        ),
                        visualTransformation=PasswordVisualTransformation()
                    )
                    TextField(
                        value=repeatPassword,
                        onValueChange={repeatPassword=it},
                        label={Text(stringResource(MR.strings.re_enter_password))},
                        keyboardOptions=KeyboardOptions(
                            autoCorrectEnabled=false,
                            keyboardType=KeyboardType.Password,
                            imeAction=ImeAction.Done
                        ),
                        visualTransformation=PasswordVisualTransformation(),
                    )
                    errorKey?.let {key->
                        Text(text=stringResource(LocalText().getStringResource(key)),color=Color.Red)
                    }
                    Button(
                        onClick={
                            scope.launch {
                                val validationError:String?
                                try {
                                    validationError=validate()
                                }
                                catch(e:Exception) {
                                    Log.e("validate register data",e.localizedMessage?:"")
                                    return@launch
                                }
                                errorKey=validationError
                                if(validationError.isNullOrEmpty()) {
                                    try {
                                        register()
                                    }
                                    catch(e:Exception) {
                                        Log.e("register",e.localizedMessage?:"")
                                        return@launch
                                    }
                                }
                            }
                        },enabled=!(email.isBlank()||login.isBlank()||password.isBlank()||repeatPassword.isBlank()
                                    ||confirmed)
                    ) {
                        Text(text=stringResource(MR.strings.sign_up))
                    }

                    if(codeSent) {
                        Text(stringResource(MR.strings.verification_code_sent))
                        if(timerOn) {
                            //todo decrease time
                            Text(text=stringResource(MR.strings.resend_code_in)+' '+resendCountdown)
                        }
                        Button(
                            onClick={
                                scope.launch {
                                    val validationError:String?
                                    try {
                                        validationError=validate()
                                    }
                                    catch(e:Exception) {
                                        Log.e("validate register data",e.localizedMessage?:"")
                                        return@launch
                                    }
                                    errorKey=validationError
                                    if(validationError.isNullOrEmpty()) {
                                        try {
                                            register()
                                        }
                                        catch(e:Exception) {
                                            Log.e("register",e.localizedMessage?:"")
                                            return@launch
                                        }
                                    }
                                }
                            },
                            enabled=resendCountdown==0
                        ) {
                            Text(stringResource(MR.strings.resend_code))
                        }

                        OtpInputField(
                            otp=code,
                            count=6,
                            otpBoxModifier=Modifier
                                .border(3.pxToDp(),Color.Black)
                                .background(Color.White),
                            otpTextType=KeyboardType.Number
                        )
                        Button(
                            onClick={
                                scope.launch {
                                    try {
                                        confirmAccount()
                                    }
                                    catch(e:Exception) {
                                        Log.e("register-confirm",e.localizedMessage?:"")
                                        return@launch
                                    }
                                }
                            },
                            enabled=code.value.length==6
                        ) {
                            Text(stringResource(MR.strings.confirm_account))
                        }
                    }
                    if(confirmed)
                        Text(stringResource(MR.strings.account_created))


                    LabelledDivider(text=stringResource(MR.strings.or))
                    Row(horizontalArrangement=Arrangement.Center,verticalAlignment=Alignment.CenterVertically) {
                        Text(text=stringResource(MR.strings.already_have_an_account))
                        TextButton(onClick={signInScreen},modifier=Modifier.padding(0.dp)) {
                            Text(text=stringResource(MR.strings.sign_in),modifier=Modifier.padding(0.dp))
                        }
                    }
                    Text(text=stringResource(MR.strings.special_sign_in_later))

                }
            }
        }
    }
}
