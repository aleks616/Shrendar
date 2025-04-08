package org.aleks616.shrendar

import java.io.File
import java.lang.management.ManagementFactory
import java.util.*
import java.util.prefs.Preferences
import kotlin.system.exitProcess

class LanguageDesktop:Language{
    override val code:String
        get()=Preferences.userNodeForPackage(this::class.java).get("language",Locale.getDefault().language)
    override val name:String=codeToLanguage(code)
}

actual fun getLanguage():Language=LanguageDesktop()
actual fun setLanguage(code:String){
    Preferences.userNodeForPackage(LanguageDesktop::class.java).put("language", code)
}