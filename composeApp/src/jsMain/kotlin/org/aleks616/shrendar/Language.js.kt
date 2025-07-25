package org.aleks616.shrendar

actual fun getLanguage():Language=object:Language{
    override val code:String
        get()=js("window.localStorage.getItem('language')")as?String?:"en"
    override val name:String
        get()=codeToLanguage(code)
}

actual fun setLanguage(code:String){
    js("window.localStorage.setItem('language',code)")
}