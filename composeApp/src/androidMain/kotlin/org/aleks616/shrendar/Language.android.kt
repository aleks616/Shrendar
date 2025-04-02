package org.aleks616.shrendar

import android.content.Context
import java.util.*

class LanguageAndroid:Language{
    override val code:String=getAndroidContext().getSharedPreferences("com.aleks616.shrendar",Context.MODE_PRIVATE).getString("language",Locale.getDefault().language)!!
    override val name:String=codeToLanguage(Locale.getDefault().language)
}

actual fun getLanguage():Language=LanguageAndroid()
