package org.aleks616.shrendar

object Utils{
    fun canBeIntParsed(text:String):Boolean{
        if(text.isNotEmpty()){
            return text.all{it.isDigit()}
        }
        return false
    }
    fun isStringANumber(text:String):Boolean{
        return text.all{it.isDigit()}
    }

   fun isValidPastOrPresentYear(year:Int):Boolean{
        return (2000..2025).contains(year)
    }

    fun isDigitsOnly(str:CharSequence):Boolean{
        for (char in str){
            if (char !in '0'..'9')
                return false
        }
        return true
    }

    fun isPasswordSafe(password:String):Boolean{
        if(!(8..32).contains(password.length)) return false

        var upper=false
        var lower=false
        var digit=false
        var symbol=false
        val symbolsSet=setOf('!','@','#','$','%','^','&','*','(',')','-','_','+','=','~','`','<','>',',','.','?','/','|','[',']','{','}',':',';')

        for(ch in password){
            if(ch.isUpperCase()) upper=true
            else if(ch.isLowerCase()) lower=true
            else if(ch.isDigit()) digit=true
            else if(ch in symbolsSet) symbol=true
            if(upper&&lower&&digit&&symbol) return true
        }
        return upper&&lower&&digit&&symbol
    }

    fun isEmailValid(email:String):Boolean{
        if (email.isBlank()) return false
        val regex="^(?:[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*|\"(?:[\\x01-\\x08\\x0b\\x0c\\x0e-\\x1f\\x21\\x23-\\x5b\\x5d-\\x7f]|\\\\[\\x01-\\x09\\x0b\\x0c\\x0e-\\x7f])*\")@(?:(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\\.)*[a-z0-9](?:[a-z0-9-]*[a-z0-9])?|\\[(?:IPv6:[a-f0-9:]+|(?:(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9])\\.){3}(?:25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9]?[0-9]))])$"

        return Regex(regex).matches(email)
    }

    /**
     * @param locale - language code
     * @param key - [StringLocale] key, eg. sc.NAME
     * **/
    fun getTranslation(locale:String,key:String):String{
        val sc=StringLocale
        val st=sc.translations
        return st[key]?.get(locale)?:""
    }


}