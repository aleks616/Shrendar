package com.example.client.register

import com.example.client.LocalText
import kotlin.js.ExperimentalJsExport
import kotlin.js.JsExport

@OptIn(ExperimentalJsExport::class)
@JsExport
class RegisterValidator {
    suspend fun validateLogin(login:String):String? {
        if(login.length<5) return "login_too_short"
        if(login.length>25) return "login_too_long"
        if(RegisterApi.doesLoginExist(login)) return "login_already_exists"
        return null
    }

    suspend fun validateEmail(email:String):String? {
        val emailAddressRegex = Regex(
            "[a-zA-Z0-9+._%\\-]{1,256}" +
            "@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+"
        )
        if(!email.matches(emailAddressRegex)) return "email_invalid"
        if(RegisterApi.doesEmailExist(email)) return "email_already_exists"
        return null
    }

    fun isPasswordValid(password:String):Boolean {
        if(password.length<8) return false
        if(password.length>32) return false
        return password.any {it.isUpperCase()}&&
               password.any {it.isLowerCase()}&&
               password.any {it.isDigit()}&&
               password.any {!it.isLetterOrDigit()}
    }
}
