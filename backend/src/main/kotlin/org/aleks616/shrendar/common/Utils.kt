package org.aleks616.shrendar.common

object Utils{
    /**
     * @param month value 1-12 NOT 0-11
     * @param day value 1-31
     * assumes February can be 29 days long, as there's no year param
     * **/
    fun doesDateExist(month:Int,day:Int):Boolean{
        return if(month>12 || day>31) false
        else if(month==2&&day>29) false
        else if((month in setOf(4,6,9,11))&&day>30) false
        else true
    }
}