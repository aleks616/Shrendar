package org.aleks616.shrendar

import platform.Foundation.NSLocale
import platform.Foundation.NSUserDefaults
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

class LanguageIOS:Language{
    private val defaults=NSUserDefaults.standardUserDefaults
    override val code:String
        get()=defaults.stringForKey("language") ?: NSLocale.currentLocale.languageCode
    override val name:String=codeToLanguage(code)
}

actual fun getLanguage():Language=LanguageIOS()
actual fun setLanguage(code:String){
    NSUserDefaults.standardUserDefaults.setObject(code, "language")
    NSUserDefaults.standardUserDefaults.synchronize()
}