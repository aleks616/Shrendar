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

}