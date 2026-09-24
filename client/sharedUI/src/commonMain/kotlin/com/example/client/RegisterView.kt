package com.example.client

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
import com.example.client.register.RegisterClient
import com.example.client.register.RegisterRequestDto
import com.example.client.register.RegisterValidator
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import java.util.Locale
import kotlin.time.Duration.Companion.seconds

@Composable
@Preview
fun RegisterView() {
    var email by remember {mutableStateOf("")}
    var login by remember {mutableStateOf("")}
    var password by remember {mutableStateOf("")}
    var repeatPassword by remember {mutableStateOf("")}
    var errorKey by remember {mutableStateOf<String?>(null)}

    val code = remember {mutableStateOf("")}
    var codeSent:Boolean by remember {mutableStateOf(false)}
    var timerOn:Boolean by remember {mutableStateOf(false)}
    var resendCountdown by remember {mutableStateOf(60)}
    var confirmed:Boolean by remember {mutableStateOf(false)}

    val scope=rememberCoroutineScope()

    suspend fun validate():String?{
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

    suspend fun register(){
        val language=Locale.getDefault().language.takeIf {it.isNotBlank()}?.uppercase()?:"EN"
        val registerRequestDto=RegisterRequestDto(login=login,displayName=login,email=email,password=password,language=language)
        val result=RegisterClient.register(registerRequestDto)
        if(result=="verification_code_sent"){
            errorKey=null
            codeSent=true
            timerOn=true
        }
        else if(result=="something_wrong"){
            errorKey="something_wrong"
        }
    }

    suspend fun confirmAccount(){
        val registerRequest=RegisterRequestDto(login=login,displayName=login,email=email,password=password)
        val confirmationResult=RegisterClient.registerConfirm(registerRequest,code.value)
        if(confirmationResult=="account_created"){
            confirmed=true
            timerOn=false
            codeSent=false
            errorKey=null
        }
        else{
            errorKey=confirmationResult
            code.value=""
        }
    }

    LaunchedEffect(timerOn){
        while(timerOn){
            if(resendCountdown>0){
                resendCountdown--
                kotlinx.coroutines.delay(1.seconds)
            }
            else{
                timerOn=false
            }
        }
    }

    MaterialTheme {
        Surface{
            Column(
                horizontalAlignment=Alignment.CenterHorizontally,
                modifier=Modifier.padding(top=25.dp).fillMaxWidth(),
                verticalArrangement=Arrangement.spacedBy(15.dp)
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
                errorKey?.let{key->
                    Text(text=stringResource(LocalText().getStringResource(key)),color=Color.Red)
                }
                Button(
                    onClick={
                        scope.launch{
                            val validationError:String?
                            try{
                                validationError=validate()
                            }
                            catch(e:Exception){
                                Log.e("validate register data",e.localizedMessage?:"")
                                return@launch
                            }
                            errorKey=validationError
                            if(validationError.isNullOrEmpty()){
                                try{
                                    register()
                                }
                                catch(e:Exception){
                                    Log.e("register",e.localizedMessage?:"")
                                    return@launch
                                }
                            }
                        }
                    }, enabled=!(email.isBlank()||login.isBlank()||password.isBlank()||repeatPassword.isBlank()
                                  ||confirmed)
                ){
                    Text(text=stringResource(MR.strings.sign_up))
                }

                if(codeSent){
                    Text(stringResource(MR.strings.verification_code_sent))
                    if(timerOn){
                        //todo decrease time
                        Text(text=stringResource(MR.strings.resend_code_in)+' '+resendCountdown)
                    }
                    Button(
                        onClick={
                            scope.launch{
                                val validationError:String?
                                try{
                                    validationError=validate()
                                }
                                catch(e:Exception){
                                    Log.e("validate register data",e.localizedMessage?:"")
                                    return@launch
                                }
                                errorKey=validationError
                                if(validationError.isNullOrEmpty()){
                                    try{
                                        register()
                                    }
                                    catch(e:Exception){
                                        Log.e("register",e.localizedMessage?:"")
                                        return@launch
                                    }
                                }
                            }
                        },
                        enabled=resendCountdown==0
                    ){
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
                            scope.launch{
                                try{
                                    confirmAccount()
                                }
                                catch(e:Exception){
                                    Log.e("register-confirm",e.localizedMessage?:"")
                                    return@launch
                                }
                            }
                        },
                        enabled=code.value.length==6
                    ){
                        Text(stringResource(MR.strings.confirm_account))
                    }
                }
                if(confirmed)
                    Text(stringResource(MR.strings.account_created))


                LabelledDivider(text=stringResource(MR.strings.or))
                Text(text=stringResource(MR.strings.special_sign_in_later))

            }
        }
    }
}
