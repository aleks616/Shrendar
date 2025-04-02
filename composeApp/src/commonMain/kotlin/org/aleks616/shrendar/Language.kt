package org.aleks616.shrendar

interface Language {
    val code:String
    val name:String
}
fun languageToCode(language: String):String{
    return when(language.lowercase()){
        "english"->"en"
        "polski"->"pl"
        /*"français"->"fr"
        "português"->"pt-BR"
        "русский"->"ru"
        "українська"->"uk"*/
        else->{
            "en"
        }
    }
}

fun codeToLanguage(code:String):String{
    return when(code.lowercase()){
        "en"->"English"
        "pl"->"Polski"
      /*  "fr"->"Français"
        "pt"->"Português"
        "pt-br"->"Português"
        "ru"->"Русский"
        "uk"->"Українська"*/
        else-> {
            "English"
        }
    }
}

expect fun getLanguage():Language