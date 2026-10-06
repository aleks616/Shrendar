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
import com.example.client.AppTheme
import com.example.client.BackButton
import com.example.client.account.AccountClient
import com.example.client.account.LoginRequestDto
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import com.example.client.common.Date as KotlinDate

private val minimumSettingsBirthdate=LocalDate.now().minusYears(120)
private val maximumSettingsBirthdate=LocalDate.now().minusYears(13)

private fun LocalDate.toSettingsEpochMillis():Long=
    atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()

private fun Long.toSettingsLocalDate():LocalDate=
    Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

@Composable
@Preview
@OptIn(ExperimentalMaterial3Api::class)
fun SettingsView(
    onBack:()->Unit={},
    onChangePassword:()->Unit={},
) {
    val context=LocalContext.current
    val scope=rememberCoroutineScope()

    var username by remember {mutableStateOf("")}
    var email by remember {mutableStateOf("")}
    var birthdate by remember {mutableStateOf<LocalDate?>(null)}
    var originalUsername by remember {mutableStateOf("")}
    var originalEmail by remember {mutableStateOf("")}
    var originalBirthdate by remember {mutableStateOf<LocalDate?>(null)}
    var isLoading by remember {mutableStateOf(true)}
    var isSaving by remember {mutableStateOf(false)}
    var errorText by remember {mutableStateOf<String?>(null)}
    var showDeleteDialog by remember {mutableStateOf(false)}
    var showDatePicker by remember {mutableStateOf(false)}
    var deleteEmail by remember {mutableStateOf("")}
    var deleteLogin by remember {mutableStateOf("")}
    var deletePassword by remember {mutableStateOf("")}

    val usernameInvalid=username.isNotEmpty()&&(username.length<4||username.length>50)
    val emailInvalid=email.isNotEmpty()&&!email.contains("@")
    val birthdateInvalid=birthdate!=null&&(
            birthdate!!<minimumSettingsBirthdate||birthdate!!>maximumSettingsBirthdate
                                          )

    fun token():String?=
        context.getSharedPreferences("authToken",Context.MODE_PRIVATE)
            .getString("authToken",null)

    suspend fun saveChanges() {
        val authToken=token()
        if(authToken.isNullOrBlank()) {
            errorText="Something went wrong"
            return
        }

        isSaving=true
        errorText=null
        try {
            if(username!=originalUsername) {
                val result=AccountClient.updateUsername(authToken,username)
                if(result!="username_changed") {
                    errorText=result
                    isSaving=false
                    return
                }
                originalUsername=username
            }

            if(email!=originalEmail) {
                val result=AccountClient.updateEmail(authToken,email)
                if(result!="email_changed") {
                    errorText=result
                    isSaving=false
                    return
                }
                originalEmail=email
            }

            if(birthdate!=originalBirthdate&&birthdate!=null) {
                val date=birthdate!!
                val result=AccountClient.addBirthday(
                    authToken,
                    KotlinDate(date.year,date.monthValue,date.dayOfMonth)
                )
                if(result!="birthday_added") {
                    errorText=result
                    isSaving=false
                    return
                }
                originalBirthdate=date
            }
        }
        catch(e:Exception) {
            Log.e("settings save",e.localizedMessage?:"")
            errorText=e.localizedMessage?:"Something went wrong"
        }
        isSaving=false
    }

    suspend fun deleteAccount() {
        val authToken=token()
        if(authToken.isNullOrBlank()) {
            errorText="Something went wrong"
            showDeleteDialog=false
            return
        }

        val request=LoginRequestDto(
            login=deleteLogin.ifEmpty {null},
            email=deleteEmail.ifEmpty {null},
            password=deletePassword,
        )
        showDeleteDialog=false
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
                errorText=result
            }
        }
        catch(e:Exception) {
            Log.e("settings delete account",e.localizedMessage?:"")
            errorText=e.localizedMessage?:"Something went wrong"
        }
    }

    LaunchedEffect(Unit) {
        val authToken=token()
        if(authToken.isNullOrBlank()) {
            errorText="Something went wrong"
            isLoading=false
        }
        else {
            try {
                val user=AccountClient.getUserData(authToken)
                if(user.login.isNullOrBlank()) {
                    errorText="Something went wrong"
                }
                else {
                    username=user.username.orEmpty()
                    email=user.email.orEmpty()
                    birthdate=user.birthDate?.let {
                        LocalDate.of(it.year,it.month,it.day)
                    }
                    originalUsername=username
                    originalEmail=email
                    originalBirthdate=birthdate
                    errorText=null
                }
            }
            catch(e:Exception) {
                Log.e("settings load",e.localizedMessage?:"")
                errorText=e.localizedMessage?:"Something went wrong"
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
                        OutlinedTextField(
                            value=username,
                            onValueChange={username=it},
                            label={Text("Username")},
                            isError=usernameInvalid,
                            supportingText={
                                if(usernameInvalid) {
                                    Text("Username length must be between 4 and 50 characters")
                                }
                            },
                            modifier=Modifier.fillMaxWidth(),
                        )

                        OutlinedTextField(
                            value=email,
                            onValueChange={},
                            label={Text("Email")},
                            enabled=false,
                            isError=emailInvalid,
                            supportingText={
                                if(emailInvalid) {
                                    Text("Invalid email address")
                                }
                            },
                            keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Email),
                            modifier=Modifier.fillMaxWidth(),
                        )

                        OutlinedButton(
                            onClick={showDatePicker=true},
                            modifier=Modifier.fillMaxWidth(),
                        ) {
                            Column(horizontalAlignment=Alignment.Start) {
                                Text("Date")
                                Text(
                                    birthdate?.format(DateTimeFormatter.ISO_LOCAL_DATE)
                                    ?:"Select date"
                                )
                            }
                        }

                        if(birthdateInvalid) {
                            Text(
                                "User must be between 13 and 120 years old",
                                color=Color.Red,
                            )
                        }

                        errorText?.let {
                            Text(it,color=Color.Red)
                        }

                        Button(
                            onClick={
                                scope.launch {saveChanges()}
                            },
                            enabled=!isSaving&&!usernameInvalid&&!emailInvalid&&!birthdateInvalid,
                            modifier=Modifier.fillMaxWidth(),
                        ) {
                            Text(if(isSaving) "Saving..." else "Save changes")
                        }

                        Button(
                            onClick=onChangePassword,
                            modifier=Modifier.fillMaxWidth(),
                        ) {
                            Text("Change password")
                        }

                        Button(
                            onClick={showDeleteDialog=true},
                            modifier=Modifier.fillMaxWidth(),
                        ) {
                            Text("Delete account")
                        }
                    }
                }
            }
        }
    }

    if(showDatePicker) {
        val datePickerState=rememberDatePickerState(
            initialSelectedDateMillis=birthdate?.toSettingsEpochMillis(),
            yearRange=minimumSettingsBirthdate.year..maximumSettingsBirthdate.year,
        )
        DatePickerDialog(
            onDismissRequest={showDatePicker=false},
            confirmButton={
                TextButton(
                    onClick={
                        birthdate=datePickerState.selectedDateMillis?.toSettingsLocalDate()
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

    if(showDeleteDialog) {
        AlertDialog(
            onDismissRequest={showDeleteDialog=false},
            title={Text("Delete account")},
            text={
                Column(verticalArrangement=Arrangement.spacedBy(8.dp)) {
                    OutlinedTextField(
                        value=deleteEmail,
                        onValueChange={deleteEmail=it},
                        label={Text("Email")},
                        keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Email),
                        singleLine=true,
                    )
                    OutlinedTextField(
                        value=deleteLogin,
                        onValueChange={deleteLogin=it},
                        label={Text("Login")},
                        singleLine=true,
                    )
                    OutlinedTextField(
                        value=deletePassword,
                        onValueChange={deletePassword=it},
                        label={Text("Password")},
                        keyboardOptions=KeyboardOptions(keyboardType=KeyboardType.Password),
                        visualTransformation=PasswordVisualTransformation(),
                        singleLine=true,
                    )
                }
            },
            dismissButton={
                TextButton(onClick={showDeleteDialog=false}) {
                    Text("Cancel")
                }
            },
            confirmButton={
                TextButton(
                    onClick={scope.launch {deleteAccount()}},
                    enabled=deleteLogin.isNotEmpty()&&deletePassword.isNotEmpty(),
                ) {
                    Text("Delete account")
                }
            },
        )
    }
}
