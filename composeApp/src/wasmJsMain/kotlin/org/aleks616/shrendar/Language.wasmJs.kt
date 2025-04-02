package org.aleks616.shrendar
import kotlinx.browser.window

class LanguageWasmJs:Language {
    override val code:String=window.navigator.language.substring(0,2)
    override val name:String=codeToLanguage(code)
}

actual fun getLanguage():Language=LanguageWasmJs()