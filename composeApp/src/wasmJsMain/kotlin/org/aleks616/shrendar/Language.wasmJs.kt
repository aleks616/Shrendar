package org.aleks616.shrendar
import kotlinx.browser.window

class LanguageWasmJs:Language {
    override val code:String
        get()=window.localStorage.getItem("language") ?: window.navigator.language.substring(0,2)
    override val name:String=codeToLanguage(code)
}

actual fun getLanguage():Language=LanguageWasmJs()
actual fun setLanguage(code:String){
    window.localStorage.setItem("language",code)
    window.location.reload()
}