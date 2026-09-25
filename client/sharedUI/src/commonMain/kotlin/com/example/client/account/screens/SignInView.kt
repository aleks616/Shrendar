package com.example.client.account.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.client.*
import com.example.client.account.AccountClient
import com.example.client.account.LoginRequestDto
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import org.json.JSONObject
import androidx.core.content.edit
import dev.icerock.moko.resources.compose.painterResource

@Composable
@Preview
@OptIn(ExperimentalMaterial3Api::class)
fun SignInView(
    onBack:()->Unit={},
) {
    var login by remember {mutableStateOf("")}
    var password by remember {mutableStateOf("")}
    var errorKey by remember {mutableStateOf<String?>(null)}
    val scope=rememberCoroutineScope()
    val context=LocalContext.current

    suspend fun signIn(context:Context) {
        try {
            val isEmail=login.contains("@")
            val loginRequest=LoginRequestDto(
                login=if(isEmail) null else login,
                email=if(isEmail) login else null,
                password=password
            )
            val result=AccountClient.login(loginRequest)
            try {
                val token=JSONObject(result).getString("token")
                val preferenceToken=context.getSharedPreferences("authToken",Context.MODE_PRIVATE)
                preferenceToken.edit {putString("authToken",token)}
                errorKey=null
                Log.i("sign in","success")
            }
            catch(e:Exception) {
                Log.e("sign in",e.toString())
                errorKey="something_wrong"
            }
        }
        catch(e:Exception) {
            Log.e("sign in",e.toString())
            errorKey="something_wrong"
        }
    }

    AppTheme {
        Surface {
            TopAppBar(
                title={},
                navigationIcon={
                    IconButton(onClick=onBack) {
                        Icon(
                            painter=painterResource(MR.images.arrow_left),
                            contentDescription="back",
                            modifier=Modifier.size(24.dp),
                            tint=MaterialTheme.colorScheme.primary
                        )
                    }
                },
            )
            Column(
                horizontalAlignment=Alignment.CenterHorizontally,
                modifier=Modifier.padding(top=25.dp).fillMaxWidth(),
                verticalArrangement=Arrangement.spacedBy(15.dp)
            ) {
                Text(text=stringResource(MR.strings.sign_in),fontSize=28.sp,fontWeight=FontWeight.Bold)
                Spacer(modifier=Modifier.height(20.dp))
                TextField(
                    value=login,
                    onValueChange={login=it},
                    label={Text(stringResource(MR.strings.login_email))},
                    keyboardOptions=KeyboardOptions(
                        autoCorrectEnabled=false,
                        keyboardType=KeyboardType.Email,
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
                errorKey?.let {key->
                    Text(text=stringResource(LocalText().getStringResource(key)),color=Color.Red)
                }

                Button(
                    onClick={
                        scope.launch {
                            try {
                                signIn(context)
                            }
                            catch(e:Exception) {
                                Log.e("sign in",e.localizedMessage?:"")
                                return@launch
                            }
                        }
                    },enabled=!(login.isEmpty()||password.isEmpty())
                ) {
                    Text(stringResource(MR.strings.sign_in))
                }
            }
        }
    }
}