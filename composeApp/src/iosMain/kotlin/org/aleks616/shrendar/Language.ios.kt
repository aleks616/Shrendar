package org.aleks616.shrendar

import platform.Foundation.NSLocale
import platform.Foundation.currentLocale
import platform.Foundation.languageCode

class LanguageIOS:Language{
    override val code:String=NSLocale.currentLocale.languageCode
    override val name:String=codeToLanguage(code)
}

actual fun getLanguage():Language=LanguageIOS()