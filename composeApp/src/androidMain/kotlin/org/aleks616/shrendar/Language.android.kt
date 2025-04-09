package org.aleks616.shrendar

import android.content.Context
import android.content.res.Configuration
import java.util.*

class LanguageAndroid:Language{
    override val code:String=getAndroidContext().getSharedPreferences("com.aleks616.shrendar",Context.MODE_PRIVATE)
        .getString("language",Locale.getDefault().language)!!
    override val name:String=codeToLanguage(Locale.getDefault().language)
}

actual fun getLanguage():Language=LanguageAndroid()

actual fun setLanguage(code:String){
    val prefs=getAndroidContext().getSharedPreferences("com.aleks616.shrendar",Context.MODE_PRIVATE)
    prefs.edit().putString("language",code)
    Locale.setDefault(Locale(code))

    val resources=getAndroidContext().resources
    val config=Configuration(resources.configuration)
    config.setLocale(Locale(code))

    getAndroidContext().createConfigurationContext(config)
    resources.updateConfiguration(config, resources.displayMetrics)
}