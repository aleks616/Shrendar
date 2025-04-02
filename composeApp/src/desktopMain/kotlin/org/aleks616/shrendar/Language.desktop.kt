package org.aleks616.shrendar

import java.util.*

class LanguageDesktop:Language{
    override val code:String=Locale.getDefault().language
    override val name:String=codeToLanguage(code)
}

actual fun getLanguage():Language=LanguageDesktop()