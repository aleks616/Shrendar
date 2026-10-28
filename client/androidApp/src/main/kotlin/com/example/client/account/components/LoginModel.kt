package com.example.client.account.components

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

class LoginModel {
    private val _isLoggedIn=MutableStateFlow(false)
    val isLoggedIn get()=_isLoggedIn.asStateFlow()
    fun setLoggedIn(value:Boolean){
        _isLoggedIn.value=value
    }
}