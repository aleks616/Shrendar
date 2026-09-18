package com.example.client.register

class RegisterValidator {
    suspend fun isLoginValid(login:String):String? {
        if(login.length<5) return "Login too short"
        if(login.length>20) return "Login too long"
        val registerApi=RegisterApi()
        if(registerApi.doesLoginExist(login)) return "User with this login already exists"
        return null
    }

    suspend fun isEmailValid(email:String):String? {
        val emailAddressRegex = Regex(
            "[a-zA-Z0-9+._%\\-]{1,256}" +
            "@" +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
            "(" +
            "\\." +
            "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25}" +
            ")+"
        )
        if(!email.matches(emailAddressRegex)) return "E-mail address is not valid"
        val registerApi=RegisterApi()
        if(registerApi.doesEmailExist(email)) return "User with this email already exists"
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
