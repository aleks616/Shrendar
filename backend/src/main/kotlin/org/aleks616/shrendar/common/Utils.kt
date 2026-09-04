package org.aleks616.shrendar.common

import java.net.MalformedURLException
import java.net.URL
import java.time.LocalDate
import java.time.temporal.ChronoUnit


object Utils{
    const val LIMIT_BASIC=3
    const val LIMIT_HIGH=10
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

    fun getDaysTillNextAnniversary(birthDate:LocalDate?):Int {
        if(birthDate==null) return -1
        val isAfterFebruary=LocalDate.now().monthValue>2
        val isNextFebruaryYearLeap=if(isAfterFebruary) LocalDate.now().plusYears(1).isLeapYear else LocalDate.now().isLeapYear
        val actualDay=if(birthDate.monthValue==2&&birthDate.dayOfMonth==29&&!isNextFebruaryYearLeap) 28 else birthDate.dayOfMonth
        val thisYearAnn=LocalDate.of(LocalDate.now().year,birthDate.monthValue,actualDay)
        val nextYearAnn=LocalDate.of(LocalDate.now().year+1,birthDate.monthValue,actualDay)
        val nextAnn=if(!thisYearAnn.isBefore(LocalDate.now())) thisYearAnn else nextYearAnn
        return LocalDate.now().until(nextAnn,ChronoUnit.DAYS).toInt()
    }

    fun isValidUrl(url:String?):Boolean {
        if(url==null) return true
        if(url.length>255) return false
        try {
            URL(url)
            return true
        }
        catch(_:MalformedURLException) {
            return false
        }
    }
}