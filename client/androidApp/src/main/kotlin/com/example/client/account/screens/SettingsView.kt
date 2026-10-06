package com.example.client.account.screens

import android.content.Context
import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.core.content.edit
import com.example.client.*
import com.example.client.account.AccountClient
import com.example.client.account.LoginRequestDto
import com.example.client.common.UserDto
import dev.icerock.moko.resources.StringResource
import dev.icerock.moko.resources.compose.stringResource
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import kotlin.js.ExperimentalJsExport
import com.example.client.common.Date as KotlinDate

private val minDate=LocalDate.now().minusYears(120)
private val maxDate=LocalDate.now().minusYears(13)

private fun LocalDate.toEpochMillis():Long=
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toLocalDate():LocalDate=
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@Composable
@Preview
@OptIn(ExperimentalJsExport::class,ExperimentalMaterial3Api::class)
fun SettingsView(
    onBack:()->Unit={},
    onChangePassword:()->Unit={},
) {
    val context=LocalContext.current
    val scope=rememberCoroutineScope()

    var userData by remember {mutableStateOf<UserDto?>(null)}
    var username by remember {mutableStateOf("")}
    var email by remember {mutableStateOf("")}
    var birthdate by remember {mutableStateOf<LocalDate?>(null)}
    var isLoading by remember {mutableStateOf(true)}
    var errorText:StringResource? by remember {mutableStateOf(null)}

    var modalOpen by remember {mutableStateOf(false)}
    var showDatePicker by remember {mutableStateOf(false)}
    var modalEmail by remember {mutableStateOf("")}
    var login by remember {mutableStateOf("")}
    var password by remember {mutableStateOf("")}

    val usernameInvalid=username.isNotEmpty()&&(username.length<4||username.length>50)
    val emailInvalid=email.isNotEmpty()&&!email.contains("@")
    val birthdateInvalid=birthdate!=null&&(
            birthdate!!<minDate||birthdate!!>maxDate
                                          )

    fun token():String?=
        context.getSharedPreferences("authToken",Context.MODE_PRIVATE)
            .getString("authToken",null)

    suspend fun updateUsername() {
        val authToken=token()
        if(!authToken.isNullOrBlank()) {
            val result=AccountClient.updateUsername(authToken,username)
            if(result!="username_changed") {
                errorText=LocalText().getStringResource(result)
            }
            else {
                userData=userData?.copy(username=username)
            }
        }
    }

    suspend fun updateEmail() {
        val authToken=token()
        if(!authToken.isNullOrBlank()) {
            val result=AccountClient.updateEmail(authToken,email)
            if(result!="email_changed") {
                errorText=LocalText().getStringResource(result)
            }
            else {
                userData=userData?.copy(email=email)
            }
        }
    }

    suspend fun updateBirthdate() {
        val authToken=token()
        val birthdateAsKotlinDate=birthdate?.let {
            KotlinDate(it.year,it.monthValue,it.dayOfMonth)
        }
        if(!authToken.isNullOrBlank()&&birthdateAsKotlinDate!=null) {
            val result=AccountClient.addBirthday(authToken,birthdateAsKotlinDate)
            if(result!="birthday_added") {
                errorText=LocalText().getStringResource(result)
            }
            else {
                userData=userData?.copy(birthDate=birthdateAsKotlinDate)
            }
        }
    }

    suspend fun submitChanges() {
        errorText=null
        try {
            if(username!=userData?.username) {
                updateUsername()
            }
            if(email!=userData?.email) {
                updateEmail()
            }
            if(birthdate!=userData?.birthDate?.let {LocalDate.of(it.year,it.month,it.day)}) {
                updateBirthdate()
            }
        }
        catch(e:Exception) {
            Log.e("settings",e.toString())
            errorText=MR.strings.something_wrong
        }
    }

    suspend fun deleteAccount() {
        val authToken=token()
        if(authToken.isNullOrBlank()) {
            errorText=MR.strings.something_wrong
            modalOpen=false
            return
        }

        val request=LoginRequestDto(
            login=login.ifEmpty {null},
            email=modalEmail.ifEmpty {null},
            password=password,
        )
        modalOpen=false
        modalEmail=""
        login=""
        password=""
        try {
            val language=java.util.Locale.getDefault().language
                             .takeIf {it.isNotBlank()}?.uppercase()?:"EN"
            val result=AccountClient.deleteAccount(authToken,request,language)
            if(result=="confirmed") {
                context.getSharedPreferences("authToken",Context.MODE_PRIVATE)
                    .edit {remove("authToken")}
                onBack()
            }
            else {
                errorText=LocalText().getStringResource(result)
            }
        }
        catch(e:Exception) {
            Log.e("settings",e.toString())
            errorText=MR.strings.something_wrong
        }
    }

    LaunchedEffect(Unit) {
        val authToken=token()
        if(authToken.isNullOrBlank()) {
            errorText=MR.strings.something_wrong
            isLoading=false
        }
        else {
            try {
                val user=AccountClient.getUserData(authToken)
                if(user.login.isNullOrBlank()) {
                    errorText=MR.strings.something_wrong
                }
                else {
                    userData=user
                    username=user.username.orEmpty()
                    email=user.email.orEmpty()
                    birthdate=user.birthDate?.let {
                        LocalDate.of(it.year,it.month,it.day)
                    }
                    errorText=null
                }
            }
            catch(e:Exception) {
                Log.e("settings",e.toString())
                errorText=MR.strings.something_wrong
            }
            isLoading=false
        }
    }

    AppTheme {
        Surface(modifier=Modifier.fillMaxSize()) {
            Column(modifier=Modifier.fillMaxSize()) {
                TopAppBar(
                    title={Text("Settings")},
                    navigationIcon={BackButton {onBack()}},
                )

                if(isLoading) {
                    Box(
                        contentAlignment=Alignment.Center,
                        modifier=Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator()
                    }
                }
                else {
                    Column(
                        modifier=Modifier
                            .fillMaxSize()
                            .verticalScroll(rememberScrollState())
                            .padding(horizontal=20.dp,vertical=16.dp),
                        verticalArrangement=Arrangement.spacedBy(16.dp),
                    ) {
                        Text("Username")
                        TextField(
                            value=username,
                            onValueChange={username=it},
                            isError=usernameInvalid,
                            supportingText={
                                if(usernameInvalid) {
                                    Text("Username length must be between 4 and 50 characters")
                                }
                            },
                            modifier=Modifier.fillMaxWidth(),
                        )

                        Text("Email")
                        TextField(
                            value=email,
                            onValueChange={},
                            enabled=false,
                            isError=emailInvalid,
                            supportingText={
                                if(emailInvalid) {
                                    Text(text=stringResource(MR.strings.invalid_email))
                                }
                            },
                            keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Email),
                            modifier=Modifier.fillMaxWidth(),
                        )

                        Text("Birthdate")
                        OutlinedButton(
                            onClick={showDatePicker=true},
                            modifier=Modifier.fillMaxWidth(),
                        ) {
                            Text(birthdate?.toString()?:"Select birthdate")
                        }

                        if(birthdateInvalid) {
                            Text(
                                "User must be between 13 and 120 years old",
                                color=Color.Red,
                            )
                        }

                        errorText?.let {
                            Text(
                                text=stringResource(it),
                                color=Color.Red,
                            )
                        }

                        Spacer(modifier=Modifier.height(48.dp))

                        Button(
                            onClick={
                                scope.launch {submitChanges()}
                            },
                            enabled=!usernameInvalid&&!emailInvalid&&!birthdateInvalid,
                            modifier=Modifier.fillMaxWidth(),
                        ) {
                            Text("Save changes")
                        }

                        Button(
                            onClick=onChangePassword,
                            modifier=Modifier.fillMaxWidth(),
                            colors=ButtonDefaults.buttonColors(containerColor=MaterialTheme.colorScheme.onSurfaceVariant)
                        ) {
                            Text(
                                text=stringResource(MR.strings.change_password),
                                color=MaterialTheme.colorScheme.surface
                            )
                        }

                        Button(
                            onClick={modalOpen=true},
                            modifier=Modifier.fillMaxWidth(),
                            colors=ButtonDefaults.buttonColors(containerColor=MaterialTheme.colorScheme.error)
                        ) {
                            Text("Delete account")
                        }
                    }
                }
            }
        }
    }

    AppTheme {
        if(showDatePicker) {
            val datePickerState=rememberDatePickerState(
                initialSelectedDateMillis=birthdate?.toEpochMillis(),
                yearRange=minDate.year..maxDate.year,
            )
            DatePickerDialog(
                onDismissRequest={showDatePicker=false},
                confirmButton={
                    TextButton(
                        onClick={
                            birthdate=datePickerState.selectedDateMillis?.toLocalDate()
                            showDatePicker=false
                        },
                    ) {
                        Text("OK")
                    }
                },
                dismissButton={
                    TextButton(onClick={showDatePicker=false}) {
                        Text("Cancel")
                    }
                },
            ) {
                DatePicker(state=datePickerState)
            }
        }

        if(modalOpen) {
            AlertDialog(
                onDismissRequest={modalOpen=false},
                title={Text("Delete account")},
                text={
                    Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                        Text(text=stringResource(MR.strings.email_address))
                        TextField(
                            value=modalEmail,
                            onValueChange={modalEmail=it},
                            keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Email),
                            singleLine=true,
                        )
                        Text(text=stringResource(MR.strings.login))
                        TextField(
                            value=login,
                            onValueChange={login=it},
                            singleLine=true,
                        )
                        Text(text=stringResource(MR.strings.password))
                        TextField(
                            value=password,
                            onValueChange={password=it},
                            keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Password),
                            visualTransformation=PasswordVisualTransformation(),
                            singleLine=true,
                        )
                    }
                },
                dismissButton={
                    TextButton(onClick={modalOpen=false}) {
                        Text("Cancel")
                    }
                },
                confirmButton={
                    TextButton(
                        onClick={scope.launch {deleteAccount()}},
                        enabled=login.isNotEmpty()&&password.isNotEmpty(),
                    ) {
                        Text("Delete account",color=MaterialTheme.colorScheme.error)
                    }
                },
            )
        }
    }
}
